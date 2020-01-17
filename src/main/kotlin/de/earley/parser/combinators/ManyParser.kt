package de.earley.parser.combinators

import de.earley.parser.*

class ManyParser<Type : TokenType, T>(
	val parser: Parser<Type, T>,
	val delimiter: Parser<Type, *>
) : Parser<Type, List<T>> {
	override val name: String
		get() = "many ${parser.name}"

	override fun eval(state: ParserState<Type>): ParserResult<Type, List<T>> {
		val list = mutableListOf<T>()
		var currentState = state
		var isFirst = true
		loop {
			if (!isFirst) {
				when (val result = delimiter.applyRule(currentState)) {
					is ParserResult.Ok -> currentState = result.next
					is ParserResult.Error -> return ParserResult.Ok(
						currentState.addSkippedError(result),
						list
					)
				}
			} else {
				isFirst = false
			}

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
fun <Type : TokenType, R> many(
	parser: Parser<Type, R>,
	delimiter: Parser<Type, *> = nop()
): Parser<Type, List<R>> = ManyParser(parser, delimiter)