package de.earley.newParser

/**
 * A parser that accepts no more input. If at end, produces [result].
 */
class Epsilon<I, O> internal constructor(
        val result : ParseResult.Ok<O>
) : Parser<I, O> {
    override fun derive(i: I): Parser<I, O> = Empty(ParseResult.Error(ErrorData.ExpectedEnd(i)))
    override fun deriveNull() = result
    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, O> = this
    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Epsilon $result")
    }

    override fun toString(): String = "e($result)"
    override fun size(seen: MutableSet<Parser<*, *>>): Int = 1
}

fun <I, O> epsilon(o : O) : Parser<I, O> = Epsilon(ParseResult.Ok.Single(o))

fun <I, O> epsilon(result: ParseResult<I, O>) : Parser<I, O> = when (result) {
    is ParseResult.Ok -> Epsilon(result)
    is ParseResult.Error -> Empty(result)
}