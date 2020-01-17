package de.earley.untyped

import de.earley.simplyTyped.Keyword

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
}


//TODO add function to create a pure lambda calculus version