package de.earley.simplyTyped

import de.earley.parser.*
import de.earley.simplyTyped.parser.SimplyTypedGrammar
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.*
import de.earley.simplyTyped.types.recover
import de.earley.simplyTyped.types.type
import kotlin.system.exitProcess

@ExperimentalStdlibApi
fun main() {

	val process =
		::readSrc +
		::lexer +
		::parser +
		::checkFreeVariables +
		::typeCheck +
		::compile +
		::eval +
		::println
	process("/refs.tl")

}

@ExperimentalStdlibApi
fun readSrc(file: String) = SimplyTypedGrammar::class.java.getResourceAsStream(file)
	.readBytes().decodeToString()

fun lexer(src: String) = lex("($src)", values(), EOF)
	.filter { it.type != WS }

fun parser(tokens: TokenStream<SimplyTypedLambdaToken>) = SimplyTypedGrammar.grammar.run(tokens).orThrow()

fun checkFreeVariables(parsed: TypedTerm): TypedTerm {
	require(parsed.freeVariables().isEmpty()) { "free variables: " + parsed.freeVariables() }
	return parsed
}

fun typeCheck(parsed: TypedTerm): TypedTerm {
	val type = parsed.type().recover {
		println("Typing error: ${it.msg} \nin ${it.element}")
		exitProcess(1)
	}
	println("$parsed : $type")

	return parsed
}

fun compile(parsed: TypedTerm): UntypedNamelessTerm =
	parsed.toNameless(emptyMap()).toUntyped()


operator fun <A, B, C> ((A) -> B).plus(other: (B) -> C): (A) -> C = {
	other(this(it))
}