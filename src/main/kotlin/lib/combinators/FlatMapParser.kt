package lib.combinators

import lib.*

//TODO flatmap has broken backtracking
//
//class FlatMapParser<Type : TokenType, A, B>(
//	val a: Parser<Type, A>,
//	val f: (A) -> Parser<Type, B>,
//	val nextName: String
//) : Parser<Type, B> {
//	override val name: String = "${a.name}+$nextName"
//
//	override fun eval(state: ParserState<Type>): ParserResult<Type, B>
//		= a.applyRule(state).flatMap { f(it.result).applyRule(it.next) }
//	override fun backtrack(): Parser<Type, B>? {
//		//TODO leaving this feels wrong, but it works (together with rec. backtracking)
////		val backtrackSecond = this.flatMapN(name, { "[${nextName()}]'" }) { f(it).backtrack() }
////		or(backtrackFirst, backtrackSecond)
//		return a.backtrack()?.flatMap(nextName, f)
//	}
//}
//
//fun <Type : TokenType, A, B> Parser<Type, A>.flatMap(
//	nextName: String,
//	f: (A) -> Parser<Type, B>
//): Parser<Type, B> = FlatMapParser(this, f, nextName)