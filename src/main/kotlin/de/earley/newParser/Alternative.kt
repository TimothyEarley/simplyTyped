package de.earley.newParser

class Alternative<I, O> internal constructor(
        var parsers : List<Parser<I, O>>
) : Fix<I, O>() {

    private val hiddenErrors : MutableList<ErrorData<I>> = mutableListOf()

    override fun innerDerive(i: I) = Alternative(parsers.map { it.derive(i) }).also {
        it.hiddenErrors += hiddenErrors
    }
    override fun innerDeriveNull() : ParseResult<I, O> {
        //TODO clean up, perf
        return (parsers.map { it.deriveNull() } + (hiddenErrors.map { ParseResult.Error(it) })).combine()
    }

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = ifNotSeen(seen, this) {
        // assume all are flattened already
        val (good, bad) = parsers.map { it.compact(seen) }
                .partition { it !is Empty }

        //TODO use same abstraction as [combine]
        //TODO we loose error info here
        @Suppress("UNCHECKED_CAST") // checked by partition
        when {
            good.isEmpty() -> Empty((bad as List<Empty<I>>).map { it.error }.combineErrors())
            bad.isEmpty() -> when {
                good.size == 1 -> good.first()
                else -> {
                    parsers = good
                    this
                }
            }
            else -> {
                //TODO this removes compaction when there is only one parser
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