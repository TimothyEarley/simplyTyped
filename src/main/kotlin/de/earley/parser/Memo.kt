package de.earley.parser

import java.util.function.BiFunction

// STATEFUL class
class Memo<Type : TokenType> {
	private val memo: MutableMap<
			Parser<Type, *>,
			MutableMap<Token<Type>, ParserResult<Type, *>>
			> = mutableMapOf()

	fun <R> read(
		parser: Parser<Type, R>,
		token: Token<Type>
	): ParserResult<Type, R>? {
		return memo.get(parser)?.get(token) as ParserResult<Type, R>?
	}

	fun <R> write(
		parser: Parser<Type, R>,
		token: Token<Type>,
		result: ParserResult<Type, R>
	) {
		memo.merge(
			parser,
			mutableMapOf(token to result),
			BiFunction { old, _ ->
				val o = old as MutableMap<Token<Type>, ParserResult<Type, R>>
				o[token] = result
				old
			}
		)
	}
}