import lib.TokenType
import lib.*
import lib.combinators.*

enum class Ts(r: String) : TokenType {
	Char("\\w"),
	OpenParen("\\("),
	ClosedParen("\\)"),
	WS("\\s"),
	EOF("\\z") ;

	override val regex: Regex = r.toRegex()

}

object G {
	private val variable = context("var") { isA(Ts.Char).string }
	private val app = context("app") { term + term }.map(String::plus)
	private val paren = context("paren") {
		isA(Ts.OpenParen).void() + term + isA(Ts.ClosedParen).void()
	}
	private val term: Parser<Ts, String> = context("term") {
		variable or paren or app
	}
	val root: Parser<Ts, String> = context("root") {
		term plusBacktrack isA(Ts.EOF).void()
	}.map { s,_->s }
}

fun main() {

//	val zero = "(z)"
//	val succ = "(s(n s z))"
//
//	val two = "$succ ($succ $zero)"

	val two = "a (b c)"

	val tokens = lex(two, Ts.values(), Ts.EOF).filter { it.type != Ts.WS }
	println(G.root.run(tokens) { println(it()) })
}