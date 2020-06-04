package de.earley.simplyTyped.parser

import de.earley.newParser.ErrorData
import de.earley.newParser.ParseResult
import de.earley.parser.SourcePosition
import de.earley.parser.Token
import de.earley.parser.src
import de.earley.simplyTyped.treeString
import kotlin.system.exitProcess

/**
 * Simplify the error data
 */
private fun ErrorData<Token<*>>.strip(force : Boolean): ErrorData<Token<*>>? = when (this) {
    ErrorData.Fix -> null
    ErrorData.EmptyCombine -> null
    is ErrorData.ExpectedName -> if (actual == null) null else this
    is ErrorData.ExpectedEnd -> if (force) null else this
    is ErrorData.Filtered<*> -> this
    is ErrorData.Named -> {
        when (val inner = data.strip(force)) {
            is ErrorData.Named -> ErrorData.Named("$name.${inner.name}", inner.data)
            else -> inner?.let { ErrorData.Named(name, inner) }
        }
    }
    is ErrorData.Multiple -> {
        val inner = errors.mapNotNull { it.strip(force) }
        when (inner.size) {
            0 -> null //TODO this seems wrong
            1 -> inner.single()
            else -> ErrorData.Multiple.from(inner)
        }
    }
}

fun ErrorData<Token<*>>.max() : Pair<SourcePosition, ErrorData<Token<*>>> = when (this) {
    ErrorData.Fix -> SourcePosition.Start to this
    ErrorData.EmptyCombine -> SourcePosition.Start to this
    is ErrorData.ExpectedName -> (actual?.src() ?: SourcePosition.Start) to this
    is ErrorData.ExpectedEnd -> actual.src() to this
    is ErrorData.Filtered<*> -> SourcePosition.Start to this
    is ErrorData.Named -> data.max().let { (pos, d) -> pos to ErrorData.Named(name, d) }
    is ErrorData.Multiple -> errors.map { it.max() }.maxBy { it.first }!! // non empty
}

private fun ErrorData<Token<*>>.pretty(): String = this.treeString(
        { when(this) {
            ErrorData.Fix -> "End of recursion"
            ErrorData.EmptyCombine -> TODO()
            is ErrorData.Filtered<*> -> TODO()
            is ErrorData.Named -> "<${name}>"
            is ErrorData.Multiple -> "Multiple errors:"
            is ErrorData.ExpectedName -> "(${actual?.src()}) Expected [$expected], got [${actual?.value}] (${actual?.type})"
            is ErrorData.ExpectedEnd -> "(${actual.src()}) Expected the end, got [${actual.value}] (${actual.type})"
        } },
        { when(this) {
            ErrorData.Fix -> emptyList()
            ErrorData.EmptyCombine -> TODO()
            is ErrorData.Filtered<*> -> TODO()
            is ErrorData.Named -> listOf(data)
            is ErrorData.Multiple -> errors.toList()
            is ErrorData.ExpectedName -> emptyList()
            is ErrorData.ExpectedEnd -> emptyList()
        } }
)

fun handleError(result : ParseResult.Error<Token<SimplyTypedLambdaToken>>) : Nothing {
    System.err.println("Error parsing:")
    val prettyError = result.error /* .max().second */.strip(false)?.pretty()
    println(prettyError ?: result.error.pretty())
    exitProcess(1)
}