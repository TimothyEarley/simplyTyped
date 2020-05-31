package de.earley.newParser

/**
 * A parser that accepts no more input.
 */
data class Delta<I, O>(val p : Parser<I, O>) : Parser<I, O> {
    override fun derive(i: I) = TODO() // Empty(ParseResult.Error(ErrorData.Expected(null, i)))
    override fun deriveNull() = p.deriveNull()
    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = epsilon(p.deriveNull())
    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Delta") + p.toDot(seen) + dotPath(this, p)
    }
}