package de.earley.simplyTyped

import de.earley.parser.*
import de.earley.simplyTyped.parser.SimplyTypedGrammar
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.*
import de.earley.simplyTyped.types.recover
import de.earley.simplyTyped.types.type
import kotlin.system.exitProcess

@ExperimentalStdlibApi
fun main() {
	//TODO add actual numbers

	val src = SimplyTypedGrammar::class.java.getResourceAsStream("/refs.tl")
		.readBytes().decodeToString()
	val tokens = lex("($src)", values(), EOF)
		.filter { it.type != WS }
	val parsed = SimplyTypedGrammar.grammar.run(tokens).orThrow()

	require(parsed.freeVariables().isEmpty()) { "free variables: " + parsed.freeVariables() }

	val type = parsed.type().recover {
		println("Typing error: ${it.msg} \nin ${it.element}")
		exitProcess(1)
	}

	println("$parsed : $type")
	println("~>")
	println(parsed.eval())

}