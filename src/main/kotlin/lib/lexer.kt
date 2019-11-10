package lib

interface TokenType {
	val regex: Regex
}
data class Token<Type:TokenType>(
	val line: Int,
	val col: Int,
	val value: String,
	val type: Type
)

private class Lexer(private val source: String) {

	private data class Position(val index: Int, val line: Int, val col: Int) {
		override fun toString(): String = "$line:$col"
	}
	private var position: Position = Position(0, 1, 1)
	private var lastMatch: String? = null
	private var lastMatchPosition: Position? = null

	val isEOF: Boolean get() = position.index > source.lastIndex

	fun matchRegex(regex: Regex): Boolean {
		val anchoredMatchRegex = Regex("\\A" + regex.pattern)
		val match = anchoredMatchRegex.find(source.substring(position.index)) ?: return false
		lastMatch = match.value
		lastMatchPosition = position
		skip(match.value.length)
		return true
	}

	fun <Type:TokenType> token(type: Type): Token<Type> = Token(
		line = lastMatchPosition!!.line,
		col = lastMatchPosition!!.col,
		value = lastMatch!!,
		type = type
	)

	fun <Type:TokenType> eof(eof: Type): Token<Type> = Token(
		line = position.line,
		col = position.col,
		value = "",
		type = eof
	)

	fun describePosition(): String = "${source.substring(position.index,
		(position.index + 20).coerceAtMost(source.lastIndex))} at $position"

	// unsafe
	private fun skip(length: Int) {
		var (index, line, col) = position
		repeat(length) {
			if (source[index] == '\n') {
				line++
				col = 1
			} else {
				col++
			}
			index++
		}
		position = Position(index, line, col)
	}

}

fun <Type:TokenType> lex(input: String, types: Array<Type>, eof: Type): TokenStream<Type> = Lexer(input).run {
	sequence {
		while (! isEOF ) {
			val type = types.find { matchRegex(it.regex) } ?: error("No token found for input ${describePosition()}")
			yield(token(type))
		}
		yield(eof(eof))
	}.toTokenStream()
}