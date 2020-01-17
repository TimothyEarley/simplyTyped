package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*

object LetBindingGrammar {
	val letBinding: P<TypedTerm.LetBinding> = context("let binding") {
		(isAMatch(Identifier, "let").void() +
				isA(Identifier).string +
				isA(Equals).void() +
				TermGrammar.safeTerm +
				isAMatch(Identifier, "in").void() +
				TermGrammar.term
		).map(TypedTerm::LetBinding)
	}
}