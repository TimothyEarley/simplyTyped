import Term.*
import lib.*
import lib.combinators.*
import java.lang.IllegalStateException

// λ

enum class LambdaToken(r: String): TokenType {
	Lambda("λ"),
	Identifier("\\w+"),
	Dot("\\."),
	OpenParen("\\("),
	ClosedParen("\\)"),
	Colon(":"),
	WS("\\s"),
	EOF("\\z"); //TODO better EOF handling

	override val regex = Regex(r)
}

typealias P<R> = Parser<LambdaToken, R>

object Grammar {

	private val variable: P<Variable> = context("var") {
		isA(LambdaToken.Identifier).string.map(::Variable)
	}

	private val abstraction: P<Abstraction> = context("abs") {
		(
				isA(LambdaToken.Lambda).void() +
				isA(LambdaToken.Identifier).string +
				isA(LambdaToken.Dot).void() +
				term
		).map(::Abstraction)
	}
	private val app: P<App> = context("app") {
		((parenTerm or variable) + (parenTerm or variable)).map(::App)
	}

	private val parenTerm: P<Term> = context("paren") {
		isA(LambdaToken.OpenParen).void() + term + isA(LambdaToken.ClosedParen).void()
	}

	private val term= context("term") {
		app or parenTerm or abstraction or variable
	}

	val grammar = context("root") {
		(term + isA(LambdaToken.EOF).void()) //.map { term, _ -> term } // we have no backtracking, so if a term is found then no other possible terms are checked
	}
}

fun parse(src: String, debug: Boolean = false): ParserResult<LambdaToken, Term> {
	val tokens = lex("($src)", LambdaToken.values(), LambdaToken.EOF)
		.filter { it.type != LambdaToken.WS }
	return Grammar.grammar.run(tokens, debug)
}

private fun examples() {
	listOf(
		"λx.x",
		"(λx.x) (λx.x)",
		"((λx.x) (λy.y)) (λz.z)",
		"((λx. λy. x) (λy. y)) (λx. x)",
		"λx.(λy.x)"
	).forEach {
		print(it)
		val parsed = parse(it).orThrow()
		print(" ~> ")
		println(parsed.toNameless().eval().toNamed())
	}
}

fun main() {

	val zero = "(λs.λz.z)"
	val succ = "(λn.λs.λz.s ((n s) z))"

	val two = "($succ ($succ $zero))"

	val matches = mapOf(
		parse(zero).orThrow() to "zero",
		parse(succ).orThrow() to "succ",
		parse(two).orThrow() to "two"
	)

	println(parse(two))
	println(two.eval())
	println(two.eval().matchAll(matches))

}


private fun String.eval(): Term = parse(this)
	.orThrow()
	.toNameless()
	.eval()
	.toNamed()

private fun Term.matchAll(map: Map<Term, String>): Term =
	map.entries.fold(this) { acc, (term, name) ->
		acc.match(term, name)
	}

private fun Term.match(term: Term, name: String): Term =
	if (this.alphaEqual(term)) Variable(name)
	else when (this) {
		is Variable -> this
		is Abstraction -> Abstraction(binder, body.match(term, name))
		is App -> App(left.match(term, name), right.match(term, name))
}

private fun Term.alphaEqual(other: Term): Boolean =
	if (this === other) true
	else if (this is Variable && other is Variable) true // can always rename variables
	else try {
		this.toNameless() == other.toNameless()
	} catch (e: IllegalStateException) {
		false
	}
