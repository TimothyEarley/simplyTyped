package lib.combinators

import lib.Parser
import lib.TokenType


//fun <Type : TokenType, R> repeatSep(
//	parser: Parser<Type, R>,
//	sep: Parser<Type, Any>
//): Parser<Type, List<R>> =
//	optional(parser + many(sep.void() + parser)).map { p ->
//		if (p == null) emptyList()
//		else {
//			val (head, tail) = p
//			listOf(head) + tail
//		}
//	}

