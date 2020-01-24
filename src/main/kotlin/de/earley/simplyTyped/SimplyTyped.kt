package de.earley.simplyTyped

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
		::lexer +
//		::debugTokens +
		::parser +
		::checkFreeVariables +
		::unname +
		::resolveUserTypes +
		::addFolding +
		::typeCheck +
		::removeTypes +
		::eval +
		::log

	process("/counter.tl")
	process("/source.tl")
	process("/list.tl")

}

@ExperimentalStdlibApi
fun readSrc(file: String) = SimplyTypedGrammar::class.java.getResourceAsStream(file)
	.readBytes().decodeToString()

fun lexer(src: String) = lex("($src)", values(), EOF)
	.filter { it.type != WS }

fun parser(tokens: TokenStream<SimplyTypedLambdaToken>) = SimplyTypedGrammar.grammar.run(tokens).orExit()

fun checkFreeVariables(parsed: TypedTerm): TypedTerm {
	require(parsed.freeVariables().isEmpty()) { "free variables: " + parsed.freeVariables() }
	return parsed
}

fun typeCheck(parsed: TypedNamelessTerm): TypedNamelessTerm {
	val type = parsed.type().recover {
		System.err.println("Typing error: ${it.msg} \nin ${it.element}")
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