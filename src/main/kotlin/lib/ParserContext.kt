package lib

/**
 * Gives parsers a context name for debugging and error reporting
 */
inline class ParserContext(val name: String)

fun <Type : TokenType, R, P : Parser<Type, R>> context(name: String, block: ParserContext.() -> P): Parser<Type, R> {
	val parser by kotlin.lazy {
		ParserContext(name).run(block)
	}
	return LazyParser(
		kotlin.lazy { parser::apply },
		{ parser.backtrack() },
		{name}
	)
}