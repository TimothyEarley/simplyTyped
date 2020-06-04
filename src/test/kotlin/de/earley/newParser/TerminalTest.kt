package de.earley.newParser

import io.kotest.core.spec.style.BehaviorSpec

internal class TerminalTest : BehaviorSpec({

    val name = "char test"
    val char = 'c'

    testParser(Terminal<Char>(name) { it == char }) {
        sequenceOf(char) isOk char
        sequenceOf('a') isError ErrorData.ExpectedName(name, 'a')
        emptySequence<Char>() isError ErrorData.ExpectedName(name, null)
    }

})