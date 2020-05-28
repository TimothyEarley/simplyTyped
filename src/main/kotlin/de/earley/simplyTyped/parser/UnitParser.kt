package de.earley.simplyTyped.parser

import de.earley.newParser.map
import de.earley.newParser.named
import de.earley.newParser.src
import de.earley.newParser.token
import de.earley.parser.combinators.isA
import de.earley.parser.combinators.map
import de.earley.parser.context
import de.earley.parser.src
import de.earley.simplyTyped.terms.TypedTerm

object UnitParser {

	val unit = context("unit") {
		isA(SimplyTypedLambdaToken.Unit).map { TypedTerm.Unit(it.src()) }
	}

	val newUnit = named("unit") {
		token(SimplyTypedLambdaToken.Unit).src().map { TypedTerm.Unit(it) }
	}

}