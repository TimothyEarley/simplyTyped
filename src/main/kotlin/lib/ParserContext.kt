package lib

import lib.combinators.LazyParser
import lib.combinators.rename

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