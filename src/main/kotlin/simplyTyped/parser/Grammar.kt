package simplyTyped.parser

import lib.Parser
import lib.combinators.*
import lib.context
import simplyTyped.parser.SimplyTypedLambdaToken.*

typealias P<R> = Parser<SimplyTypedLambdaToken, R>

object SimplyTypedGrammar {

	val grammar = context("root") {
		(TermGrammar.term + isA(EOF).void()) //.map { term, _ -> term } // we have no backtracking, so if a term is found then no other possible terms are checked
	}
}