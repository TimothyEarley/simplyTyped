package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.*
import de.earley.simplyTyped.types.Type.*
import de.earley.simplyTyped.types.TypingResult.*


fun TypedTerm.type(): TypingResult<Type> = toNameless(emptyMap()).type(emptyMap(), emptyMap())

private typealias TypeEnvironment = Map<Int, Type>
private fun TypeEnvironment.inc(): TypeEnvironment = this.mapKeys { (k, _) -> k + 1 }
private operator fun TypeEnvironment.plus(type: Type): TypeEnvironment = this.inc() + (0 to type)

private fun TypedNamelessTerm.type(
	variableTypes: TypeEnvironment,
	userTypes: Map<TypeName, Type>
): TypingResult<Type> = when (this) {
	is TypedNamelessTerm.Variable -> /*T-Var*/ {
		val type = variableTypes[this.number]
		type?.resolveUserType(userTypes, this)
			?: Error("Variable $number not found in the type env $variableTypes", this)
	}
	is TypedNamelessTerm.Abstraction -> /*T-Abs*/ {
		argType.resolveUserType(userTypes, this).flatMap { arg ->
			body.type(variableTypes + arg, userTypes)
				.map { t2 -> FunctionType(arg, t2) }
		}
	}
	is TypedNamelessTerm.App -> {
		/*T-App*/
		left.type(variableTypes, userTypes).flatMap { f ->
			right.type(variableTypes, userTypes).flatMap { arg ->
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
		bound.type(variableTypes, userTypes)
			.flatMap { t1 ->
				expression.type(variableTypes + t1, userTypes)
			}
	}
	is TypedNamelessTerm.Record -> {
			contents.mapValues { it.value.type(variableTypes, userTypes) }
				.sequence()
				.map { types ->
					RecordType(types)
				}
		}
	is TypedNamelessTerm.RecordProjection -> {
		record.type(variableTypes, userTypes)
			.flatMap { recordType ->
				if (recordType !is RecordType) Error("projection on a non record type", this)
				else if (! recordType.types.containsKey(project)) Error("projection label not in record", this)
				else Ok(recordType.types.getValue(project))
			}

	}
	is TypedNamelessTerm.IfThenElse -> {
		/*T-If*/
		condition.type(variableTypes, userTypes).flatMap { conditionType ->
			if (conditionType != Bool) Error("condition is not a boolean but a $conditionType", this)
			else then.type(variableTypes, userTypes).flatMap { thenType ->
				`else`.type(variableTypes, userTypes).flatMap { elseType ->
					if (thenType == elseType) Ok(thenType)
					else Error("then and else differ: $thenType != $elseType", this)
				}
			}
		}
	}
	is TypedNamelessTerm.Fix -> {
		func.type(variableTypes, userTypes)
			.flatMap { funcType ->
				if (funcType is FunctionType && funcType.from == funcType.to) Ok(funcType.from)
				else Error("Fix cannot be applied to $funcType", this)
			}

	}
	is TypedNamelessTerm.Unit -> Ok(Type.Unit)
	is TypedNamelessTerm.TypeDef -> body.type(variableTypes, userTypes + (name to type))
	is TypedNamelessTerm.Variant -> term.type(variableTypes, userTypes).flatMap { termType ->
		type.resolveUserType(userTypes, this).flatMap { actualType ->
			if (actualType !is Variant) Error("variants must be variant type, not $actualType.", this)
			else {
				if (!actualType.variants.containsKey(slot)) Error(
					"variant has type $actualType, but the key $actualType is not found.",
					this
				)
				val checkType = actualType.variants.getValue(slot)
				if (checkType != termType) Error(
					"type $termType does not match variant type $checkType",
					this
				)
				Ok(actualType)
			}
		}
	}
	is TypedNamelessTerm.Case -> on.type(variableTypes, userTypes).flatMap { onType ->
		if (onType !is Variant) Error("Can only do case of on variant type, not $onType", this)
		else {
			cases.map {
				val slotType = onType.variants[it.slot]
				if (slotType == null) Error("pattern in case is wrong!", this)
				else {
					it.term.type(variableTypes + slotType, userTypes)
				}
			}.sequence().flatMap { patternTypes ->
				val firstType = patternTypes.first()
				if (patternTypes.all { it == firstType }) Ok(firstType)
				else Error("cases do not have the same type: $patternTypes", this)
			}
		}
	}
}

private fun Type.resolveUserType(userTypes: Map<TypeName, Type>, context: TypedNamelessTerm): TypingResult<Type> = when (this) {
	is UserType -> {
		//TODO at the moment we only have type aliases
		//TODO recursive
		val actualType = userTypes[this.name]
		if (actualType == null) Error("Unknown type $this.", context)
		else Ok(actualType)
	}
	else -> Ok(this)
}