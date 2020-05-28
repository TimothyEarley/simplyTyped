package de.earley.parser

// This is just because it cool to have lexing and parsing concurrently

interface TokenStream<T:TokenType> {
	val head: Token<T>?
	val tail: TokenStream<T>
}

private class IteratorTokenStream<T:TokenType> (
	private val iterator: Iterator<Token<T>>,
	private val index: Int,
	private val cache: MutableList<Token<T>>
) : TokenStream<T> {
	override val head: Token<T>? by lazy { get(index) }
	override val tail: TokenStream<T> by lazy {
		IteratorTokenStream(iterator, index + 1, cache)
	}

	private fun get(i: Int): Token<T>? =
		if (getUpTo(i)) cache[i] else null

	private fun getUpTo(i: Int): Boolean {
		while (cache.lastIndex < i) {
			if (!iterator.hasNext()) return false
			cache += iterator.next()
		}
		return true
	}

}

fun <T : TokenType> Sequence<Token<T>>.toTokenStream(): TokenStream<T> = IteratorTokenStream(
	this.iterator(),
	0,
	mutableListOf()
)

fun <T : TokenType> TokenStream<T>.toSequence(): Sequence<Token<T>> = sequence {
	var current : TokenStream<T> = this@toSequence
	while (current.head != null) {
		yield(current.head!!)
		current = current.tail
	}
}

fun <T : TokenType> TokenStream<T>.filter(p: (Token<T>) -> Boolean): TokenStream<T> = object : TokenStream<T> {
	private val headTail: Pair<Token<T>?, TokenStream<T>> by lazy {
		val originalHead = this@filter.head
		when {
			originalHead == null -> null to this@filter.tail
			p(originalHead) -> originalHead to this@filter.tail.filter(p)
			else -> {
				val ftail = this@filter.tail.filter(p)
				ftail.head to ftail.tail
			}
		}
	}

	override val head: Token<T>? by lazy { headTail.first	}
	override val tail: TokenStream<T> by lazy { headTail.second	}
}

// for debug
fun <T : TokenType> TokenStream<T>.toList(): List<Token<T>> = when (this.head){
	null -> emptyList()
	else -> listOf(this.head!!) + this.tail.toList()
}