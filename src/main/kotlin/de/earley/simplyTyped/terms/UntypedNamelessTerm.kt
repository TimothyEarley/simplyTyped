package de.earley.simplyTyped.terms

import de.earley.simplyTyped.types.Type

sealed class UntypedNamelessTerm {
	data class Variable(val number: Int): UntypedNamelessTerm() {
		override fun toString(): String = "v$number"
	}
	data class Abstraction(val body: UntypedNamelessTerm): UntypedNamelessTerm(){
		override fun toString(): String = "(λ.$body)"
	}
	data class App(val left: UntypedNamelessTerm, val right: UntypedNamelessTerm): UntypedNamelessTerm() {
		override fun toString(): String =
			if (left is KeywordTerm && left.keyword == Keyword.Arithmetic.Succ) {
				var current = right
				var sum = 1
				while (current is App && current.left is KeywordTerm && (current.left as KeywordTerm).keyword == Keyword.Arithmetic.Succ) {
					current = current.right
					sum++
				}
				if (current is KeywordTerm && current.keyword  == Keyword.Arithmetic.Zero) "$sum"
				else "$sum+$current"
			}
			else "($left $right)"
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
	object Unit : UntypedNamelessTerm() {
		override fun toString(): String = "unit"
	}
	data class Variant(val slot: String, val term: UntypedNamelessTerm): UntypedNamelessTerm() {
		override fun toString(): String = "<$slot = $term>"
	}
	data class Case(val on: UntypedNamelessTerm, val cases: List<UntypedNamelessCasePattern>): UntypedNamelessTerm() {
		override fun toString(): String = "case $on of ${cases.joinToString(separator = "|")}"
	}
	data class Assign(val variable: UntypedNamelessTerm, val term: UntypedNamelessTerm): UntypedNamelessTerm() {
		override fun toString(): String = "$variable := $term"
	}
	data class Read(val variable: UntypedNamelessTerm): UntypedNamelessTerm() {
		override fun toString(): String = "!$variable"
	}
	data class Ref(val term: UntypedNamelessTerm): UntypedNamelessTerm() {
		override fun toString(): String = "ref $term"
	}
	class Label(val index: Int): UntypedNamelessTerm() {
		override fun toString(): String = "l$index"
	}
}

//TODO add function to create a pure lambda calculus version