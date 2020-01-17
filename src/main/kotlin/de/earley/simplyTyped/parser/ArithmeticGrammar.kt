package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.terms.Keyword
import de.earley.simplyTyped.terms.Keyword.*
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.numberTerm

private fun keyword(type: Keyword): P<TypedTerm.KeywordTerm> = context(type.name) {
	isAMatch(Identifier, type.name).map { TypedTerm.KeywordTerm(type) }
}

//TODO if expressions
object ArithmeticGrammar {

	private val functions = (
			(
					keyword(Arithmetic.Succ) or keyword(Arithmetic.Pred) or keyword(Arithmetic.IsZero)
					) +
					TermGrammar.safeTerm ).map(TypedTerm::App) //TODO we could add something else here

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