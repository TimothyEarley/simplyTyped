package de.earley.simplyTyped.parser

import de.earley.newParser.*
import de.earley.parser.Token
import de.earley.parser.combinators.*
import de.earley.parser.combinators.void
import de.earley.parser.context
import de.earley.parser.src
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*


object TermGrammar {

	private val variable: P<TypedTerm.Variable> =
		context("var") {
			isA(Identifier).map { it.value to it.src() }.map(TypedTerm::Variable)
		}

	private val abstraction: P<TypedTerm.Abstraction> =
		context("abs") {
			(
					isA(Lambda).src +
							isA(Identifier).string +
							isA(Colon).void() +
							TypeGrammar.type +
							isA(Dot).void() +
							term
					).map { src, binder, argType, body -> TypedTerm.Abstraction(binder, argType, body, src) }
		}
	private val app: P<TypedTerm.App> = context("app") {
		(safeTerm + safeTerm).map { left, right -> TypedTerm.App(left, right, left.src) }
	}

	private val parenTerm: P<TypedTerm> = context("paren") {
		isA(OpenParen).void() + term + isA(ClosedParen).void()
	}

	/**
	 * A safe term has a well defined end and pattern
	 */
	val safeTerm : P<TypedTerm> = context("safe term") {
		parenTerm or
		UnitParser.unit or
		LetBindingGrammar.binding or
		TypeDefGrammar.typeDef or
		VariantGrammar.variants or
		RecordGrammar.record or
		ArithmeticGrammar.arithmeticExpression or
		RefsGrammar.refs or //TODO refs with sequence needs brackets. This is not nice
		variable
	}


	val term: P<TypedTerm> = context("term") {
		app or
		SequenceGrammar.sequence or
		RecordGrammar.projection or
		safeTerm or
		abstraction
	}

	val newTerm : TermParser = recursive { term ->
		val app = named("app") { (term + term).leftAssoc().map { a, b -> TypedTerm.App(a, b, a.src) } }
		val paren = named("paren") { token(OpenParen).void() + term + token(ClosedParen).void() }
		val lambda = named("lambda") {
			(
					token(Lambda).src() +
					token(Identifier).string() +
					token(Colon).void() +
					TypeGrammar.newType +
					token(Dot).void() +
					term
			).leftAssoc().map { src, x, type, body -> TypedTerm.Abstraction(x, type, body, src) }
		}
		val variable = named("variable") {
			token(Identifier).map { TypedTerm.Variable(it.value, it.src()) }
		}

		// important: order specifies precedence
		named("term") {
			paren or
			lambda or
			LetBindingGrammar.newBinding(term) or
			TypeDefGrammar.newTypeDef(term) or
			ArithmeticGrammar.newArithmeticExpression(term) or
			newVariantGrammar(term) or
			newRecordGrammar(term) or
			app or
			variable or
			UnitParser.newUnit
		}.leftAssoc()
	}

}

typealias TermParser = Parser<Token<SimplyTypedLambdaToken>, TypedTerm>