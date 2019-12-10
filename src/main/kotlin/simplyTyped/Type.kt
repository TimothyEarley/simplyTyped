package simplyTyped

sealed class Type {
	data class FunctionType(val from: Type, val to: Type): Type() {
		override fun toString(): String = "$from -> $to"
	}
	data class Base(val name: String) : Type() {
		override fun toString(): String = name
	}
}

val Nat = Type.Base("Nat")
val Bool = Type.Base("Bool")