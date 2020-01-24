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
	data class Variant(val slot: String, val term: TypedNamelessTerm, val type: Type): TypedNamelessTerm() {
		override fun toString(): String = "<$slot = $term> as $type"
	}
	data class Case(val on: TypedNamelessTerm, val cases: List<NamelessCasePattern>): TypedNamelessTerm() {
		override fun toString(): String = "case $on of ${cases.joinToString()}"
	}
	data class Assign(val variable: TypedNamelessTerm, val term: TypedNamelessTerm): TypedNamelessTerm() {
		override fun toString(): String = "$variable := $term"
	}
	data class Read(val variable: TypedNamelessTerm): TypedNamelessTerm() {
		override fun toString(): String = "!$variable"
	}
	data class Ref(val term: TypedNamelessTerm): TypedNamelessTerm() {
		override fun toString(): String = "ref $term"
	}
	data class Fold(val type: Type, val term: TypedNamelessTerm): TypedNamelessTerm() {
		override fun toString(): String = "fold[$type] $term"
	}
	data class Unfold(val type: Type?, val term: TypedNamelessTerm): TypedNamelessTerm() {
		override fun toString(): String = "unfold[$type] $term"
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
	is TypedNamelessTerm.Variant -> UntypedNamelessTerm.Variant(slot, term.toUntyped())
	is TypedNamelessTerm.Case -> UntypedNamelessTerm.Case(on.toUntyped(), cases.map {
		UntypedNamelessCasePattern(it.slot, it.term.toUntyped())
	})
	is TypedNamelessTerm.Read -> UntypedNamelessTerm.Read(variable.toUntyped())
	is TypedNamelessTerm.Assign -> UntypedNamelessTerm.Assign(variable.toUntyped(), term.toUntyped())
	is TypedNamelessTerm.Ref -> UntypedNamelessTerm.Ref(term.toUntyped())
	is TypedNamelessTerm.Fold -> term.toUntyped() //TODO what would be the use of moving it to runtime?
	is TypedNamelessTerm.Unfold -> term.toUntyped()
}