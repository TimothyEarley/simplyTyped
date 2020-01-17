package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.terms.VariableName
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*

object RecordGrammar {

	private val recordEntry: P<Pair<VariableName, TypedTerm>> = context("record entry") {
		isA(Identifier).string + isA(Equals).void() + TermGrammar.safeTerm
	}

	val projection: P<TypedTerm.RecordProjection> = context("projection") {
		TermGrammar.term + isA(Dot).void() + isA(Identifier).string
	}.map(TypedTerm::RecordProjection)


	val record: P<TypedTerm.Record> = context("record") {
		isA(OpenBracket).void() +
		many(recordEntry, delimiter = isA(type = Comma)) +
		isA(ClosedBracket).void()
	}.map { entries -> TypedTerm.Record(entries.toMap()) }

}