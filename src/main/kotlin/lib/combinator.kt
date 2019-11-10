package lib

//TODO do this: http://www.vpri.org/pdf/tr2007002_packrat.pdf

typealias Parser<Type, R> = Lazy<ParserState<Type>.() -> ParserResult<Type, R>>

fun <Type : TokenType, R> Parser<Type, R>.apply(state: ParserState<Type>): ParserResult<Type, R> {
	//TODO what if token is null
	val cached = state.readMemo(this, state.token()!!)
	return if (cached == null || state.differentChain(this)) {
		val memo = state.writeMemo(this, state.token()!!, ParserResult.Error.LeftRecursion(state.token()))
		val result = this.value.invoke(memo)
		result //TODO set memo
	} else {
		cached
	}
}

fun <Type:TokenType, R> Parser<Type, R>.run(tokens: TokenStream<Type>): ParserResult<Type, R>
		= this.apply(ParserState.of(tokens))

// combinators

fun <Type:TokenType, A, B> Parser<Type, A>.map(f: (A) -> B): Parser<Type, B> = lazy {
	fun ParserState<Type>.(): ParserResult<Type, B> {
		return this@map.apply(this).map(f)
	}
}

fun <Type:TokenType, A, B> Parser<Type, A>.flatMap(f: (A) -> Parser<Type, B>): Parser<Type, B> = lazy {
	fun ParserState<Type>.(): ParserResult<Type, B> {
		return this@flatMap.apply(this).flatMap {
			f(it.result).apply(it.next)
		}
	}
}

fun <Type:TokenType, A1, A2, B> Parser<Type, Pair<A1, A2>>.map(f: (A1, A2) -> B): Parser<Type, B>
		= this.map { (a1, a2) -> f(a1, a2) }

fun <Type:TokenType, A1, A2, A3, B> Parser<Type, Pair<Pair<A1, A2>, A3>>.map(f: (A1, A2, A3) -> B): Parser<Type, B>
		= this.map { (a12, a3) -> f(a12.first, a12.second, a3) }

fun <Type:TokenType> ParserContext.isA(type: Type): Parser<Type, Token<Type>> = lazy {
	fun ParserState<Type>.(): ParserResult<Type, Token<Type>> {
		return match(type)
	}
}

val <Type:TokenType> Parser<Type, Token<Type>>.string get() = map { it.value }

// special case handling for unwanted tokens
object VOID
fun <Type:TokenType, R> Parser<Type, R>.void(): Parser<Type, VOID> = this.map { VOID }

operator fun <Type:TokenType, A, B> Parser<Type, A>.plus(next: Parser<Type, B>): Parser<Type, Pair<A, B>>
		= this.flatMap { a -> next.map { a to it } }

@JvmName("voidPlus")
operator fun <Type:TokenType, B> Parser<Type, VOID>.plus(next: Parser<Type, B>): Parser<Type, B>
		= this.flatMap { next }

@JvmName("plusVoid")
operator fun <Type:TokenType, A> Parser<Type, A>.plus(next: Parser<Type, VOID>): Parser<Type, A>
		= this.flatMap { a -> next.map { a } }

@JvmName("voidPlusVoid")
operator fun <Type:TokenType> Parser<Type, VOID>.plus(next: Parser<Type, VOID>): Parser<Type, VOID>
		= this.flatMap { next.map { VOID } }

/**
 * 0 to inf many repetitions
 */
fun <Type:TokenType, R> many(parser: Parser<Type, R>): Parser<Type, List<R>> = lazy {
	fun ParserState<Type>.(): ParserResult<Type, List<R>> {
		val list = mutableListOf<R>()
		var state = this
		loop {
			when (val result = parser.apply(state)) {
				is ParserResult.Ok -> {
					list += result.result
					state = result.next
				}
				is ParserResult.Error -> return ParserResult.Ok(
					state.addSkippedError(result),
					list
				)
			}
		}
	}
}

fun <Type:TokenType, R> optional(parser: Parser<Type, R>): Parser<Type, R?> = lazy {
	fun ParserState<Type>.(): ParserResult<Type, R?> {
		return parser.apply(this).flatMapLeft { ParserResult.Ok(this.addSkippedError(it), null) }
	}
}

fun <Type:TokenType, R> repeatSep(parser: Parser<Type, R>, sep: Parser<Type, Any>): Parser<Type, List<R>> =
	optional(parser + many(sep.void() + parser)).map { p ->
		if (p == null) emptyList()
		else {
			val (head, tail) = p
			listOf(head) + tail
		}
	}

infix fun <Type:TokenType, R> Parser<Type, R>.or(other: Parser<Type, R>): Parser<Type, R> = lazy {
	fun ParserState<Type>.(): ParserResult<Type, R> {
		return this@or.apply(this).flatMapLeft { firstError ->
			other.apply(this).flatMapLeft { firstError neither it }
		}
	}
}

// helper
inline fun loop(f: () -> Unit): Nothing {
	while (true) {
		f()
	}
}