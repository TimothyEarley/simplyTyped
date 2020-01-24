package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.types.Type

object TypeDefGrammar {

	private val recTypeDef: P<TypedTerm> = context("rec type def") {
		isA(RecTypeDef).void() +
		isA(Identifier).string +
		isA(Equals).void() +
		TypeGrammar.type +
		isA(In).void() +
		TermGrammar.term
	}.map { name, type, body -> TypedTerm.TypeDef(name, Type.RecursiveType(name, type), body) }

	private val simpleTypeDef: P<TypedTerm> = context("type def") {
		isA(TypeDef).void() +
		isA(Identifier).string +
		isA(Equals).void() +
		TypeGrammar.type +
		isA(In).void() +
		TermGrammar.term
	}.map(TypedTerm::TypeDef)

	val typeDef: P<TypedTerm> = simpleTypeDef or recTypeDef

}

