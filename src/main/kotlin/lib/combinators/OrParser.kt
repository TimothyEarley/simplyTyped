package lib.combinators

import lib.*

//TODO expand to arbitrary many options
class OrParser<Type: TokenType, T>(
	val optionA: Parser<Type, T>,
	val optionB: Parser<Type, T>
) : Parser<Type, T> {
	override val name: String = "${optionA.name}|${optionB.name}"

	override fun eval(state: ParserState<Type>): ParserResult<Type, T> =
		optionA.applyRule(state).flatMapLeft { firstError ->
			optionB.applyRule(state).flatMapLeft { firstError neither it }
		}

	override fun backtrack(): Parser<Type, T>? {
		// backtrack the first option, or if not applicable, the second option
		val back = optionA.backtrack()
		return if (back == null) {
			optionB
		} else {
			back or optionB
		}
	}
}

infix fun <Type: TokenType, R> Parser<Type, R>.or(other: Parser<Type, R>): Parser<Type, R>
		= OrParser(this, other)