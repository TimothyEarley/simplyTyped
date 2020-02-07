package de.earley.simplyTyped.types

/**
 * get a type T st. T <: A and T <: B.
 */
fun meetTypes(a: Type, b: Type): Type = when {
	//TODO recursive meet
	a is Type.RecordType && b is Type.RecordType ->
		Type.RecordType(a.types + b.types)
	a is Type.Variant && b is Type.Variant ->
		Type.Variant(a.variants + b.variants)
	else -> TODO() // bottom type
}

infix fun <K> Map<K, Type>.meet(other : Map<K, Type>): Map<K, Type> {
	val result = this.toMutableMap()
	other.entries.forEach { (k, t) ->
		result.merge(k, t, ::meetTypes)
	}
	return result
}