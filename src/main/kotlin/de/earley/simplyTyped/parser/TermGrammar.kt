package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.TypedTerm
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*


object TermGrammar {

	private val variable: P<TypedTerm.Variable> =
		context("var") {
			isA(Identifier).string.map(TypedTerm::Variable)
		}

	private val abstraction: P<TypedTerm.Abstraction> =
		context("abs") {
			(
					isA(Lambda).void() +
							isA(Identifier).string +
							isA(Colon).void() +
							TypeGrammar.type +
							isA(Dot).void() +
							term
					).map(TypedTerm::Abstraction)
		}
	private val app: P<TypedTerm.App> = context("app") {
		(safeTerm + safeTerm).map(TypedTerm::App)
	}

	private val parenTerm: P<TypedTerm> = context("paren") {
		isA(OpenParen).void() + term + isA(ClosedParen).void()
	}

	private val atomic: P<TypedTerm> = context("atomic") {
		ArithmeticGrammar.arithmeticExpression or variable
	}

	val safeTerm = parenTerm or
			LetBindingGrammar.letBinding or atomic

	val term: P<TypedTerm> = context("term") {
		app or
		safeTerm or
		abstraction
	}

}