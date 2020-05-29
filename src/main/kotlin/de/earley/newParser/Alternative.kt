package de.earley.newParser

import de.earley.newParser.ParseResult.Error.Companion.combine

class Alternative<I, O> internal constructor(
        var parsers : List<Parser<I, O>>
) : Fix<I, O>() {
    override fun innerDerive(i: I) = Alternative(parsers.map { it.derive(i) })
    override fun innerDeriveNull() : ParseResult<O> {
        //TODO duplicate code in compact
        val (good, bad) = parsers.map { it.deriveNull() }
                .partitionResult()
        // TODO suppressed errors
        return when (val result = good.combine()) {
            is ParseResult.Ok -> result
            is ParseResult.Error -> bad.combine()
        }
    }

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = ifNotSeen(seen, this) {
        //TODO error msg
        val (good, bad) = parsers.map { it.compact(seen) }
                .partition { it !is Empty }

        parsers = good
        when {
            parsers.isEmpty() -> Empty((bad as List<Empty>).map { it.error }.combine())
            parsers.size == 1 -> parsers.first()
            else -> this
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
}

private fun <T> Iterable<ParseResult<T>>.partitionResult(): Pair<List<ParseResult.Ok<T>>, List<ParseResult.Error>> {
    val (ok, err) = partition { it is ParseResult.Ok }
    @Suppress("UNCHECKED_CAST") // checked by partition, will break if third case is added
    return (ok as List<ParseResult.Ok<T>>) to (err as List<ParseResult.Error>)
}

fun <I, O> or(vararg parsers : Parser<I, O>): Parser<I, O> {
    val nonEmpty = parsers
            .flatMap {
                if (it is Alternative) it.parsers
                else listOf(it)
            }
            //TODO .filter { it != Empty }
    return when {
        nonEmpty.isEmpty() -> Empty(ParseResult.Error(ErrorData.TempMsg("TODO or all empty"))) //TODO error msg
        nonEmpty.size == 1 -> nonEmpty.first()
        else -> Alternative(nonEmpty.toMutableList())
    }
}

infix fun <I, O> Parser<I, O>.or(other : Parser<I, O>) : Parser<I, O> = or(this, other)