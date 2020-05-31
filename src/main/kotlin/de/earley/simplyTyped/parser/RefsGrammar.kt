package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.TypedTerm

object RefsGrammar {

	private val ref : P<TypedTerm> = context("ref") {
		isA(Ref).src + TermGrammar.safeTerm
	}.map { src, term -> TypedTerm.Ref(term, src) }

	private val assign : P<TypedTerm.Assign> = context("assign") {
		TermGrammar.safeTerm + isA(Colon).src + isA(Equals).void() + TermGrammar.term
	}.map { variable, src, term  -> TypedTerm.Assign(variable, term, src) }

	private val read: P<TypedTerm.Read> = context("read") {
		isA(Exclamation).src + TermGrammar.safeTerm
	}.map { src, variable -> TypedTerm.Read(variable, src) }

	val refs: P<TypedTerm> = assign or read or ref

}