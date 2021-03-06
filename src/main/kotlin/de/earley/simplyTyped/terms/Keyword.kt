package de.earley.simplyTyped.terms

//TODO enum?
sealed class Keyword(val name: String, val isValue: Boolean) {

	override fun toString(): String = name

	sealed class Arithmetic(name: String, isValue: Boolean) : Keyword(name, isValue) {
		object Succ : Arithmetic("succ", true)
		object Pred : Arithmetic("pred", false)
		object IsZero : Arithmetic("iszero", false)
		object Zero : Arithmetic("0", true)
	}

	sealed class Bools(name: String) : Keyword(name, true) {
		object True : Bools("true")
		object False : Bools("false")
	}
}