package de.earley.simplyTyped.parser

import de.earley.newParser.testParser
import de.earley.parser.SourcePosition
import de.earley.parser.Token
import de.earley.parser.TokenType
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.terms.TypedTerm
import de.earley.simplyTyped.types.Type
import io.kotest.core.spec.style.BehaviorSpec

internal class BasicTermGrammar : BehaviorSpec({

    testParser(SimplyTypedGrammar.newGrammar) {

        tokens(
                Lambda to "λ",
                Identifier to "x",
                Colon to ":",
                Identifier to "Nat",
                Dot to ".",
                Identifier to "y",
                EOF to "EOF"
        ) isOk TypedTerm.Abstraction("x", Type.Nat, TypedTerm.Variable("y", SourcePosition.Synth), SourcePosition.Synth)

        tokens(
                Identifier to "x",
                Identifier to "y",
                EOF to "EOF"
        ) isOk TypedTerm.App(
                TypedTerm.Variable("x", SourcePosition.Synth),
                TypedTerm.Variable("y", SourcePosition.Synth),
                SourcePosition.Synth
        )


    }

})

private fun <T : TokenType> tokens(vararg t : Pair<T, String>) : Sequence<Token<T>> = sequenceOf(*t).map {
    val s = SourcePosition.Synth
    Token(s.line, s.col, it.second, it.first)
}