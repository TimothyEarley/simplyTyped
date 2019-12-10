package simplyTyped

sealed class TypedNamelessTerm {
	data class Variable(val number: Int): TypedNamelessTerm() {
		override fun toString(): String = "v$number"
	}
	data class Abstraction(val argType: Type, val body: TypedNamelessTerm): TypedNamelessTerm(){
		override fun toString(): String = "(λ:$argType.$body)"
	}
	data class App(val left: TypedNamelessTerm, val right: TypedNamelessTerm): TypedNamelessTerm() {
		override fun toString(): String = "($left $right)"
	}
	data class KeywordTerm(val keyword: Keyword) : TypedNamelessTerm() {
		override fun toString(): String = keyword.toString()
	}
	data class LetBinding(val bound: TypedNamelessTerm, val expression: TypedNamelessTerm) : TypedNamelessTerm() {
		override fun toString(): String = "let v0 = $bound in $expression"
	}
}

fun TypedNamelessTerm.toUntyped(): UntypedNamelessTerm = when (this) {
	is TypedNamelessTerm.Variable -> UntypedNamelessTerm.Variable(number)
	is TypedNamelessTerm.Abstraction -> UntypedNamelessTerm.Abstraction(body.toUntyped())
	is TypedNamelessTerm.App -> UntypedNamelessTerm.App(left.toUntyped(), right.toUntyped())
	is TypedNamelessTerm.KeywordTerm -> UntypedNamelessTerm.KeywordTerm(keyword)
	is TypedNamelessTerm.LetBinding -> UntypedNamelessTerm.LetBinding(bound.toUntyped(), expression.toUntyped())
}