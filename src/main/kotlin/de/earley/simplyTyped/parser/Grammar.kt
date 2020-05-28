package de.earley.simplyTyped.parser

import de.earley.newParser.named
import de.earley.newParser.plus
import de.earley.newParser.token
import de.earley.newParser.void
import de.earley.parser.Parser
import de.earley.parser.TokenStream
import de.earley.parser.combinators.isA
import de.earley.parser.combinators.plus
import de.earley.parser.combinators.void
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.EOF
import de.earley.simplyTyped.terms.TypedTerm


typealias P<R> = Parser<SimplyTypedLambdaToken, R>

object SimplyTypedGrammar {

	val grammar = context("root") {
		(TermGrammar.term + isA(EOF).void()) //.map { term, _ -> term } // we have no backtracking, so if a term is found then no other possible terms are checked
	}

	val newGrammar = named("root") {
		TermGrammar.newTerm + token(EOF).void()
	}
}