package de.earley.simplyTyped

import de.earley.parser.*
import de.earley.simplyTyped.parser.SimplyTypedGrammar
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.*
import de.earley.simplyTyped.types.type

fun parse(src: String, debug: Boolean = false): ParserResult<SimplyTypedLambdaToken, TypedTerm> {
	val tokens = lex("($src)", values(), EOF)
		.filter { it.type != WS }
	return SimplyTypedGrammar.grammar.run(tokens, debug)
}

private fun examples() {
	listOf(
		"λx:Nat.x",
		"(λx:(Nat->Nat).x) (λx:Nat.x)",
		"((λx:Nat.x) (λy:Nat.y)) (λz:Nat.z)",
		"((λx:Nat. λy:Nat. x) (λy:Nat. y)) (λ:Nat. x)", //broken
		"λx:Nat.(λy:Nat.x)"
	).forEach {
		print(it)
		val parsed = parse(it).orThrow()
		print(" ~> ")
		println(parsed.toNameless().toUntyped().eval())
	}
}

fun main() {
	val src = """
		let 1 = (succ 0) in
		let 2 = (succ 1) in
		let 3 = (succ 2) in
		let 4 = (succ 3) in
		let add = (λ a:Nat . λ b:Nat . b) in
		add 3 4
	""".trimIndent()
	// val src = "pred (let id = (λn:Nat.n) in succ (id (id 0)))"
	//TODO "let one = (λx. succ 0) in iszero (one 0)" has wrong error reporting (it is missing a type)
	val parsed = parse(src).orThrow()
	require(parsed.freeVariables().isEmpty()) { "free variables: " + parsed.freeVariables() }
	val type = parsed.type() ?: error("$parsed does not typecheck!")
	println("$parsed : $type")
	println("~>")
	println(parsed.eval())

}