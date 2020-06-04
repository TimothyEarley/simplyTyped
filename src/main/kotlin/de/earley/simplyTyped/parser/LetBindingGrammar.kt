package de.earley.simplyTyped.parser

import de.earley.newParser.*
import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.terms.fix

object LetBindingGrammar {
	private val letBinding: P<TypedTerm.LetBinding> = context("let binding") {
		(
				isA(Let).src +
				isA(Identifier).string +
				isA(Equals).void() +
				TermGrammar.term +
				isA(In).void() +
				TermGrammar.term
		).map { src, binder, bound, expression -> TypedTerm.LetBinding(binder, bound, expression, src)}
	}

	private val letrecBinding: P<TypedTerm.LetBinding> = context("letrec binding") {
		(
				isA(LetRec).src +
				isA(Identifier).string +
				isA(Colon).void() +
				TypeGrammar.type +
				isA(Equals).void() +
				TermGrammar.term +
				isA(In).void() +
				TermGrammar.term
		).map { src, x, type, bound, expression ->
			// syntax desugaring
			// letrec x : T = t1 in t2 => let x = fix (λx : T.t1) in t2
			TypedTerm.LetBinding(x, fix(x, type, bound), expression, src)

		}
	}

	val binding = letBinding or letrecBinding

	fun newBinding(term : TermParser) = named("binding") {
		val letRecBinding = named("rec") {(
				token(LetRec).src() +
				token(Identifier).string() +
				token(Colon).void() +
				TypeGrammar.newType +
				token(Equals).void() +
				term +
				token(In).void() +
				term
			).map { src, binder, type, bound, expression ->
			// syntax desugaring
			// letrec x : T = t1 in t2 => let x = fix (λx : T.t1) in t2
			TypedTerm.LetBinding(binder, fix(binder, type, bound), expression, src)
		}}

		val letBinding = (token(Let).src() +
				token(Identifier).string() +
				token(Equals).void() +
				term +
				token(In).void() +
				term
				).map { src, binder, bound, expression ->
					TypedTerm.LetBinding(binder, bound, expression, src)
				}

		letBinding or letRecBinding
	}
}