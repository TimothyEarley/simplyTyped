package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.VariableName

sealed class Type {
	data class FunctionType(val from: Type, val to: Type): Type() {
		override fun toString(): String = "$from -> $to"
	}
	data class Base(val name: String) : Type() {
		override fun toString(): String = name
	}
	data class RecordType(val types: Map<VariableName, Type>): Type() {
		override fun toString(): String = "{${types.entries.joinToString()}}"
	}
}

val Nat = Type.Base("Nat")
val Bool = Type.Base("Bool")