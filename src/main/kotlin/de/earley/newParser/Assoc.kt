package de.earley.newParser

//TODO extract delegate pattern?

class Assoc<I, O>(
        private var parser : Parser<I, O>,
        private val left : Boolean
) : Parser<I, O> {
    override fun deriveNull(): ParseResult<I, O> = applyAssoc(parser.deriveNull())

    private fun applyAssoc(result : ParseResult<I, O>) : ParseResult<I, O> = when (result) {
        is ParseResult.Ok.Single -> result
        is ParseResult.Ok.Multiple -> ParseResult.Ok.Single(
                if (left) result.set.first() else result.set.last()
        )
        is ParseResult.Error -> result
        is ParseResult.Ok.Maybe<*, O> -> ParseResult.Ok.Maybe(applyAssoc(result.ok) as ParseResult.Ok<O>, result.error)
    }

    override fun derive(i: I): Parser<I, O> = Assoc(parser.derive(i), left)

    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, O> = ifNotSeen<Parser<I, O>>(seen, this) {
        parser = parser.compact(seen, disregardErrors)
        if (parser is Empty) parser
        else this
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>): String = ifNotSeen(seen, "") {
        dotNode("left assoc") + parser.toDot(seen) + dotPath(this, parser)
    }

    override fun toString(): String = if (left) "l($parser)" else "r($parser)"
    override fun size(seen: MutableSet<Parser<*, *>>): Int = ifNotSeen(seen, 1) {
        1 + parser.size(seen)
    }
}

/**
 * Give priority to the first choice in the parsing tree.
 */
fun <I, O> Parser<I, O>.leftAssoc() = Assoc(this, true)

/**
 * Give priority to the last choice in the parsing tree.
 */
fun <I, O> Parser<I, O>.rightAssoc() = Assoc(this, false)