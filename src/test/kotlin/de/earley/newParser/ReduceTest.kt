package de.earley.newParser

import io.kotest.core.spec.style.BehaviorSpec

internal class ReduceTest : BehaviorSpec({

    testParser(Reduce(char('c') ,"test") { "$it$it"}) {

        sequenceOf('c') isOk setOf("cc")
        sequenceOf('a') isError ErrorData.ExpectedName("c", 'a')

    }

})