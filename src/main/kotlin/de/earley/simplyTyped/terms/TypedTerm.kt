package de.earley.simplyTyped.terms

import de.earley.parser.Locatable
import de.earley.parser.SourcePosition
import de.earley.simplyTyped.terms.TypedTerm.*
import de.earley.simplyTyped.types.Type

typealias VariableName = String

sealed class TypedTerm : Locatable {
	data class Variable(val name: VariableName, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = name
	}
	data class Abstraction(val binder: VariableName, val argType: Type, val body: TypedTerm, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "(λ$binder : $argType . $body)"
	}
	data class App(val left: TypedTerm, val right: TypedTerm, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "($left $right)"
	}
	data class KeywordTerm(val keyword: Keyword, override val src: SourcePosition) : TypedTerm() {
		override fun toString(): String = keyword.toString()
	}
	data class LetBinding(val binder: VariableName, val bound: TypedTerm, val expression: TypedTerm, override val src: SourcePosition) : TypedTerm() {
		override fun toString(): String = "let $binder = $bound in $expression"
	}
	data class Record(val contents: Map<VariableName, TypedTerm>, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "{${contents.entries.joinToString { (k, v) -> "$k = $v" }}}"
	}
	data class RecordProjection(val record: TypedTerm, val project: VariableName, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "${record}.$project"
	}
	data class IfThenElse(val condition: TypedTerm, val then: TypedTerm, val `else`: TypedTerm, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "if $condition then $then else $`else`"
	}
	data class Fix(val func: TypedTerm, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "fix $func"
	}
	data class Unit(override val src: SourcePosition) : TypedTerm() {
		override fun toString(): String = "unit"
	}
	data class TypeDef(val name: VariableName, val type: Type, val body: TypedTerm, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "type $name = $type in $body"
	}
	data class Variant(val slot: String, val term: TypedTerm, val type: Type, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "<$slot = $term> as $type"
	}
	data class Case(val on: TypedTerm, val cases: List<CasePattern>, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "case $on of ${cases.joinToString()}"
	}
	data class Assign(val variable: TypedTerm, val term: TypedTerm, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "$variable := $term"
	}
	data class Read(val variable: TypedTerm, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "!$variable"
	}
	data class Ref(val term: TypedTerm, override val src: SourcePosition): TypedTerm() {
		override fun toString(): String = "ref $term"
	}
}

//fix (λx : T.t1)
fun fix(x: VariableName, type: Type, expression: TypedTerm): TypedTerm =
	Fix(Abstraction(x, type, expression, SourcePosition.Synth), SourcePosition.Synth)

fun numberTerm(n: Int, src: SourcePosition): TypedTerm =
	if (n == 0) KeywordTerm(Keyword.Arithmetic.Zero, src)
	else App(KeywordTerm(Keyword.Arithmetic.Succ, SourcePosition.Synth), numberTerm(n - 1, SourcePosition.Synth), src)

typealias Bindings = Map<String, Int>
fun Bindings.inc(): Bindings = this.mapValues { (_, v) -> v + 1 }
operator fun Bindings.plus(variable: VariableName) = inc() + (variable to 0)

fun TypedTerm.toNameless(
	bindings: Bindings
): TypedNamelessTerm = when (this) {
	is Variable -> TypedNamelessTerm.Variable(
		bindings[name] ?: error("free variable $name in $this with bindings: $bindings"),
		src
	)
	is Abstraction -> TypedNamelessTerm.Abstraction(
		this.argType,
		body.toNameless(bindings + binder),
		src
	)
	is App -> TypedNamelessTerm.App(
		left.toNameless(
			bindings
		),
		right.toNameless(bindings),
		src
	)
	is KeywordTerm -> TypedNamelessTerm.KeywordTerm(keyword, src)
	is LetBinding -> TypedNamelessTerm.LetBinding(
		bound.toNameless(
			bindings
		),
		expression.toNameless(bindings + binder),
		src
	)
	is Record -> TypedNamelessTerm.Record(contents.mapValues { it.value.toNameless(bindings) }, src)
	is RecordProjection -> TypedNamelessTerm.RecordProjection(
		record.toNameless(bindings),
		project,
		src
	)
	is IfThenElse -> TypedNamelessTerm.IfThenElse(
		condition.toNameless(bindings),
		then.toNameless(bindings),
		`else`.toNameless(bindings),
		src
	)
	is Fix -> TypedNamelessTerm.Fix(func.toNameless(bindings), src)
	is TypedTerm.Unit -> TypedNamelessTerm.Unit(src)
	is TypeDef -> TypedNamelessTerm.TypeDef(name, type, body.toNameless(bindings), src)
	is Variant -> TypedNamelessTerm.Variant(slot, term.toNameless(bindings), type, src)
	is Case -> TypedNamelessTerm.Case(on.toNameless(bindings), cases.map {
		NamelessCasePattern(it.slot, it.term.toNameless(bindings + it.variableName))
	}, src)
	is Assign -> TypedNamelessTerm.Assign(variable.toNameless(bindings), term.toNameless(bindings), src)
	is Read -> TypedNamelessTerm.Read(variable.toNameless(bindings), src)
	is Ref -> TypedNamelessTerm.Ref(term.toNameless(bindings), src)
}

fun TypedTerm.freeVariables(): Set<Variable> = when (this) {
	is Variable -> setOf(this)
	is Abstraction -> this.body.freeVariables().filter { it.name != this.binder }.toSet()
	is App -> this.left.freeVariables() + this.right.freeVariables()
	is KeywordTerm -> emptySet()
	is LetBinding -> this.bound.freeVariables() + (this.expression.freeVariables().filter { it.name != this.binder }.toSet())
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

fun TypedTerm.tree(indent : String = "") : String = "\n" + indent + when (this) {
	is Variable -> this.name
	is Abstraction -> "λ $binder : $argType ." + body.tree("$indent|   ")
	is App -> "()" + left.tree("$indent|   ") + right.tree("$indent|   ")
	is KeywordTerm -> TODO()
	is LetBinding -> "let $binder" + bound.tree("$indent|   ") + expression.tree("$indent|   ")
	is Record -> TODO()
	is RecordProjection -> TODO()
	is IfThenElse -> TODO()
	is Fix -> TODO()
	is TypedTerm.Unit -> "Unit"
	is TypeDef -> TODO()
	is Variant -> TODO()
	is Case -> TODO()
	is Assign -> TODO()
	is Read -> TODO()
	is Ref -> TODO()
}