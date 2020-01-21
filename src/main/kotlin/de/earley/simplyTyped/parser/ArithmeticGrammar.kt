package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.Number
import de.earley.simplyTyped.terms.Keyword.Arithmetic
import de.earley.simplyTyped.terms.Keyword.Bools
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.terms.numberTerm

object ArithmeticGrammar {

	private val functions = context("arith function") {
		((
			isA(Succ).map { Arithmetic.Succ } or
			isA(Pred).map { Arithmetic.Pred } or
			isA(IsZero).map { Arithmetic.IsZero }
		).map(TypedTerm::KeywordTerm) + TermGrammar.safeTerm ).map(TypedTerm::App)
	}

	private val ifThenElse: P<TypedTerm.IfThenElse> = context("if then else") {
		isA(If).void() +
		TermGrammar.term +
		isA(Then).void() +
		TermGrammar.term +
		isA(Else).void() +
		TermGrammar.term
	}.map(TypedTerm::IfThenElse)

	private val boolean: P<TypedTerm> = context("boolean") {
		isA(True).map { Bools.True } or isA(False).map { Bools.False }
	}.map(TypedTerm::KeywordTerm)

	private val number = context("number") {
		isA(Number).map { n -> numberTerm(n.value.toInt()) }
	}

	val arithmeticExpression: P<TypedTerm> = number or boolean or functions or ifThenElse

}