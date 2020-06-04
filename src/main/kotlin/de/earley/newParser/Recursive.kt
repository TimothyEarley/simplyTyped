package de.earley.newParser

class Recursive<I, O> : Fix<I, O>() {
    lateinit var p : Parser<I, O>
    override fun innerDerive(i: I) = p.derive(i)
    override fun innerDeriveNull() = p.deriveNull()

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = p.compact(seen)

    // not in dot graph
    override fun dotName(): String = p.dotName()
    override fun toDot(seen: MutableSet<Parser<*, *>>) = p.toDot(seen)
    override fun toString(): String = //TODO combine rec with name to have nicer toString
        if (::p.isInitialized) "rec(${p.hashCode()})" else "rec"
}

fun <I, O> recursive(block : (Parser<I, O>) -> Parser<I, O>) : Parser<I, O> {
    val parser = Recursive<I, O>()
    parser.p = block(parser)
    return parser
}