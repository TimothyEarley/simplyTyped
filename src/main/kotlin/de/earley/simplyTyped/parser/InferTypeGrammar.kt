package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.types.NamePool

object InferTypeGrammar {

	val inferTypeAbs = context("infer abs") {
		isA(SimplyTypedLambdaToken.Lambda).src +
		isA(SimplyTypedLambdaToken.Identifier).string +
		isA(SimplyTypedLambdaToken.Dot).void() +
		TermGrammar.term
	}.map { src, v, body -> TypedTerm.Abstraction(v, NamePool.freeName(), body, src) }

	val inferVariant = context("infer variant") {
		isA(SimplyTypedLambdaToken.OpenAngle).src +
		isA(SimplyTypedLambdaToken.Identifier).string +
		isA(SimplyTypedLambdaToken.Equals).void() +
		TermGrammar.term +
		isA(SimplyTypedLambdaToken.ClosedAngle).void()
	}.map { src, slot, term ->
		TypedTerm.Variant(slot, term, NamePool.freeName(), src)
	}
}