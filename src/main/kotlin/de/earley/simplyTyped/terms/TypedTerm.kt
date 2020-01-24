package de.earley.simplyTyped.terms

import de.earley.parser.combinators.isAMatch
import de.earley.parser.combinators.map
import de.earley.parser.context
import de.earley.simplyTyped.eval
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken
import de.earley.simplyTyped.terms.TypedTerm.*
import de.earley.simplyTyped.types.Type
import java.util.concurrent.locks.Condition
import kotlin.math.exp

typealias VariableName = String

sealed class TypedTerm {
	data class Variable(val name: VariableName): TypedTerm() {
		override fun toString(): String = name
	}
	data class Abstraction(val binder: VariableName, val argType: Type, val body: TypedTerm): TypedTerm() {
		override fun toString(): String = "(λ$binder : $argType . $body)"
	}
	data class App(val left: TypedTerm, val right: TypedTerm): TypedTerm() {
		override fun toString(): String = "($left $right)"
	}
	data class KeywordTerm(val keyword: Keyword) : TypedTerm() {
		override fun toString(): String = keyword.toString()
	}
	data class LetBinding(val binder: VariableName, val bound: TypedTerm, val expression: TypedTerm) : TypedTerm() {
		override fun toString(): String = "let $binder = $bound in $expression"
	}
	data class Record(val contents: Map<VariableName, TypedTerm>): TypedTerm() {
		override fun toString(): String = "{${contents.entries.joinToString { (k, v) -> "$k = $v" }}}"
	}
	data class RecordProjection(val record: TypedTerm, val project: VariableName): TypedTerm() {
		override fun toString(): String = "${record}.$project"
	}
	data class IfThenElse(val condition: TypedTerm, val then: TypedTerm, val `else`: TypedTerm): TypedTerm() {
		override fun toString(): String = "if $condition then $then else $`else`"
	}
	data class Fix(val func: TypedTerm): TypedTerm() {
		override fun toString(): String = "fix $func"
	}
	object Unit : TypedTerm() {
		override fun toString(): String = "unit"
	}
	data class TypeDef(val name: VariableName, val type: Type, val body: TypedTerm): TypedTerm() {
		override fun toString(): String = "type $name = $type in $body"
	}
	data class Variant(val slot: String, val term: TypedTerm, val type: Type): TypedTerm() {
		override fun toString(): String = "<$slot = $term> as $type"
	}
	data class Case(val on: TypedTerm, val cases: List<CasePattern>): TypedTerm() {
		override fun toString(): String = "case $on of ${cases.joinToString()}"
	}
	data class Assign(val variable: TypedTerm, val term: TypedTerm): TypedTerm() {
		override fun toString(): String = "$variable := $term"
	}
	data class Read(val variable: TypedTerm): TypedTerm() {
		override fun toString(): String = "!$variable"
	}
	data class Ref(val term: TypedTerm): TypedTerm() {
		override fun toString(): String = "ref $term"
	}
}

//fix (λx : T.t1)
fun fix(x: VariableName, type: Type, expression: TypedTerm): TypedTerm =
	Fix(Abstraction(x, type, expression))

fun numberTerm(n: Int): TypedTerm =
	if (n == 0) KeywordTerm(Keyword.Arithmetic.Zero)
	else App(KeywordTerm(Keyword.Arithmetic.Succ), numberTerm(n - 1))

typealias Bindings = Map<String, Int>
fun Bindings.inc(): Bindings = this.mapValues { (_, v) -> v + 1 }
operator fun Bindings.plus(variable: VariableName) = inc() + (variable to 0)

fun TypedTerm.toNameless(
	bindings: Bindings
): TypedNamelessTerm = when (this) {
	is Variable -> TypedNamelessTerm.Variable(
		bindings[name] ?: error("free variable $name in $this with bindings: $bindings")
	)
	is Abstraction -> TypedNamelessTerm.Abstraction(
		this.argType,
		body.toNameless(bindings + binder)
	)
	is App -> TypedNamelessTerm.App(
		left.toNameless(
			bindings
		), right.toNameless(bindings)
	)
	is KeywordTerm -> TypedNamelessTerm.KeywordTerm(keyword)
	is LetBinding -> TypedNamelessTerm.LetBinding(
		bound.toNameless(
			bindings
		), expression.toNameless(bindings + binder)
	)
	is Record -> TypedNamelessTerm.Record(contents.mapValues { it.value.toNameless(bindings) })
	is RecordProjection -> TypedNamelessTerm.RecordProjection(
		record.toNameless(bindings),
		project
	)
	is IfThenElse -> TypedNamelessTerm.IfThenElse(
		condition.toNameless(bindings),
		then.toNameless(bindings),
		`else`.toNameless(bindings)
	)
	is Fix -> TypedNamelessTerm.Fix(func.toNameless(bindings))
	is TypedTerm.Unit -> TypedNamelessTerm.Unit
	is TypeDef -> TypedNamelessTerm.TypeDef(name, type, body.toNameless(bindings))
	is Variant -> TypedNamelessTerm.Variant(slot, term.toNameless(bindings), type)
	is Case -> TypedNamelessTerm.Case(on.toNameless(bindings), cases.map {
		NamelessCasePattern(it.slot, it.term.toNameless(bindings + it.variableName))
	})
	is Assign -> TypedNamelessTerm.Assign(variable.toNameless(bindings), term.toNameless(bindings))
	is Read -> TypedNamelessTerm.Read(variable.toNameless(bindings))
	is Ref -> TypedNamelessTerm.Ref(term.toNameless(bindings))
}

fun TypedTerm.freeVariables(): Set<Variable> = when (this) {
	is Variable -> setOf(this)
	is Abstraction -> this.body.freeVariables() - Variable(this.binder)
	is App -> this.left.freeVariables() + this.right.freeVariables()
	is KeywordTerm -> emptySet()
	is LetBinding -> this.bound.freeVariables() + (this.expression.freeVariables() - Variable(this.binder))
	is Record -> contents.flatMap { it.value.freeVariables() }.toSet()
	is RecordProjection -> record.freeVariables()
	is IfThenElse -> condition.freeVariables() + then.freeVariables() + `else`.freeVariables()
	is Fix -> func.freeVariables()
	is TypedTerm.Unit -> emptySet()
	is TypeDef -> body.freeVariables()
	is Variant -> term.freeVariables()
	is Case -> on.freeVariables() + cases.flatMap { it.freeVariables() }
	is Assign -> variable.freeVariables() + term.freeVariables()
	is Read -> variable.freeVariables()
	is Ref -> term.freeVariables()
}