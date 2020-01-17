package de.earley.parser.combinators

import de.earley.parser.*

class IsAParser<Type : TokenType>(
	val context: ParserContext,
	val type: Type
): Parser<Type, Token<Type>> {
	override val name: String = "isA<$type>"

	override fun eval(state: ParserState<Type>): ParserResult<Type, Token<Type>> = with(state) {
		context.match(type)
	}
	override fun backtrack(): Parser<Type, Token<Type>>? = null
}

fun <Type : TokenType> ParserContext.isA(type: Type): Parser<Type, Token<Type>>
		= IsAParser(this, type)

val <Type : TokenType> Parser<Type, Token<Type>>.string get() = map { it.value }

//CLEANUP
class MatchParser<Type : TokenType>(
	val context: ParserContext,
	val type: Type,
	val value: String
): Parser<Type, Token<Type>> {
	override val name: String = "isAMatch<$type,$value>"

	override fun eval(state: ParserState<Type>): ParserResult<Type, Token<Type>> = with(state) {
		context.match(type, value)
	}
	override fun backtrack(): Parser<Type, Token<Type>>? = null
}

fun <Type : TokenType> ParserContext.isAMatch(type: Type, value: String): Parser<Type, Token<Type>> = MatchParser(this, type, value)