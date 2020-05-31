package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.VariableName

typealias TypeName = String

sealed class Type {
	data class FunctionType(val from: Type, val to: Type): Type() {
		override fun toString(): String = "($from -> $to)"
	}

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
	object Top : Type() {
		override fun toString(): String = "Top"
	}

	data class Ref(val of: Type): Type() {
		override fun toString(): String = "Ref $of"
	}

	data class Variant(val variants: Map<TypeName, Type>): Type() {
		override fun toString(): String = "<${variants.entries.joinToString { (k, v) -> "$k = $v" }}>"
	}

	data class RecursiveType(val binder: TypeName, val body: Type): Type() {
		override fun toString(): String = "μ $binder . $body"
	}

	data class TypeVariable(val name: TypeName): Type() {
		override fun toString(): String = "v$name"
	}

	data class UserType(val name: TypeName) : Type() {
		override fun toString(): String = "U:$name"
	}
}
