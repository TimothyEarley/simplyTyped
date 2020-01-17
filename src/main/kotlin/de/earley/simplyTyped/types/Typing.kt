package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.*
import de.earley.simplyTyped.types.Type.*
import de.earley.simplyTyped.types.TypingResult.*


fun TypedTerm.type(): TypingResult<Type> = toNameless(emptyMap()).type(emptyMap())

private typealias TypeEnvironment = Map<Int, Type>
private fun TypeEnvironment.inc(): TypeEnvironment = this.mapKeys { (k, _) -> k + 1 }
private operator fun TypeEnvironment.plus(type: Type): TypeEnvironment = this.inc() + (0 to type)

private fun TypedNamelessTerm.type(
	typeEnvironment: TypeEnvironment
): TypingResult<Type> = when (this) {
	is TypedNamelessTerm.Variable -> /*T-Var*/ {
		val type = typeEnvironment[this.number]
		if (type == null) Error("Variable $number not found in the type env $typeEnvironment", this)
		else Ok(type)
	}
	is TypedNamelessTerm.Abstraction -> /*T-Abs*/ {
		body.type(typeEnvironment + argType)
			.map { t2 -> FunctionType(argType, t2) }
	}
	is TypedNamelessTerm.App -> {
		/*T-App*/
		left.type(typeEnvironment).flatMap { f ->
			right.type(typeEnvironment).flatMap { arg ->
				if (f is FunctionType && f.from == arg) Ok(f.to) else Error("App mismatch: Function is $f, argument is $arg", this)
			}
		}
	}
	is TypedNamelessTerm.KeywordTerm -> when (this.keyword) {
		/*T-Succ ' */ Keyword.Arithmetic.Succ -> Ok(FunctionType(Nat, Nat))
		/*T-Pred ' */ Keyword.Arithmetic.Pred -> Ok(FunctionType(Nat, Nat))
		/*T-IsZero ' */ Keyword.Arithmetic.IsZero -> Ok(FunctionType(Nat, Bool))
		/*T-Zero*/ Keyword.Arithmetic.Zero -> Ok(Nat)
		/*T-True*/ Keyword.Bools.True -> Ok(Bool)
		/*T-False*/ Keyword.Bools.False -> Ok(Bool)
	}
	is TypedNamelessTerm.LetBinding -> {
		/*T-Let*/
		bound.type(typeEnvironment)
			.flatMap { t1 ->
				expression.type(typeEnvironment + t1)
			}
	}
	is TypedNamelessTerm.Record -> {
			contents.mapValues { it.value.type(typeEnvironment) }
				.sequence()
				.map { types ->
					RecordType(types)
				}
		}
	is TypedNamelessTerm.RecordProjection -> {
		record.type(typeEnvironment)
			.flatMap { recordType ->
				if (recordType !is RecordType) Error("projection on a non record type", this)
				else if (! recordType.types.containsKey(project)) Error("projection label not in record", this)
				else Ok(recordType.types.getValue(project))
			}

	}
	is TypedNamelessTerm.IfThenElse -> {
		/*T-If*/
		condition.type(typeEnvironment).flatMap { conditionType ->
			if (conditionType != Bool) Error("condition is not a boolean but a $conditionType", this)
			else then.type(typeEnvironment).flatMap { thenType ->
				`else`.type(typeEnvironment).flatMap { elseType ->
					if (thenType == elseType) Ok(thenType)
					else Error("then and else differ: $thenType != $elseType", this)
				}
			}
		}
	}
	is TypedNamelessTerm.Fix -> {
		func.type(typeEnvironment)
			.flatMap { funcType ->
				if (funcType is FunctionType && funcType.from == funcType.to) Ok(funcType.from)
				else Error("Fix cannot be applied to $funcType", this)
			}

	}
	is TypedNamelessTerm.Unit -> Ok(Type.Unit)
}