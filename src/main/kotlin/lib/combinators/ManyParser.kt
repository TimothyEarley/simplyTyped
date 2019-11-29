package lib.combinators

import lib.*

class ManyParser<Type : TokenType, T>(
	val parser: Parser<Type, T>
) : Parser<Type, List<T>> {
	override val name: String
		get() = TODO("not implemented")

	override fun eval(state: ParserState<Type>): ParserResult<Type, List<T>> {
		val list = mutableListOf<T>()
		var currentState = state
		loop {
			when (val result = parser.applyRule(currentState)) {
				is ParserResult.Ok -> {
					list += result.result
					currentState = result.next
				}
				is ParserResult.Error -> return ParserResult.Ok(
					currentState.addSkippedError(result),
					list
				)
			}
		}
	}

	override fun backtrack(): Parser<Type, List<T>>? {
		TODO()
	}
}

/**
 * 0 to inf many repetitions
 */
fun <Type : TokenType, R> many(parser: Parser<Type, R>): Parser<Type, List<R>> = ManyParser(parser)