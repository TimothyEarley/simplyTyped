package de.earley.simplyTyped

import de.earley.simplyTyped.terms.Keyword
import de.earley.simplyTyped.terms.Keyword.*
import de.earley.simplyTyped.terms.UntypedNamelessTerm
import de.earley.simplyTyped.terms.UntypedNamelessTerm.*
import kotlin.contracts.contract

fun UntypedNamelessTerm.eval(): UntypedNamelessTerm {
	var current = this.mem(emptyList())
	while(! current.expr.isValue()) {
		println(current) //TODO debug switch
		current = current.evalStep()
		//TODO GC
	}
	return current.expr.queryMemory(current.memory)
}

private class EvalState(
	val expr: UntypedNamelessTerm,
	val memory: List<UntypedNamelessTerm>
) {
	override fun toString(): String = "$memory : $expr "
}

//TODO check for value, and then dont do a step
//TODO nullable return
private fun EvalState.evalStep(): EvalState = with(expr) {
	if (isValue()) error("Value can not do a step: $this")
	else when (this) {
		is Variable -> error("variable cannot be evaluated")
		is Abstraction -> error("this is a value")
		is KeywordTerm -> error("this needs context")
		UntypedNamelessTerm.Unit -> error("this is a value")

		is App -> when {
			/**
			 * Arithmetic
			 */
			isOp(Arithmetic.IsZero) -> when {
				/*E-IsZeroZero*/ right.isKeyword(Arithmetic.Zero) -> Bools.True.asTerm().mem(memory)
				/*E-IsZeroSucc*/ right.isOp(Arithmetic.Succ) -> Bools.False.asTerm().mem(memory)
				/*E-IsZero*/ !right.isValue() -> right.mem(memory).evalStep().let {
					copy(right = it.expr).mem(it.memory)
				}
				else -> error("No applicable rule!")
			}
			isOp(Arithmetic.Pred) -> when {
				/*E-PredZero*/ right.isKeyword(Arithmetic.Zero) -> right.mem(memory)
				/*E-PredSucc*/ right.isOp(Arithmetic.Succ) -> right.right.mem(memory)
				/*E-Pred*/ !right.isValue() -> right.mem(memory).evalStep().let {
					copy(right = it.expr).mem(it.memory)
				}
				else -> error("No applicable rule!")
			}
			/**
			 * Plain lambda calc
			 */
			/*E-AppAbs*/ left is Abstraction && right.isValue() -> left.body.sub(
				0,
				right.shift(1, 0)
			).shift(-1, 0).mem(memory)
			/*E-App1*/ !left.isValue() -> left.mem(memory).evalStep().let {
				copy(left = it.expr).mem(it.memory)
			}
			/*E-App2*/ left.isValue() && !right.isValue() -> right.mem(memory).evalStep().let {
				copy(right = it.expr).mem(it.memory)
			}
			else -> error("No applicable rule for $this!")
		}
		is LetBinding -> when {
			/*E-LetV*/ bound.isValue() -> expression.sub(0, bound).mem(memory)
			/*E-Let*/ else -> bound.mem(memory).evalStep().let {
				copy(bound = it.expr).mem(it.memory)
			}
		}
		is Record -> {
			/*E-Rcd*/
			// find first non value
			val firstNonValue = contents.entries.find { !it.value.isValue() }
			require(firstNonValue != null) { "Record had no values left, so should be a value itself" }
			val newContents = contents.toMutableMap()
			val step = firstNonValue.value.mem(memory).evalStep()
			newContents[firstNonValue.key] = step.expr
			copy(contents = newContents).mem(step.memory)
		}
		is RecordProjection -> when {
			record !is Record -> error("projecting on a non record")
			/*E-ProjRcd*/ record.isValue() -> record.contents[project]?.mem(memory)
				?: error("Attempted to project unknown label $project out of record $record")
			/*E-Proj*/ else -> record.mem(memory).evalStep().let {
				copy(record = it.expr).mem(it.memory)
			}
		}
		is IfThenElse -> when {
			/*E-If*/ !condition.isValue() -> condition.mem(memory).evalStep().let {
				copy(condition = it.expr).mem(it.memory)
			}
			/*E-IfTrue*/ condition.isKeyword(Bools.True) -> then.mem(memory)
			/*E-IfFalse*/ condition.isKeyword(Bools.False) -> `else`.mem(memory)
			else -> error("If with invalid condition: $condition")
		}
		is Fix -> when {
			/*E-FixBeta*/ func is Abstraction -> func.body.sub(0, Fix(func)).mem(memory)
			/*E-Fix*/ !func.isValue() -> func.mem(memory).evalStep().let {
				copy(it.expr).mem(it.memory)
			}
			else -> error("cannot apply fix to $func")
		}
		is Variant -> {
			/*E-Variant*/ term.mem(memory).let {
				copy(term = it.expr).mem(it.memory)
			}
		}
		is Case -> when {
			on !is Variant -> error("attempted case on a non variant: $this")
			/*E-CaseVariant*/ on.isValue() -> (cases.find { it.slot == this.on.slot }
				?: error("case does not match!")).term.sub(0, on).mem(memory)
			/*E-Case*/ else -> on.mem(memory).evalStep().let {
				copy(on = it.expr).mem(it.memory)
			}
		}
		is Assign -> when {
			/*E-AssignA*/ ! variable.isValue() -> variable.mem(memory).evalStep().let {
				copy(variable = it.expr).mem(it.memory)
			}
			/*E-AssignB*/ ! term.isValue() -> term.mem(memory).evalStep().let {
				copy(term = it.expr).mem(it.memory)
			}
			/*E-Assign*/ variable is Label -> UntypedNamelessTerm.Unit.mem(memory.toMutableList().apply {
				set(variable.index, term)
			})
			else -> error("cannot assign to $variable")
		}
		is Read -> when {
			/*E-Deref*/ ! variable.isValue() -> variable.mem(memory).evalStep().let {
				copy(variable = it.expr).mem(it.memory)
			}
			/*E-DerefLoc*/ variable is Label -> memory[variable.index].mem(memory)
			else -> error("cannot read ref $this")
		}
		is Ref -> when {
			/*E-RefV*/ term.isValue() -> Label(memory.size).mem(memory + term)
			/*E-Ref*/ else -> term.mem(memory).evalStep().let {
				copy(term = it.expr).mem(it.memory)
			}
		}
		is Label -> TODO()
	}
}

