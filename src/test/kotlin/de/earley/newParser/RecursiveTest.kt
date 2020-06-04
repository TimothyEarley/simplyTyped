package de.earley.newParser

import io.kotest.core.spec.style.BehaviorSpec

internal class RecursiveTest : BehaviorSpec({

    // infinite parser
    testParser<Char, String>(recursive { r ->
        (char('c') + r).map { a, b -> a + b }
    }) {
        sequenceOf('c', 'c', 'c') isError ErrorData.ExpectedName("c", null)
    }

    testParser<Char, String>(recursive { r ->
        (char('c') + (epsilon<Char, String>("") or r)).map { a, b -> a + b }
    }) {
        sequenceOf<Char>() isError ErrorData.ExpectedName("c", null)
        sequenceOf('c', 'c', 'c') isOk "ccc"
    }

})