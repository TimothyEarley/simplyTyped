package de.earley.newParser

/**
 * Accepts no more input for the parser.
 *
 * The class is only there to provide a lazy encapsulation over epsilon(parser.deriveNull())
 */
class Delta<I, O>(private var parser: Parser<I, O>) : Parser<I, O> {
    override fun derive(i: I): Parser<I, O> = Empty(ParseResult.Error(ErrorData.ExpectedEnd(i)))
    override fun deriveNull(): ParseResult<I, O> = parser.deriveNull()

    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, O> = ifNotSeen<Parser<I, O>>(seen, this) {
        epsilon(parser.deriveNull())
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>): String = ifNotSeen(seen, "") {
        dotNode("Delta") + parser.toDot(seen) + dotPath(this, parser)
    }

    override fun size(seen: MutableSet<Parser<*, *>>): Int = ifNotSeen(seen, 1) {
        1 + parser.size(seen)
    }

    override fun toString(): String = "Delta($parser)"
}


fun <I, O> delta(parser : Parser<I, O>) : Parser<I, O> =
        if (parser is Empty) parser else Delta(parser)