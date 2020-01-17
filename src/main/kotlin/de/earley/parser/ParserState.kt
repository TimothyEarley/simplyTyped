package de.earley.parser

typealias Writer = (() -> String) -> Unit
/**
 * Input to a parser
 */
class ParserState<Type: TokenType> private constructor(
	private val tokens: TokenStream<Type>,
	val memo: Memo<Type>, // constant
	val writer: Writer, // constant
	val chain: List<Parser<Type, *>>,
	private val skippedError: ParserResult.Error<Type>? = null
) {
	val head: Token<Type>? get() = tokens.head

	companion object {
		fun <Type: TokenType> of(tokens: TokenStream<Type>, writer: Writer) =
			ParserState(tokens, Memo(), writer, emptyList())
	}

	fun ParserContext.match(type: Type): ParserResult<Type, Token<Type>> {
		val head = head
		return when (head?.type) {
			type -> ParserResult.Ok(this@ParserState.nextState(), head)
			else -> skippedError nneither ParserResult.Error.Single(this, type, null, head)
		}
	}

	fun ParserContext.match(type: Type, value: String): ParserResult<Type, Token<Type>> {
		val head = head
		return if (head?.type == type && head.value == value) ParserResult.Ok(this@ParserState.nextState(), head)
		else skippedError nneither ParserResult.Error.Single(this, type, value, head)
	}

	fun addSkippedError(e: ParserResult.Error<Type>): ParserState<Type> =
		ParserState(
			tokens = tokens,
			skippedError = skippedError nneither e,
			memo = memo,
			writer = writer,
			chain = chain
		)

	fun addToChain(parser: Parser<Type, *>) = ParserState(
		tokens = tokens,
		skippedError = skippedError,
		memo = memo,
		writer = writer,
		chain = chain + parser
	)

	private fun nextState(): ParserState<Type> = ParserState(
		tokens = tokens.tail,
		skippedError = skippedError,
		memo = memo,
		writer = writer,
		chain = chain
	)

	fun setChain(newChain: List<Parser<Type, *>>): ParserState<Type> = ParserState(
		tokens = tokens,
		skippedError = skippedError,
		memo = memo,
		writer = writer,
		chain = newChain
	)
}