package de.earley.newParser

class Filter<I, O>(
        private var p : Parser<I, O>,
        private val cond : (O) -> Boolean,
        private val name : String
) : Parser<I, O> {

    override fun derive(i: I): Parser<I, O> = Filter(p.derive(i), cond, name)

    override fun deriveNull(): ParseResult<I, O> = when (val result = p.deriveNull()) {
        is ParseResult.Ok.Single -> if (cond(result.t)) result else ParseResult.Error(ErrorData.Filtered(result.t, name))
        is ParseResult.Ok.Multiple -> ParseResult.Ok.Multiple.nonEmpty(result.set.filter(cond).toSet()) //TODO error prop
        is ParseResult.Error -> result
        is ParseResult.Ok.Maybe<*, *> -> TODO()
    }

    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, O> = ifNotSeen(seen, this) {
        p = p.compact(seen, disregardErrors)
        this
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>): String = ifNotSeen(seen, "") {
        dotNode("filter $name") + p.toDot(seen) + dotPath(this, p)
    }

    override fun toString(): String = "filter[$name]($p)"
    override fun size(seen: MutableSet<Parser<*, *>>): Int = ifNotSeen(seen, 1) {
        1 + p.size(seen)
    }
}

fun <I, O> Parser<I, O>.filter(name : String, cond : (O) -> Boolean) : Parser<I, O> = Filter(this, cond, name)