package lib.combinators

import lib.*

class LazyParser<Type : TokenType, A>(
	provider: () -> Parser<Type, A>
) : Parser<Type, A> {
	val parser by kotlin.lazy(provider)

	override val name: String = "lazy"

	override fun eval(state: ParserState<Type>): ParserResult<Type, A> = parser.applyRule(state)
	override fun backtrack(): Parser<Type, A>? = parser.backtrack()
}

fun <Type: TokenType, R> lazy(f: () -> Parser<Type, R>): Parser<Type, R> = LazyParser(f)