package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.VariableName

typealias TypeName = String

sealed class Type {
	data class FunctionType(val from: Type, val to: Type): Type() {
		override fun toString(): String = "$from -> $to"
	}
//	data class Base(val name: String) : Type() {
//		override fun toString(): String = name
//	}
	data class RecordType(val types: Map<VariableName, Type>): Type() {
		override fun toString(): String = "{${types.entries.joinToString()}}"
	}
	object Nat : Type() {
		override fun toString(): String = "Nat"
	}
	object Bool : Type() {
		override fun toString(): String = "Bool"
	}
	object Unit : Type() {
		override fun toString(): String = "Unit"
	}

	data class UserType(val name: TypeName) : Type() {
		override fun toString(): String = name
	}
}
