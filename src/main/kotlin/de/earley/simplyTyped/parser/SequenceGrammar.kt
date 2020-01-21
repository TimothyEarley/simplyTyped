package de.earley.simplyTyped.parser

import de.earley.parser.combinators.isA
import de.earley.parser.combinators.map
import de.earley.parser.combinators.plus
import de.earley.parser.combinators.void
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.types.Type
import java.util.concurrent.atomic.AtomicInteger

object SequenceGrammar {

	private var counter = AtomicInteger(0)

	val sequence: P<TypedTerm> = context("sequence") {
		TermGrammar.safeTerm + isA(SemiColon).void() + TermGrammar.safeTerm
	}.map { first, second ->
		TypedTerm.App(
			left = TypedTerm.Abstraction(binder = "_${counter.getAndIncrement()}", argType = Type.Unit, body = second),
			right = first
		)
	}

}