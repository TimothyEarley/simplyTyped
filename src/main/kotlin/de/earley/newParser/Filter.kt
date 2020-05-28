package de.earley.newParser

class Filter<I, O>(
        private var p : Parser<I, O>,
        private val cond : (O) -> Boolean
) : Parser<I, O> {

    override fun derive(i: I): Parser<I, O> = Filter(p.derive(i), cond)

    override fun deriveNull(): ParseResults<O> = p.deriveNull().filter(cond).toSet()

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = ifNotSeen(seen, this) {
        p = p.compact(seen)
        this
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>): String = ifNotSeen(seen, "") {
        dotNode("filter") + p.toDot(seen) + dotPath(this, p)
    }
}

fun <I, O> Parser<I, O>.filter(cond : (O) -> Boolean) : Parser<I, O> = Filter(this, cond)