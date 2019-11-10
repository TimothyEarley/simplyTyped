package lib

/**
 * Gives parsers a context name for debugging
 */
inline class ParserContext(val name: String)

fun <Type : TokenType, T : Token<Type>, R> context(name: String, block: ParserContext.() -> Parser<Type, R>): Parser<Type, R> = lazy {
	ParserContext(name).run(block).value
}