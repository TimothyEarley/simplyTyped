package de.earley.parser

data class SourcePosition(
	val line: Int,
	val col: Int
) : Comparable<SourcePosition> {

	override fun toString() = "Line: $line, Col: $col"

	companion object {
		val Synth = SourcePosition(Int.MAX_VALUE, Int.MAX_VALUE)
	}

	override fun compareTo(other: SourcePosition): Int {
	return 	if (this.line < other.line) -1 else this.col - other.col
	}

}

interface Locatable {
	val src: SourcePosition
}

fun Token<*>.src() = SourcePosition(line, col)