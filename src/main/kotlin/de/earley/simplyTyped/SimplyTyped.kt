package de.earley.simplyTyped

import de.earley.newParser.ParseResult
import de.earley.newParser.deriveAll
import de.earley.parser.*
import de.earley.simplyTyped.parser.SimplyTypedGrammar
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.parser.handleError
import de.earley.simplyTyped.terms.*
import de.earley.simplyTyped.types.recover
import de.earley.simplyTyped.types.resolveUserTypes
import de.earley.simplyTyped.types.type
import java.io.InputStream
import kotlin.system.exitProcess

@ExperimentalStdlibApi
fun main() {
	processFile("/example.tl")
}

@ExperimentalStdlibApi
fun readInput(input : InputStream) = input.readBytes().decodeToString()

fun extraParens(src : String) : String = "($src)"

fun lexer(src: String) = lex(src, values(), EOF)
		.filter { it.type != WS }
		.filter { it.type != COMMENT }

fun parser(tokens: TokenStream<SimplyTypedLambdaToken>): TypedTerm = SimplyTypedGrammar.grammar.run(tokens).orExit()

fun newParser(tokens : TokenStream<SimplyTypedLambdaToken>): TypedTerm =
		SimplyTypedGrammar.newGrammar.deriveAll(tokens.toSequence())
				.handleResult()

fun ParseResult<Token<SimplyTypedLambdaToken>, TypedTerm>.handleResult() : TypedTerm = when (this) {
	is ParseResult.Ok.Single -> t
	is ParseResult.Ok.Multiple -> {
		println("${set.size} trees found.")
		set.first()
	}
	is ParseResult.Error -> handleError(this)
	is ParseResult.Ok.Maybe<*, TypedTerm> -> {
		println("Hidden error $error")
		ok.handleResult()
	}
}

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

val processSource : (String) -> UntypedNamelessTerm =
	::lexer +
	::newParser +
	::checkFreeVariables +
	::unname +
	::resolveUserTypes +
	::typeCheck +
	::removeTypes +
	::eval

@ExperimentalStdlibApi
fun processInput(input : InputStream) = processSource(readInput(input))

@ExperimentalStdlibApi
fun processFile(file : String) = processInput(SimplyTypedGrammar::class.java.getResourceAsStream(file))