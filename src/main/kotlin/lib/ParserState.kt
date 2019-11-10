package lib

/**
 * Input to a parser
 */
class ParserState<Type: TokenType> private constructor(
	private val tokens: TokenStream<Type>,
	val memo: Memo<Type>,
	val chain: List<Parser<Type, *>>,
	private val skippedError: ParserResult.Error<Type>? = null
) {
	val head: Token<Type>? get() = tokens.head

	companion object {
		fun <Type: TokenType> of(tokens: TokenStream<Type>) =
			ParserState(tokens, Memo(), emptyList())
	}

	fun ParserContext.match(type: Type): ParserResult<Type, Token<Type>> {
		val head = head
		return when (head?.type) {
			type -> ParserResult.Ok(this@ParserState.nextState(), head)
			else -> skippedError nneither ParserResult.Error.Single(this, type, null, head)
		}
	}

	fun addSkippedError(e: ParserResult.Error<Type>): ParserState<Type> =
		ParserState(
			tokens = tokens,
			skippedError = skippedError nneither e,
			memo = memo,
			chain = chain
		)

	fun addToChain(parser: Parser<Type, *>) = ParserState(
		tokens = tokens,
		skippedError = skippedError,
		memo = memo,
		chain = chain + parser
	)

	private fun nextState(): ParserState<Type> = ParserState(
		tokens = tokens.tail,
		skippedError = skippedError,
		memo = memo,
		chain = chain
	)

	fun setChain(newChain: List<Parser<Type, *>>): ParserState<Type> = ParserState(
		tokens = tokens,
		skippedError = skippedError,
		memo = memo,
		chain = newChain
	)
}