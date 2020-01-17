package de.earley.parser.combinators

import de.earley.parser.Parser
import de.earley.parser.ParserResult
import de.earley.parser.ParserState
import de.earley.parser.TokenType

private class NOPParser<Type : TokenType> : Parser<Type, VOID> {
	override val name: String = "NOP"

	override fun eval(state: ParserState<Type>): ParserResult.Ok<Type, VOID> =
		ParserResult.Ok(state, VOID)

	override fun backtrack(): Parser<Type, VOID>? = null
}

fun <Type: TokenType> nop(): Parser<Type, VOID> = NOPParser()