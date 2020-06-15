package de.earley.newParser

interface Parser<Input, out Output> {
    fun derive(i : Input) : Parser<Input, Output>
    fun deriveNull() : ParseResult<Input, Output>

    /**
     * Attempt to reduce the number of nodes in the parse graph.
     * If [disregardErrors] is true the reduction might discard error information
     */
    fun compact(seen : MutableSet<Parser<*, *>>, disregardErrors: Boolean) : Parser<Input, Output>

    // debug stuff
    fun toDot(seen : MutableSet<Parser<*, *>>) : String //TODO make sealed interface and implement externally
    fun dotName() : String = hashCode().toString()
    fun size(seen: MutableSet<Parser<*, *>>) : Int
}

fun <T> Parser<*, *>.ifNotSeen(seen : MutableSet<Parser<*, *>>, default : T, block : () -> T) : T =
    if (! seen.contains(this)) {
        seen.add(this)
        block()
    } else default


var index = 0
fun <I, O> Parser<I, O>.deriveAll(inputs : Sequence<I>) =
    inputs.fold(this) { parser, i ->
        val next = parser.derive(i).compact(mutableSetOf(), false)
//        next.graphWriter("dot/${index++}.dot")
        next
    }.deriveNull()