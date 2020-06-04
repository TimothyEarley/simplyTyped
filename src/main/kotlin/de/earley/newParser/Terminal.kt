package de.earley.newParser

import de.earley.parser.SourcePosition
import de.earley.parser.Token
import de.earley.parser.TokenType
import de.earley.parser.src

data class Terminal<I>(val name: String, val check: (I) -> Boolean) : Parser<I, I> {
    override fun derive(i: I) : Parser<I, I> =
        if (check(i))
            epsilon(i)
        else
            Empty(ParseResult.Error(ErrorData.ExpectedName<I>(name, i)))

    override fun deriveNull(): ParseResult<I, I> = ParseResult.Error(ErrorData.ExpectedName(name, null))

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, I> = this
    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode(name)
    }

    override fun toString(): String = name
}

fun char(c : Char) : Parser<Char, Char> = Terminal(c.toString()) { it == c }

// Token stuff

//TODO rename to "isA"
fun <T : TokenType> token(type : T) : Parser<Token<T>, Token<T>> = Terminal(type.symbol) { it.type == type }
fun <I, T : TokenType> Parser<I, Token<T>>.src() : Parser<I, SourcePosition> = Reduce(this, "src") { it.src() }
fun <I, T : TokenType> Parser<I, Token<T>>.string() : Parser<I, String> = Reduce(this, "string") { it.value }
fun <I, T : TokenType> Parser<I, Token<T>>.matches(name : String) : Parser<I, Token<T>> = filter("matches '$name'") { it.value == name }