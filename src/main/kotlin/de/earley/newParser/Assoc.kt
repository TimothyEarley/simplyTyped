package de.earley.newParser

//TODO extract delegate pattern?

class Assoc<I, O>(
        private var parser : Parser<I, O>,
        private val left : Boolean
) : Parser<I, O> {
    override fun deriveNull(): ParseResult<I, O> = when (val result = parser.deriveNull()) {
        is ParseResult.Ok.Single -> result
        is ParseResult.Ok.Multiple -> ParseResult.Ok.Single(
                if (left) result.set.first() else result.set.last()
        )
        is ParseResult.Error -> result
    }

    override fun derive(i: I): Parser<I, O> = Assoc(parser.derive(i), left)

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = ifNotSeen<Parser<I, O>>(seen, this) {
        parser = parser.compact(seen)
        if (parser is Empty) parser
        else this
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>): String = ifNotSeen(seen, "") {
        dotNode("left assoc") + parser.toDot(seen) + dotPath(this, parser)
    }

    override fun toString(): String = "l($parser)"
}

/**
 * Give priority to the first choice in the parsing tree.
 */
fun <I, O> Parser<I, O>.leftAssoc() = Assoc(this, true)

/**
 * Give priority to the last choice in the parsing tree.
 */
fun <I, O> Parser<I, O>.rightAssoc() = Assoc(this, false)