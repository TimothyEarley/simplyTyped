package de.earley.newParser

class Reduce<I, A, B>(
        private var p : Parser<I, A>,
        private val name : String,
        private val reduce : (A) -> B
) : Parser<I, B> {
    override fun derive(i: I) = Reduce(p.derive(i), name, reduce)
    override fun deriveNull(): ParseResult<I, B> = p.deriveNull().map(reduce)
    override fun compact(seen: MutableSet<Parser<*, *>>, disregardErrors: Boolean): Parser<I, B> = ifNotSeen(seen, this) {
        val compact = p.compact(seen, disregardErrors)
        val result : Parser<I, B> = when {
            compact is Empty<I> -> compact
            //TODO can I merge reduces? Typing it is difficult

            //TODO make this more general (mark things that can be trvially reduced)
            compact is Epsilon<I, A> -> epsilon(compact.result.map(reduce))
            compact is Concat<I, *, *> && compact.first is Epsilon && compact.second is Epsilon -> epsilon(compact.deriveNull().map { reduce(it as A)})
            else -> {
                p = compact
                this
            }
        }
        result
    }

    // reduce is not in the graph
    override fun dotName(): String = p.dotName()
    override fun toDot(seen: MutableSet<Parser<*, *>>) = p.toDot(seen)
    override fun toString(): String = "$name($p)"
    override fun size(seen: MutableSet<Parser<*, *>>): Int = ifNotSeen(seen, 1) {
        1 + p.size(seen)
    }
}

fun <I, A, B> Parser<I, A>.map(f : (A) -> B) : Parser<I, B> = Reduce(this, "map", f)

@JvmName("mapPair")
fun <I, A, B, C> Parser<I, Pair<A, B>>.map(f : (A, B) -> C) : Parser<I, C> =
        map { (a, b) -> f(a, b) }
@JvmName("mapPairPair")
fun <I, A, B, C, D> Parser<I, Pair<Pair<A, B>, C>>.map(f : (A, B, C) -> D) : Parser<I, D> =
        map { (p, c) -> f(p.first, p.second, c) }
@JvmName("map4")
fun <I, A, B, C, D, E> Parser<I, Pair<Pair<Pair<A, B>, C>, D>>.map(f : (A, B, C, D) -> E) : Parser<I, E> =
        map { (p, c) -> f(p.first.first, p.first.second, p.second, c) }
@JvmName("map5")
fun <I, A, B, C, D, E, F> Parser<I, Pair<Pair<Pair<Pair<A, B>, C>, D>, E>>.map(f : (A, B, C, D, E) -> F) : Parser<I, F> =
        map { (p, c) -> f(p.first.first.first, p.first.first.second, p.first.second, p.second, c) }
@JvmName("map6")
fun <I, A, B, C, D, E, F, G> Parser<I, Pair<Pair<Pair<Pair<Pair<A, B>, C>, D>, E>, F>>.map(f : (A, B, C, D, E, F) -> G) : Parser<I, G> =
        map { (p, c) -> f(p.first.first.first.first, p.first.first.first.second, p.first.first.second, p.first.second, p.second, c) }