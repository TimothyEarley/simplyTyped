package de.earley.newParser

data class Reduce<I, A, B>(
        private var p : Parser<I, A>,
        private val reduce : (A) -> B
) : Parser<I, B> {
    override fun derive(i: I) = Reduce(p.derive(i), reduce)
    override fun deriveNull(): ParseResult<B> = when (val result = p.deriveNull()) {
        is ParseResult.Ok.Single -> ParseResult.Ok.Single(reduce(result.t))
        is ParseResult.Ok.Multiple -> ParseResult.Ok.Multiple.nonEmpty(result.set.map(reduce).toSet())
        is ParseResult.Error -> result
    }

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, B> = ifNotSeen(seen, this) {
        p = p.compact(seen)

        val result: Parser<I, B> = when (p) {
            is Empty -> p as Empty
            //TODO combine reductions
            else -> this
        }
        result
    }

    // reduce is not in the graph
    override fun dotName(): String = p.dotName()
    override fun toDot(seen: MutableSet<Parser<*, *>>) = p.toDot(seen)
}


fun <I, A, B> Parser<I, A>.map(f : (A) -> B) : Parser<I, B> = Reduce(this, f)
@JvmName("mapPair")
fun <I, A, B, C> Parser<I, Pair<A, B>>.map(f : (A, B) -> C) : Parser<I, C> =
    Reduce(this) { (a, b) -> f(a, b) }
@JvmName("mapPairPair")
fun <I, A, B, C, D> Parser<I, Pair<Pair<A, B>, C>>.map(f : (A, B, C) -> D) : Parser<I, D> =
    Reduce(this) { (p, c) -> f(p.first, p.second, c) }
@JvmName("map4")
fun <I, A, B, C, D, E> Parser<I, Pair<Pair<Pair<A, B>, C>, D>>.map(f : (A, B, C, D) -> E) : Parser<I, E> =
        Reduce(this) { (p, c) -> f(p.first.first, p.first.second, p.second, c) }
@JvmName("map5")
fun <I, A, B, C, D, E, F> Parser<I, Pair<Pair<Pair<Pair<A, B>, C>, D>, E>>.map(f : (A, B, C, D, E) -> F) : Parser<I, F> =
        Reduce(this) { (p, c) -> f(p.first.first.first, p.first.first.second, p.first.second, p.second, c) }