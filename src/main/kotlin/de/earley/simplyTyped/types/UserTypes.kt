package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.NamelessCasePattern
import de.earley.simplyTyped.terms.TypedNamelessTerm
import de.earley.simplyTyped.terms.TypedNamelessTerm.*

fun resolveUserTypes(term: TypedNamelessTerm): TypedNamelessTerm = term.resolveUserTypes(emptyMap())

private fun TypedNamelessTerm.resolveUserTypes(userTypes: Map<TypeName, Type>): TypedNamelessTerm = when (this) {
	is Variable -> this
	is Abstraction -> Abstraction(argType.resolve(userTypes, this), body.resolveUserTypes(userTypes))
	is App -> App(left.resolveUserTypes(userTypes), right.resolveUserTypes(userTypes))
	is KeywordTerm -> this
	is LetBinding -> LetBinding(bound.resolveUserTypes(userTypes), expression.resolveUserTypes(userTypes))
	is Record -> Record(contents.mapValues { it.value.resolveUserTypes(userTypes) })
	is RecordProjection -> RecordProjection(record.resolveUserTypes(userTypes), project)
	is IfThenElse -> IfThenElse(condition.resolveUserTypes(userTypes), then.resolveUserTypes(userTypes), `else`.resolveUserTypes(userTypes))
	is Fix -> Fix(func.resolveUserTypes(userTypes))
	TypedNamelessTerm.Unit -> this
	is TypeDef -> body.resolveUserTypes(userTypes + (name to type.resolve(userTypes, this)))
	is Variant -> Variant(slot, term.resolveUserTypes(userTypes), type.resolve(userTypes, this))
	is Case -> Case(on.resolveUserTypes(userTypes), cases.map { NamelessCasePattern(it.slot, it.term.resolveUserTypes(userTypes)) })
	is Assign -> Assign(variable.resolveUserTypes(userTypes), term.resolveUserTypes(userTypes))
	is Read -> Read(variable.resolveUserTypes(userTypes))
	is Ref -> Ref(term.resolveUserTypes(userTypes))
}

private fun Type.resolve(userTypes: Map<TypeName, Type>, context: TypedNamelessTerm): Type = resolveUserType(userTypes, context).recover {
	error(it)
}.type

private fun Type.resolveUserType(userTypes: Map<TypeName, Type>, context: TypedNamelessTerm): TypingResult<Type> = when (this) {
	is Type.UserType -> {
		//TODO at the moment we only have type aliases
		//TODO recursive
		val actualType = userTypes[this.name]
		if (actualType == null) TypingResult.Error("Unknown type $this.", context)
		else TypingResult.Ok(actualType)
	}
	is Type.FunctionType -> from.resolveUserType(userTypes, context).flatMap { fromType ->
		to.resolveUserType(userTypes, context).map {  toType ->
			Type.FunctionType(fromType, toType)
		}
	}
	is Type.RecordType -> types.mapValues { it.value.resolveUserType(userTypes, context) }.sequence().map {
		Type.RecordType(it)
	}
	Type.Nat, Type.Bool, Type.Unit, Type.Top -> TypingResult.Ok(this)
	is Type.Ref -> of.resolveUserType(userTypes, context).map { Type.Ref(it) }
	is Type.Variant -> variants.mapValues { it.value.resolveUserType(userTypes, context) }.sequence().map {
		Type.Variant(it)
	}
}