package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.parser.src
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.Number
import de.earley.simplyTyped.terms.Keyword.Arithmetic
import de.earley.simplyTyped.terms.Keyword.Bools
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.terms.numberTerm

object ArithmeticGrammar {

	private val functions = context("arith function") {
		(
			(
				isA(Succ).map { Arithmetic.Succ to it.src() } or
				isA(Pred).map { Arithmetic.Pred to it.src() } or
				isA(IsZero).map { Arithmetic.IsZero to it.src() }
			).map(TypedTerm::KeywordTerm) +
			TermGrammar.safeTerm
		).map { left, right -> TypedTerm.App(left, right, left.src) }
	}

	private val ifThenElse: P<TypedTerm.IfThenElse> = context("if then else") {
		isA(If).src +
		TermGrammar.term +
		isA(Then).void() +
		TermGrammar.term +
		isA(Else).void() +
		TermGrammar.term
	}.map { src, condition, then, `else` -> TypedTerm.IfThenElse(condition, then, `else`, src) }

	private val boolean: P<TypedTerm> = context("boolean") {
		isA(True).map { Bools.True to it.src() } or isA(False).map { Bools.False to it.src() }
	}.map(TypedTerm::KeywordTerm)

	private val number = context("number") {
		isA(Number).map { n -> numberTerm(n.value.toInt(), n.src()) }
	}

	val arithmeticExpression: P<TypedTerm> = number or boolean or functions or ifThenElse

}