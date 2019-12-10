package untyped

import untyped.NamelessTerm.*

sealed class Term {
	data class Variable(val name: String): Term() {
		override fun toString(): String = name
	}
	data class Abstraction(val binder: String, val body: Term): Term() {
		override fun toString(): String = "(λ$binder.$body)"
	}
	data class App(val left: Term, val right: Term): Term() {
		override fun toString(): String = "($left $right)"
	}
}

sealed class NamelessTerm {
	data class Variable(val number: Int): NamelessTerm() {
		override fun toString(): String = number.toString()
	}
	data class Abstraction(val body: NamelessTerm): NamelessTerm(){
		override fun toString(): String = "(λ.$body)"
	}
	data class App(val left: NamelessTerm, val right: NamelessTerm): NamelessTerm() {
		override fun toString(): String = "($left $right)"
	}
}

private typealias Bindings = Map<String, Int>
private fun Bindings.inc(): Bindings = this.mapValues { (_, v) -> v + 1 }
fun Term.toNameless(
	bindings: Bindings = emptyMap()
): NamelessTerm = when (this) {
	is Term.Variable -> Variable(bindings[name] ?: error("free variable"))
	is Term.Abstraction -> Abstraction(body.toNameless(bindings.inc() + (binder to 0)))
	is Term.App -> App(left.toNameless(bindings), right.toNameless(bindings))
}

private val variables = listOf(
	"x", "y", "z", "a", "b", "c", "d", "e"
)

private typealias ReverseBindings = Map<Int, String>
@JvmName("reverseInc")
private fun ReverseBindings.inc(): ReverseBindings = this.mapKeys { (k, _) -> k + 1 }
private fun ReverseBindings.newName() =
	if (this.isEmpty()) variables.first()
	else variables[values.map { variables.indexOf(it) }.max()!! + 1]

fun NamelessTerm.toNamed(
	bindings: ReverseBindings = emptyMap()
): Term = when (this) {
	is Variable -> Term.Variable(bindings[number] ?: error("free variable"))
	is Abstraction -> {
		val binder = bindings.newName()
		Term.Abstraction(binder, body.toNamed(bindings.inc() + (0 to binder)))
	}
	is App -> Term.App(left.toNamed(bindings), right.toNamed(bindings))
}