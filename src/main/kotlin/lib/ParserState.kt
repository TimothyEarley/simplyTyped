package lib

/**
 * Input to a parser
 */
class ParserState<Type: TokenType> private constructor(
	private val tokens: TokenStream<Type>,
	//TODO read the paper as to what chains actually do
	private val chain: List<Parser<Type, Any?>> = emptyList(),
	private val memo: Map<Parser<Type, Any?>, Map<Token<Type>, ParserResult<Type, Any?>>> = emptyMap(),
	private val skippedError: ParserResult.Error<Type>? = null
) {

	fun token(): Token<Type>? {
		return tokens.head
	}

	fun ParserContext.match(type: Type): ParserResult<Type, Token<Type>> {
		val head = tokens.head
		return when (head?.type) {
			type -> ParserResult.Ok(this@ParserState.nextState(), head)
			else -> skippedError nneither ParserResult.Error.Single(this, type, null, head)
		}
	}

	private fun nextState(): ParserState<Type> = ParserState(
		tokens = tokens.tail,
		memo = memo,
		skippedError = skippedError
	)

	fun addSkippedError(e: ParserResult.Error<Type>): ParserState<Type> =
		ParserState(
			tokens = tokens,
			memo = memo,
			skippedError = skippedError nneither e
		)

	// get the memoized result from the parser, if any.
	@Suppress("UNCHECKED_CAST") // we make sure it is type safe in [writeMemo]
	fun <R> readMemo(parser: Parser<Type, R>, atToken: Token<Type>): ParserResult<Type, R>? {
		return memo[parser]?.get(atToken) as? ParserResult<Type, R>
	}

	// mark the parser as failed
	fun <R> writeMemo(parser: Parser<Type, R>, atToken: Token<Type>, result: ParserResult<Type, R>) = //TODO check if already contains
		ParserState(
			tokens = tokens,
			chain = chain + parser,
			memo = memo.toMutableMap().also {
				it.merge(parser, mapOf(atToken to result)) { old, new ->
					old + new
				}
			},
			skippedError = skippedError
		)

	// check if the chain is different from the last call
	fun differentChain(parser: Parser<Type, *>): Boolean {
		if (chain.isEmpty()) return true
		val prev = chain.last()

		// find this parser the last time it was used
		val lastTime = chain.indexOfLast { it == parser }
		val lastTimePrev = chain.getOrNull(lastTime - 1) ?: return true

		return lastTimePrev != prev
	}

	companion object {
		fun <Type: TokenType> of(tokens: TokenStream<Type>) =
			ParserState(tokens)
	}
}