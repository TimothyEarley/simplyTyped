package de.earley.parser.combinators

import de.earley.parser.*

class MapParser<Type : TokenType, A, B>(
	private val a: Parser<Type, A>,
	private val f: (A) -> B
) : Parser<Type, B> {
	override val name: String = "m(${a.name})"
	override fun eval(state: ParserState<Type>): ParserResult<Type, B> = a.applyRule(state).map(f)
	override fun backtrack(): Parser<Type, B>? = a.backtrack()?.map(f)
}

fun <Type : TokenType, A, B> Parser<Type, A>.map(f: (A) -> B): Parser<Type, B>
		= MapParser(this, f)

fun <Type : TokenType, A, B, C> Parser<Type, Pair<A, B>>.map(f: (A, B) -> C): Parser<Type, C>
		= MapParser(this) { f(it.first, it.second) }

fun <Type : TokenType, A, B, C, D> Parser<Type, Pair<Pair<A, B>, C>>.map(f: (A, B, C) -> D): Parser<Type, D>
		= MapParser(this) { f(it.first.first, it.first.second, it.second) }

fun <Type : TokenType, A, B, C, D, E> Parser<Type, Pair<Pair<Pair<A, B>, C>, D>>.map(f: (A, B, C, D) -> E): Parser<Type, E>
		= MapParser(this) { f(it.first.first.first, it.first.first.second, it.first.second, it.second) }

fun <Type : TokenType, A, B, C, D, E, F> Parser<Type, Pair<Pair<Pair<Pair<A, B>, C>, D>, E>>.map(f: (A, B, C, D, E) -> F): Parser<Type, F>
		= MapParser(this) { f(it.first.first.first.first, it.first.first.first.second, it.first.first.second, it.first.second, it.second) }