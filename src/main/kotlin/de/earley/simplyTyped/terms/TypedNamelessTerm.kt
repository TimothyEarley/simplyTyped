package de.earley.simplyTyped.terms

import de.earley.simplyTyped.types.Type

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
	//TODO still named
	data class Record(val contents: Map<VariableName, TypedNamelessTerm>) : TypedNamelessTerm() {
		override fun toString(): String = "{${contents.entries.joinToString { (k, v) -> "$k = $v" }}}"
	}
	data class RecordProjection(val  record: TypedNamelessTerm, val project: VariableName): TypedNamelessTerm() {
		override fun toString(): String = "${record}.$project"
	}
	data class IfThenElse(val condition: TypedNamelessTerm, val then: TypedNamelessTerm, val `else`: TypedNamelessTerm): TypedNamelessTerm() {
		override fun toString(): String = "if $condition then $then else $`else`"
	}
	data class Fix(val func: TypedNamelessTerm): TypedNamelessTerm() {
		override fun toString(): String = "fix $func"
	}
	object Unit : TypedNamelessTerm() {
		override fun toString(): String = "unit"
	}
	data class TypeDef(val name: VariableName, val type: Type, val body: TypedNamelessTerm): TypedNamelessTerm() {
		override fun toString(): String = "type $name = $type in"
	}
}

fun TypedNamelessTerm.toUntyped(): UntypedNamelessTerm = when (this) {
	is TypedNamelessTerm.Variable -> UntypedNamelessTerm.Variable(number)
	is TypedNamelessTerm.Abstraction -> UntypedNamelessTerm.Abstraction(body.toUntyped())
	is TypedNamelessTerm.App -> UntypedNamelessTerm.App(left.toUntyped(), right.toUntyped())
	is TypedNamelessTerm.KeywordTerm -> UntypedNamelessTerm.KeywordTerm(keyword)
	is TypedNamelessTerm.LetBinding -> UntypedNamelessTerm.LetBinding(bound.toUntyped(), expression.toUntyped())
	is TypedNamelessTerm.Record -> UntypedNamelessTerm.Record(contents.mapValues { it.value.toUntyped() })
	is TypedNamelessTerm.RecordProjection -> UntypedNamelessTerm.RecordProjection(record.toUntyped(), project)
	is TypedNamelessTerm.IfThenElse -> UntypedNamelessTerm.IfThenElse(condition.toUntyped(), then.toUntyped(), `else`.toUntyped())
	is TypedNamelessTerm.Fix -> UntypedNamelessTerm.Fix(func.toUntyped())
	is TypedNamelessTerm.Unit -> UntypedNamelessTerm.Unit
	is TypedNamelessTerm.TypeDef -> body.toUntyped() // remove type info
}