package de.earley.newParser

class Epsilon<I, O> internal constructor(private val result : ParseResult<I, O>) :
        Parser<I, O> {

    override fun derive(i: I): Parser<I, O> = Empty(ParseResult.Error(ErrorData.ExpectedEnd(i)))
    override fun deriveNull() = result
    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = this
    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Epsilon $result")
    }
}

fun <I, O> epsilon(o : O) : Parser<I, O> = Epsilon(ParseResult.Ok.Single(o))

fun <I, O> epsilon(result: ParseResult<I, O>) : Parser<I, O> = when (result) {
    is ParseResult.Ok -> Epsilon(result)
    is ParseResult.Error -> Empty(result)
}