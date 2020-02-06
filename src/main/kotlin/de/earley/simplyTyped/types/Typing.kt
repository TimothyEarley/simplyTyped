package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.*
import de.earley.simplyTyped.types.Type.*
import de.earley.simplyTyped.types.TypingResult.*


fun TypedNamelessTerm.type(): TypingResult<Type> = type(emptyMap())

typealias TypeEnvironment = Map<Int, Type>
fun TypeEnvironment.inc(): TypeEnvironment = this.mapKeys { (k, _) -> k + 1 }
operator fun TypeEnvironment.plus(type: Type): TypeEnvironment = this.inc() + (0 to type)

private fun TypedNamelessTerm.type(
	variableTypes: TypeEnvironment
): TypingResult<Type> = when (this) {
	is TypedNamelessTerm.Variable -> /*T-Var*/ {
		val type = variableTypes[this.number]
		require(type !is UserType) { "User types should be removed before type checking" }
		type?.let { Ok(it.tryUnfold()) } ?: Error("Variable $number not found in the type env $variableTypes", this)
	}
	is TypedNamelessTerm.Abstraction -> /*T-Abs*/ {
		body.type(variableTypes + argType)
			.map { t2 -> FunctionType(argType, t2) }
	}
	is TypedNamelessTerm.App -> {
		/*AT-App*/
		left.type(variableTypes).flatMap { f ->
			if (f !is FunctionType) Error("Trying to call non-function $f", this)
			else right.type(variableTypes).flatMap { arg ->
				if (arg.isSubtype(f.from)) Ok(f.to) else Error(
					"App mismatch: Function is $f, argument is $arg",
					this
				)
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
		bound.type(variableTypes)
			.flatMap { t1 ->
				expression.type(variableTypes + t1)
			}
	}
	is TypedNamelessTerm.Record -> {
		contents.mapValues { it.value.type(variableTypes) }
			.sequence()
			.map { types ->
				RecordType(types)
			}
	}
	is TypedNamelessTerm.RecordProjection -> {
		record.type(variableTypes)
			.flatMap { recordType ->
				if (recordType !is RecordType) Error("projection on a non record type", this)
				else if (! recordType.types.containsKey(project)) Error("projection label not in record", this)
				else Ok(recordType.types.getValue(project))
			}

	}
	is TypedNamelessTerm.IfThenElse -> {
		/*T-If*/
		condition.type(variableTypes).flatMap { conditionType ->
			if (conditionType != Bool) Error("condition is not a boolean but a $conditionType", this)
			else then.type(variableTypes).flatMap { thenType ->
				`else`.type(variableTypes).flatMap { elseType ->
					if (thenType == elseType) Ok(thenType)
					else Error("then and else differ: $thenType != $elseType", this)
				}
			}
		}
	}
	is TypedNamelessTerm.Fix -> {
		func.type(variableTypes).flatMap { funcType ->
			when {
				funcType !is FunctionType ->
					Error("can only fix on functions, not on $funcType", this)

				funcType.from sameTypeAs funcType.to ->
					Ok(funcType.from)

				else ->
					Error("Fix cannot be applied to (${funcType.from}) -> (${funcType.to})", this)
			}
		}
	}
	is TypedNamelessTerm.Unit -> Ok(Type.Unit)
	is TypedNamelessTerm.TypeDef -> body.type(variableTypes)
	is TypedNamelessTerm.Variant -> term.type(variableTypes).flatMap { termType ->
		val actualType = type.tryUnfold()
		when {
			actualType !is Variant -> Error("variants must be variant type, not $actualType.", this)
			!actualType.variants.containsKey(slot) -> Error(
				"variant has type $actualType, but the key $slot is not found.",
				this
			)
			actualType.variants.getValue(slot) notSameTypeAs termType -> Error(
				"type $termType does not match variant type ${actualType.variants.getValue(slot)}",
				this
			)
			else -> Ok(actualType)
		}
	}
	is TypedNamelessTerm.Case -> on.type(variableTypes).flatMap { onType ->
		if (onType !is Variant) Error("Can only do case of on variant type, not $onType", this)
		else {
			cases.map {
				val slotType = onType.variants[it.slot]
				if (slotType == null) Error("pattern in case is wrong!", this)
				else {
					it.term.type(variableTypes + slotType)
				}
			}.sequence().flatMap { patternTypes ->
				val firstType = patternTypes.first()
				if (patternTypes.all { it sameTypeAs firstType }) Ok(firstType)
				else Error("cases do not have the same type: $patternTypes", this)
			}
		}
	}
	is TypedNamelessTerm.Assign -> /*T-Assign*/variable.type(variableTypes).flatMap { varType ->
		if (varType !is Ref) Error("Cannot assign to non ref type $varType", this)
		else term.type(variableTypes).flatMap { termType ->
			if (termType != varType.of) Error("Incompatible types in assign: ${varType.of} := $termType", this)
			else Ok(Type.Unit)
		}
	}
	is TypedNamelessTerm.Read -> variable.type(variableTypes).flatMap {
		if (it is Ref) Ok(it.of)
		else Error("Cannot dereference a non Ref type: $it", this)
	}
	is TypedNamelessTerm.Ref -> term.type(variableTypes).map {
		/*T-Ref*/ Ref(it)
	}
	is TypedNamelessTerm.Fold -> when (type) {
		/*T-Fld*/ is RecursiveType -> term.type(variableTypes).flatMap { termType ->
			if (termType == type.unfold()) Ok(type)
			else Error("Failed to fold type $termType to $type", this)
		}
		else -> Error("Can only fold recursive types", this)
	}
	is TypedNamelessTerm.Unfold -> when (type) {
		is RecursiveType -> TODO()
		null -> {
			// we need to deduce the type
			term.type(variableTypes.mapValues { (_, v) ->
				if (v is RecursiveType) v.unfold()
				else v
			})
		}
		else -> Error("Can only unfold on recursive types", this)
	}
}

private fun Type.tryUnfold(): Type {
	return if (this is RecursiveType) {
		this.unfold()
	} else {
		this
	}
}