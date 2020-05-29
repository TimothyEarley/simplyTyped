package de.earley.newParser

// val EmptyEpsilon = Epsilon<Nothing>()

class Epsilon<O> internal constructor(private val result : ParseResult<O>) :
        Parser<Any?, O> {

    override fun derive(i: Any?) = Empty(ParseResult.Error(ErrorData.Expected(null, i)))
    override fun deriveNull() = result
    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<Any?, O> = this
    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Epsilon $result")
    }
}

fun <O> epsilon(o : O) : Parser<Any?, O> = Epsilon(ParseResult.Ok.Single(o))

fun <O> epsilon(result: ParseResult<O>) : Parser<Any?, O> = when (result) {
    is ParseResult.Ok -> Epsilon(result)
    is ParseResult.Error -> Empty(result)
}