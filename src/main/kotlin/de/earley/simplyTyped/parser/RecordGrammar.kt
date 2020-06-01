package de.earley.simplyTyped.parser

import de.earley.newParser.*
import de.earley.parser.Token
import de.earley.parser.combinators.*
import de.earley.parser.combinators.many
import de.earley.parser.context
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.terms.VariableName
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*

object RecordGrammar {

	private val recordEntry: P<Pair<VariableName, TypedTerm>> = context("record entry") {
		isA(Identifier).string + isA(Equals).void() + TermGrammar.safeTerm
	}

	val projection: P<TypedTerm.RecordProjection> = context("projection") {
		TermGrammar.term + isA(Dot).src + isA(Identifier).string
	}.map { record, src, project -> TypedTerm.RecordProjection(record, project, src) }


	val record: P<TypedTerm.Record> = context("record") {
		isA(OpenBracket).src +
		many(recordEntry, delimiter = isA(type = Comma)) +
		isA(ClosedBracket).void()
	}.map { src, entries -> TypedTerm.Record(entries.toMap(), src) }

}

fun newRecordGrammar(term : TermParser) : TermParser {
	val recordEntry = token(Identifier).string() + token(Equals).void() + term

	val record = named("record") {
		(token(OpenBracket).src() +
				many(recordEntry, token(Comma)) +
				token(ClosedBracket).void()).map { src, entries -> TypedTerm.Record(entries.toMap(), src) }
	}

	val projection = named("projection") {
		(term + token(Dot).src() + token(Identifier).string())
				.map { record, src, project -> TypedTerm.RecordProjection(record, project, src) }
	}

	return record or projection
}