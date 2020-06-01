package de.earley.simplyTyped

import de.earley.newParser.ErrorData
import de.earley.newParser.ParseResult
import de.earley.newParser.deriveAll
import de.earley.newParser.graphWriter
import de.earley.parser.*
import de.earley.simplyTyped.parser.SimplyTypedGrammar
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.parser.TermGrammar
import de.earley.simplyTyped.terms.*
import de.earley.simplyTyped.types.recover
import de.earley.simplyTyped.types.resolveUserTypes
import de.earley.simplyTyped.types.type
import kotlin.system.exitProcess

@ExperimentalStdlibApi
fun main() {
	TermGrammar.newTerm.graphWriter("grammar.dot")

	val process =
		::readSrc +
//		::extraParens +
		::lexer +
//		::debugTokens +
//		::parser +
		::newParser +
//		::treeVis +
		::checkFreeVariables +
		::unname +
		::resolveUserTypes +
//		::addFolding +
		::typeCheck +
		::removeTypes +
		::eval +
		::log

//	process("/simple.tl")
//	process("/list.tl")
	process("/source.tl")
//	process("/counter.tl")

}

@ExperimentalStdlibApi
fun readSrc(file: String) = SimplyTypedGrammar::class.java.getResourceAsStream(file)
	.readBytes().decodeToString()

fun extraParens(src : String) : String = "($src)"

fun lexer(src: String) = lex(src, values(), EOF)
		.filter { it.type != WS }
		.filter { it.type != COMMENT }

fun parser(tokens: TokenStream<SimplyTypedLambdaToken>): TypedTerm = SimplyTypedGrammar.grammar.run(tokens).orExit()

fun newParser(tokens : TokenStream<SimplyTypedLambdaToken>): TypedTerm =
		when (val result = SimplyTypedGrammar.newGrammar.deriveAll(tokens.toSequence())) {
			is ParseResult.Ok.Single -> result.t
			is ParseResult.Ok.Multiple -> result.set.also {
				//TODO way too many trees
				println("${result.set.size} trees found.")
			}.first()
			is ParseResult.Error -> {
				System.err.println("Error parsing:")
				println(result.error /* .max().second */.strip(false)?.pretty())
				exitProcess(1)
			}
		}

/**
 * Simplify the error data
 */
private fun ErrorData<Token<*>>.strip(force : Boolean): ErrorData<Token<*>>? = when (this) {
	ErrorData.Fix -> null
	ErrorData.EmptyCombine -> null
	is ErrorData.ExpectedName -> if (actual == null) null else this
	is ErrorData.ExpectedEnd -> if (force) null else this
	is ErrorData.Filtered<*> -> this
	is ErrorData.Named -> {
		when (val inner = data.strip(force)) {
			is ErrorData.Named -> ErrorData.Named("$name.${inner.name}", inner.data)
			else -> inner?.let { ErrorData.Named(name, inner) }
		}
	}
	is ErrorData.Multiple -> {
		val inner = errors.mapNotNull { it.strip(force) }
		when (inner.size) {
			0 -> null //TODO this seems wrong
			1 -> inner.single()
			else -> ErrorData.Multiple.from(inner)
		}
	}
}

fun ErrorData<Token<*>>.max() : Pair<SourcePosition, ErrorData<Token<*>>> = when (this) {
    ErrorData.Fix -> SourcePosition.Start to this
    ErrorData.EmptyCombine -> SourcePosition.Start to this
    is ErrorData.ExpectedName -> (actual?.src() ?: SourcePosition.Start) to this
    is ErrorData.ExpectedEnd -> actual.src() to this
    is ErrorData.Filtered<*> -> SourcePosition.Start to this
    is ErrorData.Named -> data.max().let { (pos, d) -> pos to ErrorData.Named(name, d) }
    is ErrorData.Multiple -> errors.map { it.max() }.maxBy { it.first }!! // non empty
}

private fun ErrorData<Token<*>>.pretty(): String = this.treeString(
		{ when(this) {
			ErrorData.Fix -> "End of recursion"
			ErrorData.EmptyCombine -> TODO()
			is ErrorData.Filtered<*> -> TODO()
			is ErrorData.Named -> "<${name}>"
			is ErrorData.Multiple -> "Multiple errors:"
			is ErrorData.ExpectedName -> "(${actual?.src()}) Expected [$expected], got [${actual?.value}] (${actual?.type})"
			is ErrorData.ExpectedEnd -> "(${actual.src()}) Expected the end, got [${actual.value}] (${actual.type})"
		} },
		{ when(this) {
			ErrorData.Fix -> emptyList()
			ErrorData.EmptyCombine -> TODO()
			is ErrorData.Filtered<*> -> TODO()
			is ErrorData.Named -> listOf(data)
			is ErrorData.Multiple -> errors.toList()
			is ErrorData.ExpectedName -> emptyList()
			is ErrorData.ExpectedEnd -> emptyList()
		} }
)

fun treeVis(t : TypedTerm): TypedTerm = t.also {
	println(t.tree())
}

fun checkFreeVariables(parsed: TypedTerm): TypedTerm {
	require(parsed.freeVariables().isEmpty()) { "free variables: ${parsed.freeVariables()} in $parsed" }
	return parsed
}

fun typeCheck(parsed: TypedNamelessTerm): TypedNamelessTerm {
	val type = parsed.type().recover {
		System.err.println("Typing error: ${it.msg} \nat: ${it.element} \nat ${it.element.src}")
		exitProcess(1)
	}
	println("$parsed : $type")

	return parsed
}

fun removeTypes(parsed: TypedNamelessTerm): UntypedNamelessTerm = parsed.toUntyped()

fun unname(named: TypedTerm): TypedNamelessTerm = named.toNameless(emptyMap())

fun <T> log(t: T) = t.also {
	println(t)
}

fun debugTokens(tokens: TokenStream<SimplyTypedLambdaToken>) = tokens.also {
	println(tokens.toList().map { it.type })
}

operator fun <A, B, C> ((A) -> B).plus(other: (B) -> C): (A) -> C = {
	other(this(it))
}


fun <T> T.treeString(
		pretty : T.() -> String,
		children : T.() -> List<T>,
		indent: String = ""
) : String {

	return indent  + pretty(this) + "\n" + children(this).joinToString("") { it.treeString(pretty, children, "$indent|   ") }

}