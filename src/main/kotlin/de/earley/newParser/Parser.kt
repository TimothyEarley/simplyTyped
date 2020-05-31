package de.earley.newParser

interface Parser<Input, out Output> {
    fun derive(i : Input) : Parser<Input, Output>
    fun deriveNull() : ParseResult<Input, Output>
    fun compact(seen : MutableSet<Parser<*, *>>) : Parser<Input, Output>
    fun toDot(seen : MutableSet<Parser<*, *>>) : String //TODO make sealed interface and implement externally
    fun dotName() : String = hashCode().toString()
}

fun <T> Parser<*, *>.ifNotSeen(seen : MutableSet<Parser<*, *>>, default : T, block : () -> T) : T =
    if (! seen.contains(this)) {
        seen.add(this)
        block()
    } else default


var index = 0
fun <I, O> Parser<I, O>.deriveAll(inputs : Sequence<I>) =
    inputs.fold(this) { parser, i ->
        val next = parser.derive(i).compact(mutableSetOf())
//        next.graphWriter("dot/${index++}.dot")
        next
    }.deriveNull()