package de.earley.newParser

import io.kotest.core.spec.style.BehaviorSpec

internal class AssocTest : BehaviorSpec({

    val ambiguous = char('c').map { 1 } or char('c').map { 2 }

    testParser(ambiguous) {
        sequenceOf('c') isOk setOf(1, 2)
    }

    testParser(ambiguous.leftAssoc()) {
        sequenceOf('c') isOk 1
    }

    testParser(ambiguous.rightAssoc()) {
        sequenceOf('c') isOk 2
    }

})