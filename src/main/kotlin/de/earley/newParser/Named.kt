package de.earley.newParser

// for debugging
class Named<I, O>(
        private val name : String,
        private var p : Parser<I, O>
) : Parser<I, O> {
    override fun derive(i: I): Parser<I, O> = Named(name, p.derive(i))

    override fun deriveNull(): ParseResults<O> = p.deriveNull()

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = ifNotSeen(seen, this) {
        p = p.compact(seen)
        this
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>): String = ifNotSeen(seen, "") {
        dotNode("<$name>") + p.toDot(seen) + dotPath(this, p)
    }
}

fun <I, O> named(name : String, block : () -> Parser<I, O>) : Parser<I, O> = Named(name, block())