package simplyTyped

import simplyTyped.TypedTerm.*
import untyped.eval

sealed class TypedTerm {
	data class Variable(val name: String): TypedTerm() {
		override fun toString(): String = name
	}
	data class Abstraction(val binder: String, val argType: Type, val body: TypedTerm): TypedTerm() {
		override fun toString(): String = "(λ$binder : $argType . $body)"
	}
	data class App(val left: TypedTerm, val right: TypedTerm): TypedTerm() {
		override fun toString(): String = "($left $right)"
	}
	data class KeywordTerm(val keyword: Keyword) : TypedTerm() {
		override fun toString(): String = keyword.toString()
	}
	data class LetBinding(val binder: String, val bound: TypedTerm, val expression: TypedTerm) : TypedTerm() {
		override fun toString(): String = "let $binder = $bound in $expression"
	}
}

typealias Bindings = Map<String, Int>
fun Bindings.inc(): Bindings = this.mapValues { (_, v) -> v + 1 }
fun TypedTerm.toNameless(
	bindings: Bindings = emptyMap()
): TypedNamelessTerm = when (this) {
	is Variable -> TypedNamelessTerm.Variable(bindings[name] ?: error("free variable"))
	is Abstraction -> TypedNamelessTerm.Abstraction(this.argType, body.toNameless(bindings.inc() + (binder to 0)))
	is App -> TypedNamelessTerm.App(left.toNameless(bindings), right.toNameless(bindings))
	is KeywordTerm -> TypedNamelessTerm.KeywordTerm(keyword)
	is LetBinding -> TypedNamelessTerm.LetBinding(bound.toNameless(bindings), expression.toNameless(bindings.inc() + (binder to 0)))
}

fun TypedTerm.freeVariables(): Set<Variable> = when (this) {
	is Variable -> setOf(this)
	is Abstraction -> this.body.freeVariables() - Variable(this.binder)
	is App -> this.left.freeVariables() + this.right.freeVariables()
	is KeywordTerm -> emptySet()
	is LetBinding -> this.bound.freeVariables() + (this.expression.freeVariables() - Variable(this.binder))
}

// use erasure
fun TypedTerm.eval() = toNameless().toUntyped().eval()