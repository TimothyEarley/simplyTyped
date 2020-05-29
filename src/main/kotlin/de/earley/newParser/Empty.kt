package de.earley.newParser

/**
 * Represents a failed parser
 */
data class Empty(val error : ParseResult.Error) : Parser<Any?, Nothing> {
    override fun derive(a: Any?)  = this
    override fun deriveNull() = error

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<Any?, Nothing> = this

    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Empty")
    }
}