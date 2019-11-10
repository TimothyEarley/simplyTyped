package lib

import java.awt.Choice


private typealias ParseFunction<Type, R> = ParserState<Type>.() -> ParserResult<Type, R>

interface Parser<Type : TokenType, out R> {
	val name: String
	fun apply(state: ParserState<Type>): ParserResult<Type, R>
}

class EagerParser<Type : TokenType, out R>(
	private val eval: ParseFunction<Type, R>,
	override val name: String
) : Parser<Type, R> {
	override fun apply(state: ParserState<Type>): ParserResult<Type, R> = applyRule(state, eval)
}

class LazyParser<Type : TokenType, out R>(
	eval: Lazy<ParseFunction<Type, R>>,
	override val name: String
) : Parser<Type, R> {
	private val eval by eval
	override fun apply(state: ParserState<Type>): ParserResult<Type, R> = applyRule(state, eval)
}

private fun <Type: TokenType, R> Parser<Type, R>.applyRule(
	state: ParserState<Type>,
	eval: ParseFunction<Type, R>
): ParserResult<Type, R> {
	val memo = state.memo
	val head = state.head!!
	val updatedState = state.addToChain(this)
	val excludedNames = listOf("map", "flatMap", "map pair")
	if (this.name !in excludedNames)
		println(updatedState.chain.filterNot { it.name in excludedNames	}.joinToString(separator = " -> ", transform = { it.name }) + " at $head")

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
		if (cached is ParserResult.Error.LeftRecursion && isDifferentContext(updatedState.chain)) {
			execute()
		} else {
			//update chain from the cached value
			cached.flatMap { it.copy(next = it.next.setChain(updatedState.chain)) }
		}
	}
}

fun <Type : TokenType> isDifferentContext(chain: List<Parser<Type, *>>): Boolean {
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



fun <Type: TokenType, R> Parser<Type, R>.run(tokens: TokenStream<Type>): ParserResult<Type, R>
		= this.apply(ParserState.of(tokens))
