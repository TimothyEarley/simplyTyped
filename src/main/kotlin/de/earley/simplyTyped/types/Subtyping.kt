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
	this is Type.RecursiveType || bigger is Type.RecursiveType -> {
		when {
			this is Type.RecursiveType && bigger !is Type.RecursiveType -> this.unfold() isSubtype bigger
			this !is Type.RecursiveType && bigger is Type.RecursiveType -> this isSubtype bigger.unfold()
			this is Type.RecursiveType && bigger is Type.RecursiveType -> this.body isSubtype bigger.body
			else -> error("impossible")
		}
	}
	else -> false
}

infix fun Type.sameTypeAs(other: Type) = this == other || (this isSubtype other && other isSubtype this)
infix fun Type.notSameTypeAs(other: Type) = !(this sameTypeAs other)
