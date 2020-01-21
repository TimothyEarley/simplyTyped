package de.earley.simplyTyped.types

infix fun Type.isSubtype(bigger: Type): Boolean = when {
	this == bigger -> true
	bigger == Type.Top -> true
	this is Type.FunctionType && bigger is Type.FunctionType ->
		bigger.from isSubtype this.from && this.to isSubtype bigger.to
	this is Type.RecordType && bigger is Type.RecordType ->
		bigger.types.all { (slot, type) ->
			this.types.containsKey(slot) && this.types.getValue(slot) isSubtype type
		}
	else -> false
}