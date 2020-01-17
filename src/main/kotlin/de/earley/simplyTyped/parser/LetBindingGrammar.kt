package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.Keyword
import de.earley.simplyTyped.terms.fix

object LetBindingGrammar {
	private val letBinding: P<TypedTerm.LetBinding> = context("let binding") {
		(
				isA(Let).void() +
				isA(Identifier).string +
				isA(Equals).void() +
				TermGrammar.term +
				isA(In).void() +
				TermGrammar.term
		).map(TypedTerm::LetBinding)
	}

	private val letrecBinding: P<TypedTerm.LetBinding> = context("letrec binding") {
		(
				isA(LetRec).void() +
				isA(Identifier).string +
				isA(Colon).void() +
				TypeGrammar.type +
				isA(Equals).void() +
				TermGrammar.term +
				isA(In).void() +
				TermGrammar.term
		).map { x, type, bound, expression ->
			// syntax desugaring
			// letrec x : T = t1 in t2 => let x = fix (λx : T.t1) in t2
			TypedTerm.LetBinding(x, fix(x, type, bound), expression)

		}
	}

	val binding = letBinding or letrecBinding
}