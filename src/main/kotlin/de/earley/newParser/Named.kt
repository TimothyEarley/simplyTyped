package de.earley.newParser

// for debugging and nicer error messages
class Named<I, O>(
        private val name : String,
        private var p : Parser<I, O>
) : Parser<I, O> {
    override fun derive(i: I): Parser<I, O> {
        val result = Named(name, p.derive(i))
        return result
    }

    override fun deriveNull(): ParseResult<I, O> = p.deriveNull().mapError { ParseResult.Error(ErrorData.Named(name, it.error)) }

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = ifNotSeen(seen, this) {
        p = p.compact(seen)
        val result : Parser<I, O> = when (val result = p.compact(seen)) {
            is Empty -> Empty(ParseResult.Error( ErrorData.Named(name, result.error.error)))
            else -> {
                p = result
                this
            }
        }
        result
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>): String = ifNotSeen(seen, "") {
        dotNode("<$name>") + p.toDot(seen) + dotPath(this, p)
    }

    override fun toString(): String = name
}

fun <I, O> named(name : String, block : () -> Parser<I, O>) : Parser<I, O> = Named(name, block())