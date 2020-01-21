package de.earley.simplyTyped.terms

data class CasePattern(
	val slot: String,
	val variableName: VariableName,
	val term: TypedTerm
)

fun CasePattern.freeVariables(): Set<TypedTerm.Variable> =
	term.freeVariables() - TypedTerm.Variable(variableName)

data class NamelessCasePattern(
	val slot: String,
	val term: TypedNamelessTerm
)


data class UntypedNamelessCasePattern(
	val slot: String,
	val term: UntypedNamelessTerm
) {
	override fun toString(): String = "<$slot = v0> => $term"
}