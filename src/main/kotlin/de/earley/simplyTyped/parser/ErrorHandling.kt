package de.earley.simplyTyped.parser

import de.earley.newParser.ErrorData
import de.earley.newParser.ParseResult
import de.earley.parser.SourcePosition
import de.earley.parser.Token
import de.earley.parser.src
import de.earley.simplyTyped.Diagnostics
import de.earley.simplyTyped.treeString
import kotlin.system.exitProcess

fun ErrorData<Token<SimplyTypedLambdaToken>>.mapByPosition() : Map<SourcePosition, ErrorData<Token<SimplyTypedLambdaToken>>> = when (this) {
    ErrorData.Fix -> emptyMap()
    is ErrorData.ExpectedName -> {
        val pos = actual?.src() ?: SourcePosition.Start //TODO at end?
        mapOf(pos to this)
    }
    is ErrorData.ExpectedEnd -> emptyMap() //TODO is this case useful? mapOf(actual.src() to this)
    is ErrorData.Filtered<*> -> TODO()
    is ErrorData.Named -> this.data.mapByPosition().mapValues { (_, v) ->
        ErrorData.Named(name, v)
    }
    is ErrorData.Multiple -> {
        this.errors.flatMap { it.mapByPosition().entries }
                .groupBy({ it.key }, {it.value})
                .mapValues { (_, v) -> ErrorData.Multiple.from(v) }
    }
}

fun ErrorData<Token<SimplyTypedLambdaToken>>.keepOneName() : Pair<Boolean, ErrorData<Token<SimplyTypedLambdaToken>>> = when (this) {
    ErrorData.Fix -> false to this
    is ErrorData.ExpectedName -> false to this
    is ErrorData.ExpectedEnd -> false to this
    is ErrorData.Filtered<*> -> false to this
    is ErrorData.Named -> {
        val (hasName, inner) = data.keepOneName()
        true to if (hasName) inner else ErrorData.Named(name, inner)
    }
    is ErrorData.Multiple -> {
        val inner = errors.map { it.keepOneName() }
        val hasName = inner.any { it.first } //TODO any? all?
        hasName to ErrorData.Multiple.from(inner.map { it.second })
    }
}

private fun <I> ErrorData<I>.prettyList() : List<String> = when (this) {
    ErrorData.Fix -> emptyList()
    is ErrorData.ExpectedName -> listOf("'$expected'")
    is ErrorData.ExpectedEnd -> emptyList()
    is ErrorData.Filtered<*> -> listOf(filterName)
    is ErrorData.Named -> data.prettyList().map {
        "$it for [$name]"
    }
    is ErrorData.Multiple -> errors.flatMap { it.prettyList() }
}

private fun ErrorData<Token<SimplyTypedLambdaToken>>.findActual(): Token<SimplyTypedLambdaToken>? = when (this) {
    ErrorData.Fix -> null
    is ErrorData.ExpectedName -> actual
    is ErrorData.ExpectedEnd -> actual
    is ErrorData.Filtered<*> -> null
    is ErrorData.Named -> data.findActual()
    is ErrorData.Multiple -> errors.asSequence().map { it.findActual() }.firstOrNull { it != null }
}

//TODO error handling for 'λ x : Nat'

fun handleError(result : ParseResult.Error<Token<SimplyTypedLambdaToken>>, diagnostics: Diagnostics) : Nothing {
    // error handling strategy
    // 1. Only keep the most relevant name
    val named = result.error.keepOneName().second
    // 2. Only show errors for the most progressed parser
    val max = named.mapByPosition().maxBy { it.key }!!
    // 3. All errors are "expected something" errors. Extract the actual token:
    val actual = max.value.findActual()

    diagnostics.error("Error at ${max.key}. Found '${actual?.value}', but expected one of: ")

    // 4. Now show the errors
   val msg =  max.value.prettyList().joinToString(separator = "\n") { "- $it" }

    diagnostics.error(msg)

    exitProcess(1)
}