package de.earley.newParser

abstract class Fix<I, O> : Parser<I, O> {

    companion object {
        private val memo : PairMap<Parser<*, *>, Any?, Parser<*, *>> =
            PairMap()
    }

    @Suppress("UNCHECKED_CAST") // we must be careful what we put into the cache
    override fun derive(i: I): Parser<I, O> {
        return memo.getOrPut(this, i) {
            Delay(this, i)
        } as Parser<I, O>
    }

    abstract fun innerDerive(i : I): Parser<I, O>

    private var nullSet : ParseResults<O>? = null
    override fun deriveNull(): ParseResults<O> {
        if (nullSet != null) return nullSet!!

        var newSet = emptySet<O>()
        do {
            nullSet = newSet // importantly this prevents looping this method
            newSet = innerDeriveNull()
        } while (nullSet != newSet)

        return nullSet!!
    }

    abstract fun innerDeriveNull() : ParseResults<O>
}

private class Delay<I, O>(
        val parser: Fix<I, O>,
        val input : I
) : Parser<I, O> {
    private val derivative : Parser<I, O> by lazy {
        parser.innerDerive(input)
    }

    override fun derive(i: I): Parser<I, O> = derivative.derive(i)
    override fun deriveNull(): ParseResults<O> = derivative.deriveNull()
    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = derivative.compact(seen)

    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Delay")
    }
}

private class PairMap<A, B, C> {
    private val data : MutableMap<A, MutableMap<B, C>> = hashMapOf()
    fun getOrPut(a : A, b : B, c : () -> C) : C {
        val inner = data.getOrPut(a) { hashMapOf()}
        return inner.getOrPut(b, c)
    }
}