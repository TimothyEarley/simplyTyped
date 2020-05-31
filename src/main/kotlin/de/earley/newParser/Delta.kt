package de.earley.newParser

/*
data class Delta<I, O>(val p : Parser<I, O>) : Parser<I, O> {
    override fun derive(i: I) = TODO() // Empty(ParseResult.Error(ErrorData.Expected(null, i)))
    override fun deriveNull() = TODO() // p.deriveNull()
    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = TODO() // epsilon(p.deriveNull())
    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Delta") + p.toDot(seen) + dotPath(this, p)
    }
}
*/

/**
 * Accepts no more input for the parser.
 */
fun <I, O> delta(parser : Parser<I, O>) : Parser<I, O> = epsilon(parser.deriveNull())