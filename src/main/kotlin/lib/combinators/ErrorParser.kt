package lib.combinators

import lib.Parser
import lib.ParserResult
import lib.ParserState
import lib.TokenType

//TODO the error parser breaks backtracking

//val empty: Parser<TokenType, Unit> = EagerParser({ParserResult.Ok(this, Unit)}, "empty")
//
//
//class ErrorParser<Type : TokenType>: Parser<Type, Nothing> {
//	override val name: String
//		get() = TODO("not implemented")
//
//	override fun eval(state: ParserState<Type>): ParserResult<Type, Nothing> =
//		ParserResult.Error.NoChoice(state.head)
//	override fun backtrack(): Parser<Type, Nothing>? = null
//}
//
//fun <Type : TokenType> errorParser(): Parser<Type, Nothing> = ErrorParser()