package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.TypedNamelessTerm

fun constraintType(term: TypedNamelessTerm): TypedNamelessTerm {
	val result = term.createConstraints(emptyMap()).recover {
		error(it.msg + " at " + it.element.src)
	}.type

	val type = result.unify().also {
		println(it)
	}

	//TODO check if any free type variables left

	return term
}