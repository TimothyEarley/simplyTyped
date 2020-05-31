package de.earley.newParser

class Alternative<I, O> internal constructor(
        var parsers : List<Parser<I, O>>
) : Fix<I, O>() {
    override fun innerDerive(i: I) = Alternative(parsers.map { it.derive(i) })
    override fun innerDeriveNull() : ParseResult<I, O> = parsers.map { it.deriveNull() }.combine()

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = ifNotSeen(seen, this) {
        // assume all are flattened already
        //TODO error msg
        val (good, bad) = parsers.map { it.compact(seen) }
                .partition { it !is Empty }

        @Suppress("UNCHECKED_CAST") // checked by partition
        when {
            good.isEmpty() -> Empty((bad as List<Empty<I>>).map { it.error }.combineErrors())
            good.size == 1 -> good.first()
            else -> {
                parsers = good
                this
            }
        }
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        val sb = StringBuilder()
        sb.append(dotNode("Alternatives"))
        parsers.forEach {
            sb.append(it.toDot(seen))
        }
        parsers.forEach {
            sb.append(dotPath(this, it))
        }

        sb.toString()
    }

    override fun toString(): String = "(" + parsers.joinToString(" or ") + ")"
}

fun <I, O> or(vararg parsers : Parser<I, O>): Parser<I, O> {
    //TODO compact?
    val flattened = parsers
            .flatMap {
                if (it is Alternative) it.parsers
                else listOf(it)
            }
    return when (flattened.size) {
        1 -> flattened.first()
        else -> Alternative(flattened.toMutableList())
    }
}

infix fun <I, O> Parser<I, O>.or(other : Parser<I, O>) : Parser<I, O> = or(this, other)