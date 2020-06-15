package de.earley.newParser

class Alternative<I, O> internal constructor(
        var parsers : List<Parser<I, O>>
) : Fix<I, O>() {

    //TODO performance
    private val hiddenErrors : MutableList<ErrorData<I>> = mutableListOf()

    override fun innerDerive(i: I) = Alternative(parsers.map { it.derive(i) }).also {
        it.hiddenErrors += hiddenErrors
    }
    override fun innerDeriveNull() : ParseResult<I, O> {
        //TODO clean up, perf
        return (parsers.map { it.deriveNull() } + (hiddenErrors.map { ParseResult.Error(it) })).combine()
    }

    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, O> = ifNotSeen(seen, this) {
        // assume all are flattened already
        val (good, bad) = parsers.map { it.compact(seen, disregardErrors) }
                .partition { it !is Empty }

        //TODO use same abstraction as [combine]
        //TODO we loose error info here (if hiddenErrors is set already)
        @Suppress("UNCHECKED_CAST") // checked by partition
        when {
            good.isEmpty() -> Empty((bad as List<Empty<I>>).map { it.error }.combineErrors())
            bad.isEmpty() || disregardErrors -> when (good.size) {
                1 -> good.first()
                else -> {
                    parsers = good
                    this
                }
            }
            else -> {
                hiddenErrors.addAll(bad.map { (it as Empty).error.error })
                parsers = good
                this
            }
        }
    }

    override fun deriveNull(): ParseResult<I, O> {
        val result = super.deriveNull()
        return if (hiddenErrors.isNotEmpty()) when (result) {
            //TODO flatten?
            is ParseResult.Ok -> ParseResult.Ok.Maybe(result, ErrorData.Multiple.from(hiddenErrors))
            is ParseResult.Error -> when(result.error) {
                ErrorData.Fix -> ParseResult.Error(ErrorData.Multiple.from(hiddenErrors))
                else -> ParseResult.Error(ErrorData.Multiple.from(hiddenErrors + result.error))
            }
        }
        else result
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

    override fun size(seen: MutableSet<Parser<*, *>>): Int = ifNotSeen(seen, 1) {
        1 + parsers.sumBy { it.size(seen) }
    }
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