package de.earley.simplyTyped.terms

data class SourcePosition(
	val line: Int,
	val col: Int
)

data class TypedNamelessTermAt(
	val typedNamelessTerm: TypedNamelessTerm,
	val at: SourcePosition
)