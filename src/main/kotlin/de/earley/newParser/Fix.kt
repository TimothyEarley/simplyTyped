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

    private var result : ParseResult<I, O>? = null
    override fun deriveNull(): ParseResult<I, O> {
        if (result != null) return result!!

        var newResult : ParseResult<I, O> = ParseResult.Error(ErrorData.Fix)
        do {
            result = newResult // importantly this prevents looping this method
            newResult = innerDeriveNull()
        } while (result!!.set() != newResult.set())

        return newResult
    }

    abstract fun innerDeriveNull() : ParseResult<I, O>
}

private class Delay<I, O>(
        val parser: Fix<I, O>,
        val input : I
) : Parser<I, O> {
    private val derivative : Parser<I, O> by lazy {
        parser.innerDerive(input)
    }

    override fun derive(i: I): Parser<I, O> = derivative.derive(i)
    override fun deriveNull(): ParseResult<I, O> = derivative.deriveNull()
    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, O> = derivative.compact(seen)

    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode("Delay")
    }

    override fun toString(): String = "Delay[$input]($parser)"
}

private class PairMap<A, B, C> {
    // TODO weak map
    private val data : MutableMap<A, MutableMap<B, C>> = hashMapOf()
    fun getOrPut(a : A, b : B, c : () -> C) : C {
        val inner = data.getOrPut(a) { hashMapOf()}
        return inner.getOrPut(b, c)
    }
}