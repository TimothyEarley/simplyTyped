package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.CasePattern
import de.earley.simplyTyped.terms.TypedTerm

object VariantGrammar {

	private val variant: P<TypedTerm> = context("variant") {
		isA(OpenAngle).void() +
		isA(Identifier).string +
		isA(Equals).void() +
		TermGrammar.term +
		isA(ClosedAngle).void() +
		isAMatch(Identifier, "as").void() +
		TypeGrammar.type
	}.map(TypedTerm::Variant)

	private val pattern = context("pattern") {
		isA(OpenAngle).void() +
		isA(Identifier).string +
		isA(Equals).void() +
		isA(Identifier).string +
		isA(ClosedAngle).void() +
		isA(Equals).void() + isA(ClosedAngle).void() +
		TermGrammar.term
	}.map(::CasePattern)

	private val case: P<TypedTerm> = context("case of") {
		isA(Case).void() +
		TermGrammar.safeTerm + // safe term b.c. of is not a keyword
		isAMatch(Identifier, "of").void() +
		many(pattern, isA(Pipe))
	}.map(TypedTerm::Case)

	val variants = variant or case

}