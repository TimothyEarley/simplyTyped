import NamelessTerm.*

fun NamelessTerm.eval(): NamelessTerm {
	var current = this
	loop {
		val next = current.evalStep()
		if (current == next) return current
		current = next
	}


}
private inline fun loop(f: () -> Unit): Nothing {
	while (true) {
		f()
	}
}

private fun NamelessTerm.evalStep(): NamelessTerm = when (this) {
	is Variable -> this
	is Abstraction -> this
	is App -> when {
		left is Abstraction && right.isValue() -> left.body.sub(0, right.shift(1, 0)).shift(-1, 0)
		!left.isValue() -> App(left.evalStep(), right)
		left.isValue() && !right.isValue() -> App(left, right.evalStep())
		else -> error("No applicable rule!")
	}
}

private fun NamelessTerm.isValue(): Boolean = when (this) {
	is Variable, is Abstraction -> true
	is App -> false
}

/**
 * [d] places with [c] cutoff
 */
private fun NamelessTerm.shift(d: Int, c: Int): NamelessTerm = when (this) {
	is Variable -> Variable(if (number < c) number else number + d)
	is Abstraction -> Abstraction(body.shift(d, c + 1))
	is App -> App(left.shift(d, c), right.shift(d, c))
}

private fun NamelessTerm.sub(num: Int, replacement: NamelessTerm): NamelessTerm = when (this) {
	is Variable -> if (number == num) replacement else this
	is Abstraction -> Abstraction(body.sub(num + 1, replacement.shift(1, 0)))
	is App -> App(left.sub(num, replacement), right.sub(num, replacement))
}