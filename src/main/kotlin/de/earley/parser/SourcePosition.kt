package de.earley.parser

data class SourcePosition(
	val line: Int,
	val col: Int
) {

	override fun toString() = "Line: $line, Col: $col"

	companion object {
		val Synth = SourcePosition(Int.MAX_VALUE, Int.MAX_VALUE)
	}

}

interface Locatable {
	val src: SourcePosition
}

fun Token<*>.src() = SourcePosition(line, col)