package simplyTyped

import lib.*
import lib.combinators.*
import simplyTyped.SimplyTypedLambdaToken.*
import simplyTyped.Type.FunctionType
import simplyTyped.TypedTerm.*
import untyped.eval
import untyped.toNamed
import untyped.toNameless

enum class SimplyTypedLambdaToken(r: String): TokenType {
	Lambda("λ"),
	Identifier("\\w+"),
	Dot("\\."),
	OpenParen("\\("),
	ClosedParen("\\)"),
	Colon(":"),
	Arrow("->"),
	WS("\\s"),
	EOF("\\z"); //TODO better EOF handling

	override val regex = Regex(r)
}

private typealias P<R> = Parser<SimplyTypedLambdaToken, R>

object SimplyTypedGrammar {

	private val variable: P<Variable> = context("var") {
		isA(Identifier).string.map(::Variable)
	}

	private val parenType: P<Type> = context("paren type") {
		isA(OpenParen).void() + type + isA(ClosedParen).void()
	}

	private val functionType: P<FunctionType> = context("function type") {
		(type + isA(Arrow).void() + type).map(::FunctionType)
	}

	private val type: P<Type> = context("type") {
		parenType or functionType or isAMatch(Identifier, "Nat").map { Type.Nat }
	}

	private val abstraction: P<Abstraction> = context("abs") {
		(
				isA(Lambda).void() +
						isA(Identifier).string +
						isA(Colon).void() +
						type +
						isA(Dot).void() +
						term
				).map(::Abstraction)
	}
	private val app: P<App> = context("app") {
		((parenTerm or variable) + (parenTerm or variable)).map(::App)
	}

	private val parenTerm: P<TypedTerm> = context("paren") {
		isA(OpenParen).void() + term + isA(ClosedParen).void()
	}

	private val term: P<TypedTerm> = context("term") {
		app or parenTerm or abstraction or variable
	}

	val grammar = context("root") {
		(term + isA(EOF).void()) //.map { term, _ -> term } // we have no backtracking, so if a term is found then no other possible terms are checked
	}
}

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
		"((λx:Nat. λy:Nat. x) (λy:Nat. y)) (λ:Natx. x)",
		"λx:Nat.(λy:Nat.x)"
	).forEach {
		print(it)
		val parsed = parse(it).orThrow()
		print(" ~> ")
		println(parsed.toUntyped().toNameless().eval().toNamed())
	}
}

fun main() {
	examples()
}