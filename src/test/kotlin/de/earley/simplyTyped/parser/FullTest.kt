package de.earley.simplyTyped.parser

import de.earley.simplyTyped.PrintDiagnostics
import de.earley.simplyTyped.processSource
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.InputStream

@ExperimentalStdlibApi
internal class FullTest : StringSpec({

    getResourcesInDirectory("examples").forEach { (name, data) ->
        "testing $name" {
            val source = data.bufferedReader().readLines()
            val expected = source.first().removePrefix("#")
            val test = source.joinToString("\n")
            processSource(test, PrintDiagnostics) shouldBe processSource(expected, PrintDiagnostics)
        }
    }

})


@ExperimentalStdlibApi
private fun getResourcesInDirectory(dir : String) : List<Pair<String, InputStream>> {
    val loader = FullTest::class.java.classLoader
    val paths = (loader.getResourceAsStream("examples") ?: return emptyList())
            .bufferedReader()
            .readLines()

    return paths.map {
        it to loader.getResourceAsStream("$dir/$it")
    }
}