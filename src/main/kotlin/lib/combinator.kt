package lib

fun <Type : TokenType, A, B> Parser<Type, A>.map(f: (A) -> B): Parser<Type, B> =
	EagerParser({ this@map.apply(this).map(f) }, "map")

fun <Type : TokenType, A, B, C> Parser<Type, Pair<A, B>>.map(f: (A, B) -> C): Parser<Type, C> =
	EagerParser({ this@map.apply(this).map { f(it.first, it.second) } }, "map pair")

fun <Type : TokenType, A, B> Parser<Type, A>.flatMap(f: (A) -> Parser<Type, B>): Parser<Type, B> =
	LazyParser(lazy {
		fun ParserState<Type>.(): ParserResult<Type, B> {
			return this@flatMap.apply(this).flatMap { f(it.result).apply(it.next) }
		}
	}, "flatMap")

fun <Type : TokenType> ParserContext.isA(type: Type): Parser<Type, Token<Type>> =
	EagerParser({ match(type) }, "isA $type")

val <Type : TokenType> Parser<Type, Token<Type>>.string get() = map { it.value }

// special case handling for unwanted tokens
object VOID

fun <Type : TokenType, R> Parser<Type, R>.void(): Parser<Type, VOID> = this.map { VOID }

operator fun <Type : TokenType, A, B> Parser<Type, A>.plus(other: Parser<Type, B>): Parser<Type, Pair<A, B>> =
	this.flatMap { a -> other.map { a to it } }

@JvmName("voidPlus")
operator fun <Type : TokenType, B> Parser<Type, VOID>.plus(next: Parser<Type, B>): Parser<Type, B> =
	this.flatMap { next }

@JvmName("plusVoid")
operator fun <Type : TokenType, A> Parser<Type, A>.plus(next: Parser<Type, VOID>): Parser<Type, A> =
	this.flatMap { a -> next.map { a } }

@JvmName("voidPlusVoid")
operator fun <Type : TokenType> Parser<Type, VOID>.plus(next: Parser<Type, VOID>): Parser<Type, VOID> =
	this.flatMap { next.map { VOID } }

/**
 * 0 to inf many repetitions
 */
fun <Type : TokenType, R> many(parser: Parser<Type, R>): Parser<Type, List<R>> = EagerParser({
	val list = mutableListOf<R>()
	var state = this
	loop {
		when (val result = parser.apply(state)) {
			is ParserResult.Ok -> {
				list += result.result
				state = result.next
			}
			is ParserResult.Error -> return@EagerParser ParserResult.Ok(
				state.addSkippedError(result),
				list
			)
		}
	}
}, "many of $parser")

fun <Type : TokenType, R> optional(parser: Parser<Type, R>): Parser<Type, R?> = EagerParser({
	parser.apply(this).flatMapLeft { ParserResult.Ok(this.addSkippedError(it), null) }
}, "optional of $parser")

fun <Type : TokenType, R> repeatSep(
	parser: Parser<Type, R>,
	sep: Parser<Type, Any>
): Parser<Type, List<R>> =
	optional(parser + many(sep.void() + parser)).map { p ->
		if (p == null) emptyList()
		else {
			val (head, tail) = p
			listOf(head) + tail
		}
	}

infix fun <Type:TokenType, R> Parser<Type, R>.or(other: Parser<Type, R>): Parser<Type, R> = EagerParser({
	this@or.apply(this).flatMapLeft { firstError ->
		other.apply(this).flatMapLeft { firstError neither it }
	}
}, "${this.name}|${other.name}")

// helper
private inline fun loop(f: () -> Unit): Nothing {
	while (true) {
		f()
	}
}