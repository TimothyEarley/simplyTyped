package lib.combinators

import lib.*

class MapParser<Type : TokenType, A, B>(
	private val a: Parser<Type, A>,
	private val f: (A) -> B
) : Parser<Type, B> {
	override val name: String = "m(${a.name})"
	override fun eval(state: ParserState<Type>): ParserResult<Type, B> = a.applyRule(state).map(f)
	override fun backtrack(): Parser<Type, B>? = a.backtrack()?.map(f)
}

fun <Type : TokenType, A, B> Parser<Type, A>.map(f: (A) -> B): Parser<Type, B>
		= MapParser(this, f)

fun <Type : TokenType, A, B, C> Parser<Type, Pair<A, B>>.map(f: (A, B) -> C): Parser<Type, C>
		= MapParser(this) { f(it.first, it.second) }