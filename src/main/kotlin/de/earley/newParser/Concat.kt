package de.earley.newParser

/**
 * Concatenates two parsers.
 */
// TODO vararg concat
class Concat<I, A, B> internal constructor(
        private var p1 : Parser<I, A>,
        private var p2 : Parser<I, B>
) : Fix<I, Pair<A, B>>() {
    val first : Parser<I, A>
        get() = p1
    val second : Parser<I, B>
        get() = p2

    override fun innerDerive(i : I): Parser<I, Pair<A, B>> {
        // the input is in the first parser
        val inFirst = p1.derive(i) + p2
        // the input is in the second parser and the first parser is finished
        val inSecond = delta(p1) + p2.derive(i)
        return inFirst or inSecond
    }

    override fun innerDeriveNull() : ParseResult<I, Pair<A, B>> {
        val r1 = p1.deriveNull()
        val r2 = p2.deriveNull()

        return r1.flatMap { a ->
            r2.map { b ->
                a to b
            }
        }
    }

    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, Pair<A, B>> = ifNotSeen<Parser<I, Pair<A, B>>>(seen, this) {
        p1 = p1.compact(seen, disregardErrors)
        if (p1 is Empty) return@ifNotSeen p1 as Empty //TODO error msgs

        p2 = p2.compact(seen, disregardErrors)
        if (p2 is Empty) return@ifNotSeen p2 as Empty

        // TODO compaction with p1 = Epsilon
        this
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Concat") + p1.toDot(seen) + p2.toDot(seen) + dotPath(this, p1, "1") + dotPath(this, p2, "2")
    }

    override fun toString(): String = "($p1 + $p2)"

    override fun size(seen: MutableSet<Parser<*, *>>): Int = ifNotSeen(seen, 1) {
        1 + p1.size(seen) + p2.size(seen)
    }
}

private fun <I, A, B> doPlus(a : Parser<I, A>, b : Parser<I, B>) : Parser<I, Pair<A, B>> = when {
    a is Empty -> a
    b is Empty -> b
    else -> Concat(a, b)
}

operator fun <I, A, B> Parser<I, A>.plus(other : Parser<I, B>) : Parser<I, Pair<A, B>> = doPlus(this, other)

@JvmName("voidPlus")
operator fun <I, B> Parser<I, VOID>.plus(other : Parser<I, B>) : Parser<I, B> =
    doPlus(this, other).map(Pair<VOID, B>::second)

@JvmName("plusVoid")
operator fun <I, A> Parser<I, A>.plus(other : Parser<I, VOID>) : Parser<I, A> =
    doPlus(this, other).map(Pair<A, VOID>::first)

@JvmName("voidPlusVoid")
operator fun <I> Parser<I, VOID>.plus(other : Parser<I, VOID>) : Parser<I, VOID> =
        doPlus(this, other).map { _ -> VOID }

object VOID
fun <I, O> Parser<I, O>.void() : Parser<I, VOID> = Reduce(this, "void") { VOID }


/**
 * Instances of [parser] separated by [delimiter].
 */
fun <I, O> many(parser : Parser<I, O>, delimiter : Parser<I, *>) : Parser<I, Iterable<O>> = recursive { many ->
    val empty = epsilon<I, List<O>>(emptyList())
    // 1 , 2 , 3
    val one = parser.map { listOf(it) }
    val more = (parser + delimiter.void() + many).map { head, tail ->
        Cons(head, tail)
    }

    empty or one or more
}

// might be a case of premature optimisation. Used to construct the iterable in many
private data class Cons<T>(val head : T, val tail : Iterable<T>) : Iterable<T> {
    override fun iterator(): Iterator<T> = iterator {
        yield(head)
        yieldAll(tail)
    }
}