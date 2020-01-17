package de.earley.simplyTyped.terms

sealed class UntypedNamelessTerm {
	data class Variable(val number: Int): UntypedNamelessTerm() {
		override fun toString(): String = "v$number"
	}
	data class Abstraction(val body: UntypedNamelessTerm): UntypedNamelessTerm(){
		override fun toString(): String = "(λ.$body)"
	}
	data class App(val left: UntypedNamelessTerm, val right: UntypedNamelessTerm): UntypedNamelessTerm() {
		override fun toString(): String = "($left $right)"
	}
	data class KeywordTerm(val keyword: Keyword) : UntypedNamelessTerm() {
		override fun toString(): String = keyword.toString()
	}
	data class LetBinding(val bound: UntypedNamelessTerm, val expression: UntypedNamelessTerm) : UntypedNamelessTerm() {
		override fun toString(): String = "let v0 = $bound in $expression"
	}
	//TODO still named
	data class Record(val contents: Map<VariableName, UntypedNamelessTerm>) : UntypedNamelessTerm() {
		override fun toString(): String = "{${contents.entries.joinToString { (k,v) -> "$k = $v" }}}"
	}
	data class RecordProjection(val  record: UntypedNamelessTerm, val project: VariableName): UntypedNamelessTerm() {
		override fun toString(): String = "${record}.$project"
	}
	data class IfThenElse(val condition: UntypedNamelessTerm, val then: UntypedNamelessTerm, val `else`: UntypedNamelessTerm): UntypedNamelessTerm() {
		override fun toString(): String = "if $condition then $then else $`else`"
	}
	data class Fix(val func: UntypedNamelessTerm): UntypedNamelessTerm() {
		override fun toString(): String = "fix $func"
	}
}


//TODO add function to create a pure lambda calculus version