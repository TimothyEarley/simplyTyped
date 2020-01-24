package de.earley.simplyTyped.parser

import de.earley.parser.SourcePosition
import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.types.Type
import java.util.concurrent.atomic.AtomicInteger

object SequenceGrammar {

	private var counter = AtomicInteger(0)

	val sequence: P<TypedTerm> = context("sequence") {
		TermGrammar.safeTerm + isA(SemiColon).src + TermGrammar.safeTerm
	}.map { first, src, second ->
		TypedTerm.App(
			left = TypedTerm.Abstraction(binder = "_${counter.getAndIncrement()}", argType = Type.Unit, body = second, src = SourcePosition.Synth),
			right = first,
			src = src
		)
	}

}