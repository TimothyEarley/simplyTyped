package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.Keyword
import de.earley.simplyTyped.terms.TypedTerm

object RefsGrammar {

	private val ref : P<TypedTerm> = context("ref") {
		isA(Ref).void() + TermGrammar.safeTerm
	}.map(TypedTerm::Ref)

	private val assign : P<TypedTerm.Assign> = context("assign") {
		TermGrammar.safeTerm + isA(Colon).void() + isA(Equals).void() + TermGrammar.term
	}.map(TypedTerm::Assign)

	private val read: P<TypedTerm.Read> = context("read") {
		isA(Exclamation).void() + TermGrammar.safeTerm
	}.map(TypedTerm::Read)

	val refs: P<TypedTerm> = assign or read or ref

}