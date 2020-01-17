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

	private val natType: P<Type.Base> = context("base type") {
		isA(Identifier).string.map(Type::Base)
	}

	val type: P<Type> = context("type") {
		parenType or functionType or natType
	}
}