private fun UntypedNamelessTerm.mem(memory: List<UntypedNamelessTerm>) = EvalState(this, memory)

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
		left.isKeyword(Arithmetic.Succ) && right.isValue()
	}
	is KeywordTerm -> keyword.isValue
	is LetBinding -> false
	is Record -> contents.values.all { it.isValue() }
	is RecordProjection -> false
	is IfThenElse -> false
	is Fix -> false
	is UntypedNamelessTerm.Unit -> true
	is Variant -> term.isValue()
	is Case -> false
	is Assign -> false
	is Read -> false
	is Ref -> false
	is Label -> true
}

/**
 * [d] places with [c] cutoff
 */
private fun UntypedNamelessTerm.shift(d: Int, c: Int): UntypedNamelessTerm = when (this) {
	is Variable -> Variable(if (number < c) number else number + d)
	is Abstraction -> Abstraction(body.shift(d, c + 1))
	is App -> App(left.shift(d, c), right.shift(d, c))
	is KeywordTerm -> this
	is LetBinding -> LetBinding(bound.shift(d, c), expression.shift(d, c + 1))
	is Record -> Record(contents.mapValues { it.value.shift(d, c) })
	is RecordProjection -> RecordProjection(record.shift(d, c), project)
	is IfThenElse -> IfThenElse(condition.shift(d, c), then.shift(d, c), `else`.shift(d, c))
	is Fix -> Fix(func.shift(d, c))
	is UntypedNamelessTerm.Unit -> UntypedNamelessTerm.Unit
	is Variant -> Variant(slot, term.shift(d, c))
	is Case -> Case(on.shift(d, c), cases.map { it.copy(term = it.term.shift(d, c + 1)) })
	is Assign -> Assign(variable.shift(d, c), term.shift(d, c))
	is Read -> Read(variable.shift(d, c))
	is Ref -> Ref(term.shift(d, c))
	is Label -> this
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
	is IfThenElse -> IfThenElse(condition.sub(num, replacement), then.sub(num, replacement), `else`.sub(num, replacement))
	is Fix -> Fix(func.sub(num, replacement))
	is UntypedNamelessTerm.Unit -> UntypedNamelessTerm.Unit
	is Variant -> Variant(slot, term.sub(num, replacement))
	is Case -> Case(on.sub(num, replacement), cases.map { it.copy(term = it.term.sub(num + 1, replacement.shift(1, 0))) })
	is Assign -> Assign(variable.sub(num, replacement), term.sub(num, replacement))
	is Read -> Read(variable.sub(num, replacement))
	is Ref -> Ref(term.sub(num, replacement))
	is Label -> this
}

/**
 * destroys references
 */
private fun UntypedNamelessTerm.queryMemory(mem: List<UntypedNamelessTerm>): UntypedNamelessTerm = when (this) {
	is Variable -> this
	is Abstraction -> Abstraction(body.queryMemory(mem))
	is App -> App(left.queryMemory(mem), right.queryMemory(mem))
	is KeywordTerm -> this
	is LetBinding -> LetBinding(bound.queryMemory(mem), expression.queryMemory(mem))
	is Record -> Record(contents.mapValues { it.value.queryMemory(mem) })
	is RecordProjection -> RecordProjection(record.queryMemory(mem), project)
	is IfThenElse -> IfThenElse(condition.queryMemory(mem), then.queryMemory(mem), `else`.queryMemory(mem))
	is Fix -> Fix(func.queryMemory(mem))
	UntypedNamelessTerm.Unit -> this
	is Variant -> Variant(slot, term.queryMemory(mem))
	is Case -> Case(on.queryMemory(mem), cases.map { it.copy(term = it.term.queryMemory(mem)) })
	is Assign -> Assign(variable.queryMemory(mem), term.queryMemory(mem))
	is Read -> Read(variable.queryMemory(mem))
	is Ref -> Ref(term.queryMemory(mem))
	is Label -> mem[index]
}