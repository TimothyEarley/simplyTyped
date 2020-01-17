package de.earley.simplyTyped.parser

import de.earley.parser.combinators.isAMatch
import de.earley.parser.combinators.map
import de.earley.parser.combinators.or
import de.earley.parser.combinators.plus
import de.earley.parser.context
import de.earley.simplyTyped.Keyword
import de.earley.simplyTyped.Keyword.Arithmetic
import de.earley.simplyTyped.TypedTerm

private fun keyword(type: Keyword): P<TypedTerm.KeywordTerm> = context(type.name) {
	isAMatch(SimplyTypedLambdaToken.Identifier, type.name).map { TypedTerm.KeywordTerm(type) }
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