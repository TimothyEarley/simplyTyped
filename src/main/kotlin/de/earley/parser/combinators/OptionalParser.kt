package de.earley.parser.combinators

import de.earley.parser.*


class OptionalParser<Type : TokenType, T>(
	val parser: Parser<Type, T>
) : Parser<Type, T?> {
	override val name: String
		get() = TODO("not implemented")

	override fun eval(state: ParserState<Type>): ParserResult<Type, T?> =
		parser.applyRule(state).flatMapLeft {
			ParserResult.Ok(state.addSkippedError(it), null)
		}

	override fun backtrack(): Parser<Type, T?>? =
		parser.backtrack()?.let { optional(it) }
}

fun <Type : TokenType, R> optional(parser: Parser<Type, R>): Parser<Type, R?> =
	OptionalParser(parser)