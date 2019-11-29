package lib.combinators

import lib.*

class RenamingParser<Type : TokenType, T>(
	override val name: String,
	inner: Parser<Type, T>
) : Parser<Type, T> by inner

fun <Type : TokenType, T> Parser<Type, T>.rename(name: String): Parser<Type, T> =
	RenamingParser(name, this)