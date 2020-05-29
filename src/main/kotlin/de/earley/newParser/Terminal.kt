package de.earley.newParser

import de.earley.parser.SourcePosition
import de.earley.parser.Token
import de.earley.parser.TokenType
import de.earley.parser.src

data class Terminal<I>(val name: String? = null, val check: (I) -> Boolean) : Parser<I, I> {
    override fun derive(i: I) =
        if (check(i)) epsilon(i) else Empty(ParseResult.Error(ErrorData.Expected(name, i)))

    override fun deriveNull(): ParseResult<I> = ParseResult.Error(ErrorData.Expected(name, null))

    override fun compact(seen: MutableSet<Parser<*, *>>): Parser<I, I> = this
    override fun toDot(seen: MutableSet<Parser<*, *>>) = ifNotSeen(seen, "") {
        dotNode(name ?: "Terminal")
    }
}

fun char(c : Char) : Parser<Char, Char> = Terminal { it == c }

// Token stuff

//TODO rename to "isA"
fun <T : TokenType> token(type : T) : Parser<Token<T>, Token<T>> = Terminal("[$type]") { it.type == type }
fun <I, T : TokenType> Parser<I, Token<T>>.src() : Parser<I, SourcePosition> = map { it.src() }
fun <I, T : TokenType> Parser<I, Token<T>>.string() : Parser<I, String> = map { it.value }
fun <I, T : TokenType> Parser<I, Token<T>>.matches(name : String) : Parser<I, Token<T>> = filter("matches '$name'") { it.value == name }