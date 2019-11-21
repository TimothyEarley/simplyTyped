package lib

fun <Type : TokenType, A, B> Parser<Type, A>.map(f: (A) -> B): Parser<Type, B> =
	EagerParser(
		{ this@map.apply(this).map(f) },
		{ this@map.backtrack()?.map(f) },
		{"map(${this.name})"}
	)

fun <Type : TokenType, A, B, C> Parser<Type, Pair<A, B>>.map(f: (A, B) -> C): Parser<Type, C> =
	EagerParser(
		{ this@map.apply(this).map { f(it.first, it.second) } },
		{this@map.backtrack().map(f)},
		{"map pair"}
	)

@JvmName("orNullable")
private fun <Type : TokenType, A> or(a: Parser<Type, A>?, b: Parser<Type, A>?): Parser<Type, A>? = when (a) {
	null -> b
	else -> when (b) {
		null -> a
		else -> a or b
	}
}

private fun <Type : TokenType, A, B> Parser<Type, A>.flatMapN(
	name: () -> String,
	nextName: () -> String,
	f: (A) -> Parser<Type, B>?
): Parser<Type, B> =
	this.flatMap(name, nextName) { f(it) ?: errorParser("null flatmap") }

fun <Type : TokenType, A, B> Parser<Type, A>.flatMap(
	name: () -> String,
	nextName: () -> String,
	f: (A) -> Parser<Type, B>
): Parser<Type, B> =
	LazyParser(kotlin.lazy { //TODO can this be eager?
		fun ParserState<Type>.(): ParserResult<Type, B> {
			return apply(this).flatMap { f(it.result).apply(it.next) }
		}
	}, {
		val backtrackFirst = this.backtrack()?.flatMap({ "[${name()}]'" }, nextName, f)
		//TODO leaving this feels wrong, but it works (together with rec. backtracking)
//		val backtrackSecond = this.flatMapN(name, { "[${nextName()}]'" }) { f(it).backtrack() }
//		or(backtrackFirst, backtrackSecond)
		backtrackFirst
	}, {name() + "+" + nextName()})

fun <Type : TokenType> ParserContext.isA(type: Type): Parser<Type, Token<Type>> =
	EagerParser({ match(type) }, {null}, {"isA $type"})

val <Type : TokenType> Parser<Type, Token<Type>>.string get() = map { it.value }

// special case handling for unwanted tokens
object VOID

fun <Type : TokenType, R> Parser<Type, R>.void(): Parser<Type, VOID> = this.map { VOID }.rename("~$name~")

operator fun <Type : TokenType, A, B> Parser<Type, A>.plus(next: Parser<Type, B>): Parser<Type, Pair<A, B>> =
	this.flatMap(this::name, next::name) { a -> next.map { a to it } }.rename("${this.name}+${next.name}")

@JvmName("voidPlus")
operator fun <Type : TokenType, B> Parser<Type, VOID>.plus(next: Parser<Type, B>): Parser<Type, B> =
	this.flatMap(this::name, next::name) { next }.rename("${this.name}+${next.name}")

@JvmName("plusVoid")
operator fun <Type : TokenType, A> Parser<Type, A>.plus(next: Parser<Type, VOID>): Parser<Type, A> =
	this.flatMap(this::name, next::name) { a -> next.map { a } }.rename("${this.name}+${next.name}")

@JvmName("voidPlusVoid")
operator fun <Type : TokenType> Parser<Type, VOID>.plus(next: Parser<Type, VOID>): Parser<Type, VOID> =
	this.flatMap(this::name, next::name) { next.map { VOID } }.rename("${this.name}+${next.name}")

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
}, { parser.backtrack()?.let { many(it) } },{"many of ${parser.name}"})

fun <Type : TokenType, R> optional(parser: Parser<Type, R>): Parser<Type, R?> = EagerParser({
	parser.apply(this).flatMapLeft { ParserResult.Ok(this.addSkippedError(it), null) }
}, { parser.backtrack()?.let { optional(it) } }, {"optional of ${parser.name}"})

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
}, {
	// the first parser could use some backtracking, or the first choice was bad
	val back = this@or.backtrack()
	if (back == null) other
	else back.rename("{$name}'") or other
}, {"${this.name}|${other.name}"})

fun <Type: TokenType, R> lazy(f: () -> Parser<Type, R>): Parser<Type, R> {
	val parser by kotlin.lazy { f() }
	return LazyParser(kotlin.lazy { parser::apply }, { parser.backtrack() }, {"lazy"})
}

//val empty: Parser<TokenType, Unit> = EagerParser({ParserResult.Ok(this, Unit)}, "empty")
//
fun <Type : TokenType> errorParser(msg: String = "error"): Parser<Type, Nothing>
		= EagerParser({ParserResult.Error.NoChoice(this.head)}, { errorParser<Type>() }, {msg})

infix fun <Type: TokenType, A, B> Parser<Type, A>.plusBacktrack(next: Parser<Type, B>): Parser<Type, Pair<A, B>> {
	val plainParser = (this + next).rename("($name+${next.name})") //TODO laziness?
	fun backtracked(from: Parser<Type, A>): Parser<Type, Pair<A, B>> {
		return lazy {
			val back = from.backtrack()
			if (back != null) {
				val recursiveBacktracked = backtracked(back)
				(back + next).rename("(${back.name}+${next.name})") or recursiveBacktracked
			} else TODO()
		}
	}

	return (plainParser or backtracked(this))//.rename("${this.name}+${next.name}")
}
// helper
private inline fun loop(f: () -> Unit): Nothing {
	while (true) {
		f()
	}
}