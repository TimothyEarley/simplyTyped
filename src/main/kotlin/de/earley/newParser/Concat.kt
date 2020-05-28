package de.earley.newParser

class Concat<I, A, B> internal constructor(
        private var p1 : Parser<I, A>,
        private var p2 : Parser<I, B>
) : Fix<I, Pair<A, B>>() {
    override fun innerDerive(i : I) =
        (p1.derive(i) + p2) or (Delta(p1) + p2.derive(i))

    override fun innerDeriveNull() = p1.deriveNull().flatMapTo(mutableSetOf()) { a ->
        p2.deriveNull().map {  b ->
            a to b
        }
    }

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, Pair<A, B>> {
        if (! seen.contains(this)) {
            seen.add(this)
            p1 = p1.compact(seen)
            p2 = p2.compact(seen)
        }

        return when {
            p1 is Empty || p2 is Empty -> Empty
            // TODO compaction with p1 = Epsilon
            else -> this
        }
    }

    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Concat") + p1.toDot(seen) + p2.toDot(seen) + dotPath(this, p1, "1") + dotPath(this, p2, "2")
    }

}

private fun <I, A, B> doPlus(a : Parser<I, A>, b : Parser<I, B>) : Parser<I, Pair<A, B>> = when {
    a is Empty || b is Empty -> Empty
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
fun <I, O> Parser<I, O>.void() : Parser<I, VOID> = this.map { VOID }

