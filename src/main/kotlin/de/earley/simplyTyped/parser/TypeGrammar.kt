package de.earley.simplyTyped.parser

import de.earley.parser.combinators.*
import de.earley.parser.context
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.types.Type


object TypeGrammar {
	private val parenType: P<Type> = context("paren type") {
		isA(OpenParen).void() + type + isA(ClosedParen).void()
	}

	private val functionType: P<Type.FunctionType> =
		context("function type") {
			(type + isA(Arrow).void() + type).map(
				Type::FunctionType
			)
		}

	private val natType: P<Type.Nat> = context("nat type") {
		isAMatch(Identifier, "Nat").map { Type.Nat }
	}

	private val boolType: P<Type.Bool> = context("nat type") {
		isAMatch(Identifier, "Bool").map { Type.Bool }
	}

	private val recordType: P<Type.RecordType> = context("record type") {
		isA(OpenBracket).void() +
		many(isA(Identifier).string + isA(Colon).void() + type, isA(Comma)) +
		isA(ClosedBracket).void()
	}.map { types -> Type.RecordType(types.toMap()) }

	private val userType: P<Type.UserType> = context("user type") {
		isA(Identifier).string
	}.map(Type::UserType)

	val type: P<Type> = context("type") {
		parenType or functionType or natType or boolType or recordType or userType
	}
}