package simplyTyped

import untyped.NamelessTerm
import untyped.Term

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

}

sealed class Type {
	object Nat : Type() {
		override fun toString(): String = "Nat"
	}
	data class FunctionType(val from: Type, val to: Type): Type() {
		override fun toString(): String = "$from -> $to"
	}
}

fun TypedTerm.toUntyped(): Term = when (this) {
	is TypedTerm.Variable -> Term.Variable(name)
	is TypedTerm.Abstraction -> Term.Abstraction(binder, body.toUntyped())
	is TypedTerm.App -> Term.App(left.toUntyped(), right.toUntyped())
}