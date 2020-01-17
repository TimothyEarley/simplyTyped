package de.earley.parser

private typealias ParseFunction<Type, R> = ParserState<Type>.() -> ParserResult<Type, R>

interface Parser<Type : TokenType, out R> {
	val name: String
	/**
	 * Use [applyRule] if you are not the owner of this parser
	 */
	fun eval(state: ParserState<Type>): ParserResult<Type, R>
	// mark the choice as invalid and create a new parser without it or null if no options are left
	fun backtrack(): Parser<Type, R>?
}

fun <Type: TokenType, R> Parser<Type, R>.applyRule(
	state: ParserState<Type>
): ParserResult<Type, R> {
	val eval = this::eval

	val memo = state.memo
	val head = state.head!!
	val updatedState = state.addToChain(this)

//	state.writer {
//		updatedState.chain
//			.joinToString(separator = " -> ", transform = { it.name }) + " at $head"
//	}

	state.writer {
		"Token: $head\n" + updatedState.toTree().toString()
	}
	state.writer {
		readLine()
		""
	}

	val cached = memo.read(this, head)

	fun execute(): ParserResult<Type, R> {
		memo.write(this, head, ParserResult.Error.LeftRecursion(head))
		val result = eval(updatedState)
		memo.write(this, head, result)
		return result
	}


	return if (cached == null) {
		execute()
	} else {
		if (cached is ParserResult.Error.LeftRecursion && isDifferentContextOfDepth(updatedState.chain, 1)) {
			execute()
		} else {
			if (cached is ParserResult.Error.LeftRecursion) {
				state.writer { "Left recursion avoided: $cached" }
			}
			//update chain from the cached value
			cached.flatMap {
				// find the last index of the cached parser and add that to the current chain
				val cachedChain = it.next.chain
				val last = cachedChain.lastIndexOf(this)
				val postfixChain = cachedChain.subList(last + 1, cachedChain.size)
				it.copy(next = it.next.setChain(updatedState.chain + postfixChain))
			}
		}
	}
}

private fun <Type : TokenType> isDifferentContext(chain: List<Parser<Type, *>>): Boolean {
	val current = chain.last()
	// is it in the chain again?
	val previous = chain.dropLast(1).lastIndexOf(current)
	if (previous == -1) return true // no last occurrence
	// what is the context now, what was it before?
	val context = chain.subList(previous + 1, chain.lastIndex)
	// make sure we have enough context for the previous
	if (previous < context.size) return true
	val previousContext = chain.subList(previous - context.size, previous)

	return ! context.zip(previousContext).all { (a, b) -> a == b }
}

/**
 * Check if the current parser is in the same context as it was before and has reached a certain depth.
 *
 * TODO this is probably a terrible solution
 */
private fun <Type : TokenType> isDifferentContextOfDepth(chain: List<Parser<Type, *>>, depth: Int): Boolean {
	val current = chain.last()

	val immediatePrevious = chain.dropLast(1).lastIndexOf(current)
	if (immediatePrevious == -1) return true

	// skip the last depth occurrences
	val contextBeforeDepthPrevious = (1..depth).fold(chain.dropLast(1)) { l, _ ->
		val prev = l.lastIndexOf(current)
		if (prev == -1) return true
		l.take(prev)
	}

	val context = chain.subList(immediatePrevious + 1, chain.lastIndex)
	val previousContext = contextBeforeDepthPrevious.takeLast(context.size)

	return ! context.zip(previousContext).all { (a, b) -> a == b }
}

//TODO make writer pure?
fun <Type: TokenType, R> Parser<Type, R>.run(tokens: TokenStream<Type>, debug: Boolean = false): ParserResult<Type, R>
		= run(tokens, if (debug) { fun(s:() -> String) = println(s()) } else {{}})

private fun <Type: TokenType, R> Parser<Type, R>.run(tokens: TokenStream<Type>, writer: Writer = {}): ParserResult<Type, R>
		= this.applyRule(ParserState.of(tokens, writer))
