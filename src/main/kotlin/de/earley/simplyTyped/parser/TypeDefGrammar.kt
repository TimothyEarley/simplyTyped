package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.TypedTerm

object TypeDefGrammar {

	val typeDef: P<TypedTerm> = context("type def") {
		isA(TypeDef).void() +
		isA(Identifier).string +
		isA(Equals).void() +
		TypeGrammar.type +
		isA(In).void() +
		TermGrammar.term
	}.map(TypedTerm::TypeDef)

}