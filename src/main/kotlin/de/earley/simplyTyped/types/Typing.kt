package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.*


fun TypedTerm.type(): Type? = toNameless().type(emptyMap())

private typealias TypeEnvironment = Map<Int, Type>
private fun TypeEnvironment.inc(): TypeEnvironment = this.mapKeys { (k, _) -> k + 1 }
private operator fun TypeEnvironment.plus(type: Type): TypeEnvironment = this.inc() + (0 to type)

private fun TypedNamelessTerm.type(
	typeEnvironment: TypeEnvironment
): Type? = when (this) {
	is TypedNamelessTerm.Variable -> /*T-Var*/ typeEnvironment[this.number]
	is TypedNamelessTerm.Abstraction -> /*T-Abs*/ {
		val t2 = body.type(typeEnvironment + argType)
		if (t2 != null)	Type.FunctionType(argType, t2)
		else null
	}
	is TypedNamelessTerm.App -> {
		/*T-App*/
		val f = left.type(typeEnvironment)
		val arg = right.type(typeEnvironment)
		if (f is Type.FunctionType && f.from == arg) f.to else null
	}
	is TypedNamelessTerm.KeywordTerm -> when (this.keyword) {
		/*T-Succ ' */ Keyword.Arithmetic.Succ -> Type.FunctionType(Nat, Nat)
		/*T-Pred ' */ Keyword.Arithmetic.Pred -> Type.FunctionType(Nat, Nat)
		/*T-IsZero ' */ Keyword.Arithmetic.IsZero -> Type.FunctionType(Nat, Bool)
		/*T-Zero*/ Keyword.Arithmetic.Zero -> Nat
		/*T-True*/ Keyword.Bools.True -> Bool
		/*T-False*/ Keyword.Bools.False -> Bool
	}
	is TypedNamelessTerm.LetBinding -> {
		/*T-Let*/
		val t1 = bound.type(typeEnvironment)
		if (t1 != null) expression.type(typeEnvironment + t1)
		else null
	}
	is TypedNamelessTerm.Record -> {
		contents.mapValues {
			it.value.type(typeEnvironment)
		}
			.takeIf { !it.containsValue(null) }
			?.let { Type.RecordType(it as Map<VariableName, Type> /* we have check it*/) }

	}
	is TypedNamelessTerm.RecordProjection -> {
		val recordType = record.type(typeEnvironment)
		if (recordType == null || recordType !is Type.RecordType) null
		else recordType.types[project]
	}
	is TypedNamelessTerm.IfThenElse -> {
		/*T-If*/
		condition.type(typeEnvironment)?.takeIf { conditionType ->
			conditionType == Bool
		}?.let {
			then.type(typeEnvironment)?.let { thenType ->
				`else`.type(typeEnvironment)?.takeIf { elseType ->
					thenType == elseType
				}
			}
		}
	}
	is TypedNamelessTerm.Fix -> {
		val funcType = func.type(typeEnvironment)
		if (funcType != null && funcType is Type.FunctionType && funcType.from == funcType.to) funcType.from
		else null
	}
}