package de.earley.parser.combinators

import de.earley.parser.*


// special case handling for unwanted tokens
object VOID

fun <Type : TokenType, R> Parser<Type, R>.void(): Parser<Type, VOID>
		= this.map { VOID }.rename("~${this.name}~")