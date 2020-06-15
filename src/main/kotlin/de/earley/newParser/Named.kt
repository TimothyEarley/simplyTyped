package de.earley.newParser

// for debugging and nicer error messages
class Named<I, O>(
        private val name : String,
        private var p : Parser<I, O>
) : Parser<I, O> {
    override fun derive(i: I): Parser<I, O> = Named(name, p.derive(i))

    override fun deriveNull(): ParseResult<I, O> = p.deriveNull().mapError { ErrorData.Named(name, it) }

    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, O> = ifNotSeen(seen, this) {
        when (val result = p.compact(seen, disregardErrors)) {
            is Empty -> Empty(ParseResult.Error( ErrorData.Named(name, result.error.error)))
            else -> {
                p = result
                this
            }
        }
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>): String = ifNotSeen(seen, "") {
        dotNode("<$name>") + p.toDot(seen) + dotPath(this, p)
    }

    override fun toString(): String = name
    override fun size(seen: MutableSet<Parser<*, *>>): Int = ifNotSeen(seen, 1) {
        1 + p.size(seen)
    }
}

fun <I, O> named(name : String, block : () -> Parser<I, O>) : Parser<I, O> = Named(name, block())