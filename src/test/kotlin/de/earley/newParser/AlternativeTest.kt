package de.earley.newParser

import io.kotest.core.spec.style.BehaviorSpec

internal class AlternativeTest : BehaviorSpec({

    testParser(char('a') or char('b')) {

        sequenceOf('a') isOk 'a'
        sequenceOf('b') isOk 'b'

        sequenceOf('c') isError ErrorData.Multiple.from(listOf(
                ErrorData.ExpectedName("a", 'c'),
                ErrorData.ExpectedName("b", 'c')
        ))
    }

    testParser(char('c').map { 1 } or char('c').map { 2 }) {
        sequenceOf('c') isOk setOf(1, 2)
        sequenceOf('x') isError ErrorData.Multiple.from(listOf(
                ErrorData.ExpectedName("c", 'x')
        ))
    }

})