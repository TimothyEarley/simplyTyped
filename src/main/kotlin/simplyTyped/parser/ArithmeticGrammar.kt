package simplyTyped.parser

import lib.combinators.isAMatch
import lib.combinators.map
import lib.combinators.or
import lib.combinators.plus
import lib.context
import simplyTyped.Keyword
import simplyTyped.Keyword.*
import simplyTyped.TypedTerm
import simplyTyped.parser.SimplyTypedLambdaToken.*

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


	val arithmeticExpression: P<TypedTerm> = keyword(Arithmetic.Zero) or functions

}