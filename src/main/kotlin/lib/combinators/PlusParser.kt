package lib.combinators

import lib.*

class PlusParser<Type : TokenType, A, B>(
	val first: Parser<Type, A>,
	val second: Parser<Type, B>
) : Parser<Type, Pair<A, B>> {
	override val name: String = "${first.name}+${second.name}"

	override fun eval(state: ParserState<Type>): ParserResult<Type, Pair<A, B>> {
		return first.applyRule(state).flatMap { a ->
			second.applyRule(a.next).map { b -> a.result to b }
		}
	}

	override fun backtrack(): Parser<Type, Pair<A, B>>? {
		val backtrackedFirst = first.backtrack()
		val backtrackedSecond = second.backtrack()
		return when (backtrackedFirst) {
			null -> when (backtrackedSecond) {
				null -> null
				else -> (first + backtrackedSecond)
			}
			else -> when (backtrackedSecond) {
				null -> (backtrackedFirst + second)
				else -> (backtrackedFirst + second) or (first + backtrackedSecond)
			}
		}
	}
}

operator fun <Type : TokenType, A, B> Parser<Type, A>.plus(
	next: Parser<Type, B>
): Parser<Type, Pair<A, B>> = PlusParser(this, next)

@JvmName("voidPlus")
operator fun <Type : TokenType, B> Parser<Type, VOID>.plus(
	next: Parser<Type, B>
): Parser<Type, B> = PlusParser(this, next).map { _, b -> b }

@JvmName("plusVoid")
operator fun <Type : TokenType, A> Parser<Type, A>.plus(
	next: Parser<Type, VOID>
): Parser<Type, A> = PlusParser(this, next).map { a, _ -> a }

@JvmName("voidPlusVoid")
operator fun <Type : TokenType> Parser<Type, VOID>.plus(
	next: Parser<Type, VOID>
): Parser<Type, VOID> = PlusParser(this, next).map { _, _ -> VOID }
