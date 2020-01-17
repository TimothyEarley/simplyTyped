package de.earley.simplyTyped

import de.earley.simplyTyped.terms.Keyword
import de.earley.simplyTyped.terms.Keyword.*
import de.earley.simplyTyped.terms.UntypedNamelessTerm
import de.earley.simplyTyped.terms.UntypedNamelessTerm.*
import kotlin.contracts.contract

fun UntypedNamelessTerm.eval(): UntypedNamelessTerm {
	var current = this
	while(! current.isValue()) {
		println(current) //TODO debug switch
		current = current.evalStep()
	}
	return current
}
private inline fun loop(f: () -> Unit): Nothing {
	while (true) {
		f()
	}
}

//TODO check for value, and then dont do a step
//TODO nullable return
private fun UntypedNamelessTerm.evalStep(): UntypedNamelessTerm = when {
	isValue() -> error("Value can not do a step: $this")
	this is App -> when {
		/**
		 * Arithmetic
		 */
		isOp(Arithmetic.IsZero) -> when {
			/*E-IsZeroZero*/ right.isKeyword(Arithmetic.Zero) -> Bools.True.asTerm()
			/*E-IsZeroSucc*/ right.isOp(Arithmetic.Succ) -> Bools.False.asTerm()
			/*E-IsZero*/ !right.isValue() -> copy(right = right.evalStep())
			else -> error("No applicable rule!")
		}
		isOp(Arithmetic.Pred) -> when {
			/*E-PredZero*/ right.isKeyword(Arithmetic.Zero) -> right
			/*E-PredSucc*/ right.isOp(Arithmetic.Succ) -> right.right
			/*E-Pred*/ !right.isValue() -> copy(right = right.evalStep())
			else -> error("No applicable rule!")
		}
		/**
		 * Plain lambda calc
		 */
		/*E-AppAbs*/ left is Abstraction && right.isValue() -> left.body.sub(0, right.shift(1, 0)).shift(-1, 0)
		/*E-App1*/ !left.isValue() -> copy(left = left.evalStep())
		/*E-App2*/ left.isValue() && !right.isValue() -> copy(right = right.evalStep())
		else -> error("No applicable rule for $this!")
	}
	this is LetBinding -> when {
		/*E-LetV*/ bound.isValue() -> expression.sub(0, bound)
		/*E-Let*/ else -> copy(bound = bound.evalStep())
	}
	this is Record -> {
		/*E-Rcd*/
		// find first non value
		val firstNonValue = contents.entries.find { !it.value.isValue() }
		require(firstNonValue != null) { "Record had no values left, so should be a value itself" }
		val newContents = contents.toMutableMap()
		newContents[firstNonValue.key] = firstNonValue.value.evalStep()
		copy(contents = newContents)
	}
	this is RecordProjection && record is Record -> when {
		/*E-ProjRcd*/ record.isValue() -> record.contents[project] ?: error("Attempted to project unknown label $project out of record $record")
		/*E-Proj*/ else -> copy(record = record.evalStep())
	}
	else -> error("No applicable rule for $this!")
}

private fun UntypedNamelessTerm.isOp(keyword: Keyword): Boolean {
	contract {
		returns(true) implies (this@isOp is App)
	}
	return this is App && left.isKeyword(keyword)
}

private fun UntypedNamelessTerm.isKeyword(keyword: Keyword): Boolean {
	contract {
		returns(true) implies (this@isKeyword is KeywordTerm)
	}
	return this is KeywordTerm && this.keyword == keyword
}

private fun Keyword.asTerm() = KeywordTerm(this)

private fun UntypedNamelessTerm.isValue(): Boolean = when (this) {
	is Variable, is Abstraction -> true
	is App -> {
		left.isKeyword(Arithmetic.Succ) //TODO improve this
	}
	is KeywordTerm -> keyword.isValue
	is LetBinding -> false
	is Record -> contents.values.all { it.isValue() }
	is RecordProjection -> false
}

/**
 * [d] places with [c] cutoff
 */
private fun UntypedNamelessTerm.shift(d: Int, c: Int): UntypedNamelessTerm = when (this) {
	is Variable -> Variable(if (number < c) number else number + d)
	is Abstraction -> Abstraction(body.shift(d, c + 1))
	is App -> App(left.shift(d, c), right.shift(d, c))
	is KeywordTerm -> this
	is LetBinding -> TODO()
	is Record -> Record(contents.mapValues { it.value.shift(d, c) })
	is RecordProjection -> RecordProjection(record.shift(d, c), project)
}

/**
 * Substitute variables with [num] in [this] with [replacement]
 */
private fun UntypedNamelessTerm.sub(num: Int, replacement: UntypedNamelessTerm): UntypedNamelessTerm = when (this) {
	is Variable -> if (number == num) replacement else this
	is Abstraction -> Abstraction(body.sub(num + 1, replacement.shift(1, 0)))
	is App -> App(left.sub(num, replacement), right.sub(num, replacement))
	is KeywordTerm -> this
	is LetBinding -> LetBinding(bound.sub(num, replacement), expression.sub(num + 1, replacement.shift(1, 0)))
	is Record -> Record(contents.mapValues { it.value.sub(num, replacement) })
	is RecordProjection -> RecordProjection(record.sub(num, replacement), project)
}