package de.earley.simplyTyped

import de.earley.newParser.ParseResult
import de.earley.newParser.deriveAll
import de.earley.parser.*
import de.earley.simplyTyped.parser.SimplyTypedGrammar
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken
import de.earley.simplyTyped.parser.SimplyTypedLambdaToken.*
import de.earley.simplyTyped.parser.handleError
import de.earley.simplyTyped.terms.*
import de.earley.simplyTyped.types.addFolding
import de.earley.simplyTyped.types.recover
import de.earley.simplyTyped.types.resolveUserTypes
import de.earley.simplyTyped.types.type
import java.io.InputStream
import kotlin.system.exitProcess

@ExperimentalStdlibApi
fun main() {
	processFile("/example.tl", PrintDiagnostics)
}

@ExperimentalStdlibApi
fun readInput(input : InputStream) = input.readBytes().decodeToString()

fun extraParens(src : String) : String = "($src)"

fun lexer(src: String) = lex(src, values(), EOF)
		.filter { it.type != WS }
		.filter { it.type != COMMENT }

fun parser(tokens: TokenStream<SimplyTypedLambdaToken>): TypedTerm = SimplyTypedGrammar.grammar.run(tokens).orExit()

fun newParser(tokens : TokenStream<SimplyTypedLambdaToken>, diagnostics: Diagnostics): TypedTerm =
		SimplyTypedGrammar.newGrammar.deriveAll(tokens.toSequence())
				.handleResult(diagnostics)

fun ParseResult<Token<SimplyTypedLambdaToken>, TypedTerm>.handleResult(diagnostics: Diagnostics) : TypedTerm = when (this) {
	is ParseResult.Ok.Single -> t
	is ParseResult.Ok.Multiple -> {
		diagnostics.warn("${set.size} trees found.")
		set.first()
	}
	is ParseResult.Error -> handleError(this, diagnostics)
	is ParseResult.Ok.Maybe<*, TypedTerm> -> {
		ok.handleResult(diagnostics)
	}
}

fun checkFreeVariables(parsed: TypedTerm) {
	val free = parsed.freeVariables()
	require(free.isEmpty()) {
		//TODO diagnostics
		"free variables in $parsed: \n" + free.joinToString(separator = "\n") {
			it.src.toString() + " " + it.name
		}
	}
}

fun typeCheck(parsed: TypedNamelessTerm, diagnostics: Diagnostics): TypedNamelessTerm {
	val type = parsed.type().recover {
		diagnostics.error("Typing error: ${it.msg} \nat: ${it.element} \nat ${it.element.src}")
		exitProcess(1)
	}
	diagnostics.info("$parsed : $type")

	return parsed
}

fun removeTypes(parsed: TypedNamelessTerm): UntypedNamelessTerm = parsed.toUntyped()

fun unname(named: TypedTerm): TypedNamelessTerm = named.toNameless(emptyMap())

fun debugTokens(tokens: TokenStream<SimplyTypedLambdaToken>) = tokens.also {
	println(tokens.toList().map { it.type })
}

operator fun <A, B, C> ((A) -> B).plus(other: (B) -> C): (A) -> C = {
	other(this(it))
}


fun <T> T.treeString(
		pretty : T.() -> String,
		children : T.() -> List<T>,
		indent: String = ""
) : String {

	return indent  + pretty(this) + "\n" + children(this).joinToString("") { it.treeString(pretty, children, "$indent|   ") }

}

//TODO proper logging? logging + extra stuff?
interface Diagnostics {
	fun info(s : String)
    fun warn(s : String)
    fun error(s : String)
}

object PrintDiagnostics : Diagnostics {
	override fun info(s: String) {
		println("[info] $s")
	}

	override fun warn(s: String) {
        println("[warn] $s")
    }

    override fun error(s: String) {
        System.err.println("[error] $s")
    }
}

object NoDiagnostics : Diagnostics {
	override fun info(s: String) {}
	override fun warn(s: String) {}
    override fun error(s: String) {}
}

fun processSource(source : String, diagnostics : Diagnostics): UntypedNamelessTerm {
	val tokens = lexer(source)
	val parsed = newParser(tokens, diagnostics)
	checkFreeVariables(parsed)
	val unnamed = unname(parsed)
	val resolved = resolveUserTypes(unnamed)
	//TODO ::addFolding + or equi-rec types
	typeCheck(resolved, diagnostics)
	val untyped = removeTypes(resolved)
	return eval(untyped)
}

@ExperimentalStdlibApi
fun processInput(input : InputStream, diagnostics: Diagnostics) = processSource((readInput(input)), diagnostics)

@ExperimentalStdlibApi
fun processFile(file : String, diagnostics: Diagnostics) = processInput(SimplyTypedGrammar::class.java.getResourceAsStream(file), diagnostics)