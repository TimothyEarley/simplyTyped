package de.earley.simplyTyped.parser

import de.earley.newParser.*
import de.earley.parser.Token
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

	private val boolType: P<Type.Bool> = context("bool type") {
		isAMatch(Identifier, "Bool").map { Type.Bool }
	}

	private val unitType: P<Type.Unit> = context("unit type") {
		isAMatch(Identifier, "Unit").map { Type.Unit }
	}

	private val topType: P<Type.Top> = context("top type") {
		isAMatch(Identifier, "Top").map { Type.Top }
	}

	private val recordType: P<Type.RecordType> = context("record type") {
		isA(OpenBracket).void() +
		many(isA(Identifier).string + isA(Colon).void() + type, isA(Comma)) +
		isA(ClosedBracket).void()
	}.map { types -> Type.RecordType(types.toMap()) }

	private val userType: P<Type.UserType> = context("user type") {
		isA(Identifier).string
	}.map(Type::UserType)

	private val variantType: P<Type> = context("variant type") {
		isA(OpenAngle).void() +
		many(isA(Identifier).string + isA(Equals).void() + type, isA(Comma)) +
		isA(ClosedAngle).void()
	}.map { variants ->
		Type.Variant(variants.toMap())
	}

	private val refType: P<Type.Ref> = context("ref type") {
		isAMatch(Identifier, "Ref").void() + type
	}.map(Type::Ref)

	val type: P<Type> = context("type") {
		parenType or
		refType or
		functionType or
		natType or
		boolType or
		unitType or
		topType or
		recordType or
		variantType or
		userType
	}

	val newType : Parser<Token<SimplyTypedLambdaToken>, Type> = recursive { type ->
		//TODO name? error messages need to be slightly improved to show the name instead of [Identifier]
		fun baseType(name : String, type : Type) = token(Identifier).matches(name).map { type }

		val parenType = named("paren type") {
			token(OpenParen).void() + type + token(ClosedParen).void()
		}
		val arrowType = named("arrow") {
			(type + token(Arrow).void() + type).rightAssoc().map(Type::FunctionType)
		}
		val userType = named("user type") {
			token(Identifier).string().map(Type::UserType)
		}

		 val recordType = named("record type") {
			token(OpenBracket).void() +
			many(token(Identifier).string() + token(Colon).void() + type, token(Comma)).leftAssoc() +
			token(ClosedBracket).void()
		}.map { types -> Type.RecordType(types.toMap()) }

		val variantType = named("variant type") {
			token(OpenAngle).void() +
					many(token(Identifier).string() + token(Equals).void() + type, token(Comma)) +
					token(ClosedAngle).void()
		}.map { variants ->
			Type.Variant(variants.toMap())
		}

		named("type") {
			parenType or
			baseType("Unit", Type.Unit) or
			baseType("Bool", Type.Bool) or
			baseType("Nat", Type.Nat) or
			arrowType or
			userType or
			recordType or
			variantType
		}.leftAssoc()
	}
}