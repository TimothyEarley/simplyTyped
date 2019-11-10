import Term.*
import lib.*

// λ

enum class LambdaToken(r: String): TokenType {
	Lambda("λ"),
	Identifier("\\w+"),
	Dot("\\."),
	OpenParen("\\("),
	ClosedParen("\\)"),
	Colon(":"),
	EOF("");

	override val regex = Regex(r)
}

sealed class Term {
	data class Variable(val name: String): Term() {
		override fun toString(): String = name
	}
	data class Abstraction(val binder: String, val body: Term): Term() {
		override fun toString(): String = "(λ$binder.$body)"
	}
	data class App(val left: Term, val right: Term): Term() {
		override fun toString(): String = "($left $right)"
	}
}

typealias P<R> = Parser<LambdaToken, R>

object Grammar {

	private val variable: P<Variable> = context("variable") {
		isA(LambdaToken.Identifier).string.map(::Variable)
	}

	private val abstraction: P<Abstraction> = context("abstraction") {
		(
				isA(LambdaToken.Lambda).void() +
				isA(LambdaToken.Identifier).string +
				isA(LambdaToken.Dot).void() +
				term
		).map(::Abstraction)
	}
	private val app: P<App> = context("app") {
		(term + term).map(::App)
	}

	private val parenTerm = context("parenthesis") {
		isA(LambdaToken.OpenParen).void() + term + isA(LambdaToken.ClosedParen).void()
	}

	// broken
	private val term: P<Term> = parenTerm or app or abstraction or variable

	val grammar = context("root") {
		term + isA(LambdaToken.EOF).void()
	}
}

fun main() {
	val lexed = lex("(λx.x)(λy.y)(λz.z)", LambdaToken.values(), LambdaToken.EOF)
	println("Lexed: ")
	println(lexed.toList().map { it.type })
	val parsed = Grammar.grammar.run(lexed)

	println(parsed)

}
