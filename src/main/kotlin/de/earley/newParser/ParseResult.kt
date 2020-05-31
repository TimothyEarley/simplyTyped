package de.earley.newParser

sealed class ParseResult<out I, out O> {
    sealed class Ok<O> : ParseResult<Nothing, O>() {
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
    data class Error<out I>(val error : ErrorData<I>) : ParseResult<I, Nothing>()
}

fun <I, O> ParseResult<I, O>.set() : Set<O> = when (this) {
    is ParseResult.Ok.Single<O> -> setOf(t)
    is ParseResult.Ok.Multiple<O> -> set
    is ParseResult.Error<*> -> emptySet()
}

sealed class ErrorData<out I> {

    /**
     * How far along the parsing the error occoured. Used to find the best error.
     */
//    abstract val progress : Int

    /**
     * Start of the fix recursion.
     */
    object Fix : ErrorData<Nothing>()
    object EmptyCombine : ErrorData<Nothing>()
    data class ExpectedName<I>(val expected : String, val actual : I?) : ErrorData<I>()
    data class ExpectedEnd<I>(val actual: I) : ErrorData<I>()
    data class Filtered<O>(val original : O, val filterName : String) : ErrorData<Nothing>()
    data class Named<I>(val name : String, val data: ErrorData<I>) : ErrorData<I>()
    class Multiple<I> private constructor(val errors : Set<ErrorData<I>>) : ErrorData<I>() {
        companion object {
            fun <I> from(errors : List<ErrorData<I>>) : ErrorData<I> {
                val flattened = errors.flatMap { when (it) {
                    is Multiple -> it.errors
                    else -> listOf(it)
                } }
                return Multiple(flattened.toSet())
            }
        }
    }
}

fun <I, A, B> ParseResult<I, A>.map(f : (A) -> B) : ParseResult<I, B> = when (this) {
    is ParseResult.Ok.Single<A> -> ParseResult.Ok.Single(f(this.t))
    is ParseResult.Ok.Multiple<A> -> ParseResult.Ok.Multiple.nonEmpty(this.set.mapTo(mutableSetOf(), f))
    is ParseResult.Error<I> -> this
}

fun <I, A, B> ParseResult<I, A>.flatMap(f : (A) -> ParseResult<I, B>) : ParseResult<I, B> = when (this) {
    is ParseResult.Ok.Single<A> -> f(this.t)
    is ParseResult.Ok.Multiple<A> -> this.set.map(f).combine()
    is ParseResult.Error<I> -> this
}

fun <I, A> ParseResult<I, A>.mapError(f : (ParseResult.Error<I>) -> ParseResult<I, A>) : ParseResult<I, A> = when (this) {
    is ParseResult.Ok -> this
    is ParseResult.Error -> f(this)
}

@JvmName("combineResults")
fun <I, T> List<ParseResult<I, T>>.combine() : ParseResult<I, T> {
    val (good, bad) = this.partitionResult()

    // TODO suppressed errors
    return when (val result = good.combineOks()) {
        is ParseResult.Ok -> result
        is ParseResult.Error -> bad.combineErrors()
    }
}

fun <T> List<ParseResult.Ok<T>>.combineOks() : ParseResult<Nothing, T> {
    if (this.isEmpty()) return ParseResult.Error(ErrorData.EmptyCombine)

    val set : Set<T> = this.flatMapTo(mutableSetOf<T>()) { when (it) {
        is ParseResult.Ok.Single -> setOf(it.t)
        is ParseResult.Ok.Multiple -> it.set
    } }
    return ParseResult.Ok.Multiple.nonEmpty(set)
}

fun <I> List<ParseResult.Error<I>>.combineErrors() : ParseResult.Error<I> = ParseResult.Error(ErrorData.Multiple.from(this.map { it.error }))

fun <I, T> Iterable<ParseResult<I, T>>.partitionResult(): Pair<List<ParseResult.Ok<T>>, List<ParseResult.Error<I>>> {
    val (ok, err) = partition { it is ParseResult.Ok }
    @Suppress("UNCHECKED_CAST") // checked by partition, will break if third case is added
    return (ok as List<ParseResult.Ok<T>>) to (err as List<ParseResult.Error<I>>)
}