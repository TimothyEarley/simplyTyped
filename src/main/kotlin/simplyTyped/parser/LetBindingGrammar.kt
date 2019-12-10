package simplyTyped.parser

import lib.combinators.*
import lib.context
import simplyTyped.TypedTerm
import simplyTyped.parser.SimplyTypedLambdaToken.*

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