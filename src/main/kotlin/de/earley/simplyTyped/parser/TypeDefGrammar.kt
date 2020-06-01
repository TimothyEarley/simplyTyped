package de.earley.simplyTyped.parser

import de.earley.newParser.*
import de.earley.parser.Token
import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.types.Type

object TypeDefGrammar {

	private val recTypeDef: P<TypedTerm> = context("rec type def") {
		isA(RecTypeDef).src +
		isA(Identifier).string +
		isA(Equals).void() +
		TypeGrammar.type +
		isA(In).void() +
		TermGrammar.term
	}.map { src, name, type, body -> TypedTerm.TypeDef(name, Type.RecursiveType(name, type), body, src) }

	private val simpleTypeDef: P<TypedTerm> = context("type def") {
		isA(TypeDef).src +
		isA(Identifier).string +
		isA(Equals).void() +
		TypeGrammar.type +
		isA(In).void() +
		TermGrammar.term
	}.map { src, name, type, body -> TypedTerm.TypeDef(name, type, body, src) }

	val typeDef: P<TypedTerm> = simpleTypeDef or recTypeDef

	fun newTypeDef(term : TermParser) : TermParser = named("type def") {
		val simpleTypeDef = (token(TypeDef).src() +
				token(Identifier).string() +
				token(Equals).void() +
				TypeGrammar.newType +
				token(In).void() +
				term
		).leftAssoc().map { src, name, type, body -> TypedTerm.TypeDef(name, type, body, src) }

		simpleTypeDef

	}

}

