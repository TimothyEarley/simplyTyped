package de.earley.newParser

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

interface ParserTestSpec<I, O> {
    suspend infix fun Sequence<I>.isOk(expected : O) = isOk(setOf(expected))
    suspend infix fun Sequence<I>.isOk(expected : Set<O>)
    suspend infix fun Sequence<I>.isError(expected : ErrorData<I>)

}

fun <I, O> BehaviorSpec.testParser(parser : Parser<I, O>, body : suspend ParserTestSpec<I, O>.() -> Unit) {
    given("the parser $parser") {

        object : ParserTestSpec<I, O> {
            override suspend fun Sequence<I>.isOk(expected: Set<O>) {
                `when`("we input ${this.toList()}") {
                    val result = parser.deriveAll(this@isOk)
                    then("the result is ok") {
                        result.shouldBeInstanceOf<ParseResult.Ok<O>> {
                            it.set() shouldBe expected
                        }
                    }
                }
            }

            override suspend fun Sequence<I>.isError(expected: ErrorData<I>) {
                `when`("we input ${this.toList()}") {
                    val result = parser.deriveAll(this@isError)
                    then("the result is an error") {
                        result.shouldBeInstanceOf<ParseResult.Error<I>> {
                            it.error shouldBe expected
                        }
                    }
                }
            }
        }.body()
    }
}