package de.earley.simplyTyped

import de.earley.newParser.ErrorData
import de.earley.newParser.ParseResult
import de.earley.newParser.deriveAll
import de.earley.parser.*
import de.earley.simplyTyped.parser.SimplyTypedGrammar
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.*
import de.earley.simplyTyped.types.addFolding
import de.earley.simplyTyped.types.recover
import de.earley.simplyTyped.types.resolveUserTypes
import de.earley.simplyTyped.types.type
import kotlin.system.exitProcess

@ExperimentalStdlibApi
fun main() {
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
			is ParseResult.Ok.Multiple -> result.set.also { println(it) }.first()
			is ParseResult.Error -> {
				System.err.println("Error parsing:")
				println(result.error.pretty())
				exitProcess(1)
			}
		}

private fun ErrorData.pretty(): String = when (this) {
	ErrorData.Fix -> "No candidate in recursion"
	ErrorData.EmptyCombine -> TODO()
	is ErrorData.Multiple -> errors.joinToString(separator = "\n - ") { it.pretty() }
	is ErrorData.Expected<*> -> "Expected $expected, but got $actual"
	is ErrorData.Filtered<*> -> TODO()
	is ErrorData.TempMsg -> TODO()
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