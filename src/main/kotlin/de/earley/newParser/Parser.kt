package de.earley.newParser

interface Parser<in Input, out Output> {
    fun derive(i : Input) : Parser<Input, Output>
    fun deriveNull() : ParseResult<Output>
    fun compact(seen : MutableSet<Parser<*, *>>) : Parser<Input, Output>
    fun toDot(seen : MutableSet<Parser<*, *>>) : String //TODO make sealed interface and implement externally
    fun dotName() : String = hashCode().toString()
}

fun <T> Parser<*, *>.ifNotSeen(seen : MutableSet<Parser<*, *>>, default : T, block : () -> T) : T =
    if (! seen.contains(this)) {
        seen.add(this)
        block()
    } else default

sealed class ErrorData {
    /**
     * Start of the fix recursion.
     */
    object Fix : ErrorData() //TODO understand and make sure it is not in final output
    object EmptyCombine : ErrorData()
    class Multiple private constructor(val errors : List<ErrorData>) : ErrorData() {
        companion object {
            fun from(errors : List<ErrorData>) : ErrorData {
                val flattened = errors.flatMap { when (it) {
                    is Multiple -> it.errors
                    else -> listOf(it)
                } }
                return Multiple(flattened)
            }
        }
    }
    data class Expected<I>(val expected : I?, val actual : I?) : ErrorData()
    data class Filtered<O>(val original : O, val filterName : String) : ErrorData()
    data class TempMsg(val msg : String) : ErrorData() // TODO remove

}

sealed class ParseResult<out T> {
    sealed class Ok<T> : ParseResult<T>() {
        data class Single<T>(val t : T) : Ok<T>()
        class Multiple<T> private constructor(val set : Set<T>) : Ok<T>() {
            companion object {
                fun <T> nonEmpty(set : Set<T>) : Multiple<T> {
                    require(set.isNotEmpty())
                    return Multiple(set)
                }
            }
        }
    }
    data class Error(val error : ErrorData) : ParseResult<Nothing>() {
        companion object {
            //TODO when combining, only keep the most relevant errors (furthest along with parsing?)
            fun List<Error>.combine() : Error = Error(ErrorData.Multiple.from(this.map { it.error }))
        }
    }
}

fun <T> ParseResult<T>.set() : Set<T> = when (this) {
    is ParseResult.Ok.Single -> setOf(t)
    is ParseResult.Ok.Multiple -> set
    is ParseResult.Error -> emptySet()
}

fun <T> List<ParseResult.Ok<T>>.combine() : ParseResult<T> {
    if (this.isEmpty()) return ParseResult.Error(ErrorData.EmptyCombine)

    val set : Set<T> = this.flatMapTo(mutableSetOf<T>()) { when (it) {
        is ParseResult.Ok.Single -> setOf(it.t)
        is ParseResult.Ok.Multiple -> it.set
    } }
    return ParseResult.Ok.Multiple.nonEmpty(set)
}

var index = 0
fun <I, O> Parser<I, O>.deriveAll(inputs : Sequence<I>) =
    inputs.fold(this) { parser, i ->
        val next = parser.derive(i).compact(mutableSetOf())
//        next.graphWriter("dot/${index++}.dot")
        next
    }.deriveNull()