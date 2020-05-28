package de.earley.newParser

object Empty : Parser<Any?, Nothing> {
    override fun derive(a: Any?)  = Empty
    override fun deriveNull() = emptySet<Nothing>()

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<Any?, Nothing> = Empty

    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Empty")
    }
}