package de.earley.newParser

/**
 * Represents a failed parser
 */
data class Empty<I>(val error : ParseResult.Error<I>) : Parser<I, Nothing> {
    override fun derive(i: I)  = this
    override fun deriveNull() = error

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, Nothing> = this

    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Empty")
    }
}