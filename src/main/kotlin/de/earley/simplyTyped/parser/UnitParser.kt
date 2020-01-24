package de.earley.simplyTyped.parser

import de.earley.parser.combinators.isA
import de.earley.parser.combinators.map
import de.earley.parser.context
import de.earley.parser.src
import de.earley.simplyTyped.terms.TypedTerm

object UnitParser {

	val unit = context("unit") {
		isA(SimplyTypedLambdaToken.Unit).map { TypedTerm.Unit(it.src()) }
	}

}