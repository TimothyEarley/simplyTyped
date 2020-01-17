package de.earley.parser

import de.earley.parser.combinators.LazyParser
import de.earley.parser.combinators.rename

/**
 * Gives parsers a context name for debugging and error reporting
 */
inline class ParserContext(val name: String)

fun <Type : TokenType, R, P : Parser<Type, R>> context(
	name: String,
	block: ParserContext.() -> P
): Parser<Type, R> = LazyParser {
	ParserContext(name).run(block)
}.rename(name)