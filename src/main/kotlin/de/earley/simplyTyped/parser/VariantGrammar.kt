package de.earley.simplyTyped.parser

import de.earley.newParser.*
import de.earley.parser.combinators.*
import de.earley.parser.combinators.void
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.CasePattern
import de.earley.simplyTyped.terms.TypedTerm

object VariantGrammar {

	private val variant: P<TypedTerm> = context("variant") {
		isA(OpenAngle).src +
		isA(Identifier).string +
		isA(Equals).void() +
		TermGrammar.term +
		isA(ClosedAngle).void() +
		isAMatch(Identifier, "as").void() +
		TypeGrammar.type
	}.map { src, slot, term, type -> TypedTerm.Variant(slot, term, type, src) }

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
		isA(Case).src +
		TermGrammar.safeTerm + // safe term b.c. of is not a keyword
		isAMatch(Identifier, "of").void() +
		many(pattern, isA(Pipe))
	}.map { src, on, cases -> TypedTerm.Case(on, cases, src) }

	val variants = variant or case

}

fun newVariantGrammar(term : TermParser) : TermParser {
	val variant = named("variant") {
		token(OpenAngle).src() +
				token(Identifier).string() +
				token(Equals).void() +
				term +
				token(ClosedAngle).void() +
				token(Identifier).matches("as").void() +
				TypeGrammar.newType
	}.map { src, slot, t, type -> TypedTerm.Variant(slot, t, type, src) }

	val pattern = named("pattern") {
		token(OpenAngle).void() +
				token(Identifier).string() +
				token(Equals).void() +
				token(Identifier).string() +
				token(ClosedAngle).void() +
				token(Equals).void() +
				token(ClosedAngle).void() +
				term
	}.map(::CasePattern)

	val case = named("case of") {
		token(Case).src() +
				term +
				token(Identifier).matches("of").void() +
				many(pattern, token(Pipe))
	}.map { src, on, cases -> TypedTerm.Case(on, cases.toList(), src) }

	return variant or case
}