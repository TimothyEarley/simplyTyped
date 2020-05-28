package de.earley.newParser

import kotlin.text.StringBuilder

class Alternative<I, O> internal constructor(
        var parsers : List<Parser<I, O>>
) : Fix<I, O>() {
    override fun innerDerive(i: I) = Alternative(parsers.map { it.derive(i) })
    override fun innerDeriveNull() = parsers.flatMapTo(mutableSetOf()) { it.deriveNull() }

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = ifNotSeen(seen, this) {
        parsers = parsers.map { it.compact(seen) }
                .filter { it != Empty }

        if (parsers.size == 1) parsers.first() else this
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
}

fun <I, O> or(vararg parsers : Parser<I, O>): Parser<I, O> {
    val nonEmpty = parsers
            .flatMap {
                if (it is Alternative) it.parsers
                else listOf(it)
            }
            .filter { it != Empty }
    return when {
        nonEmpty.isEmpty() -> Empty
        nonEmpty.size == 1 -> nonEmpty.first()
        else -> Alternative(nonEmpty.toMutableList())
    }
}

infix fun <I, O> Parser<I, O>.or(other : Parser<I, O>) : Parser<I, O> = or(this, other)