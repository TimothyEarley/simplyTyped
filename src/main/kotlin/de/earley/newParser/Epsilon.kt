package de.earley.newParser

// val EmptyEpsilon = Epsilon<Nothing>()

class Epsilon<O> internal constructor(private val trees : ParseResults<O>) :
        Parser<Any?, O> {

    override fun derive(i: Any?) = Empty
    override fun deriveNull() = trees
    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<Any?, O> = this
    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Epsilon $trees")
    }
}

fun <O> epsilon(o : O) : Parser<Any?, O> = Epsilon(setOf(o))

fun <O> epsilon(trees: ParseResults<O>) : Parser<Any?, O> = when {
    trees.isEmpty() -> Empty
    else -> Epsilon(trees)
}