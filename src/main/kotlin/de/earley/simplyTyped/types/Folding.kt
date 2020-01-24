package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.NamelessCasePattern
import de.earley.simplyTyped.terms.TypedNamelessTerm
import de.earley.simplyTyped.terms.TypedNamelessTerm.*

fun addFolding(namelessTerm: TypedNamelessTerm): TypedNamelessTerm = with(namelessTerm) {
	when (this) {
		is Variable -> this
		is Abstraction -> Abstraction(argType, addFolding(body))
		is App -> App(addFolding(left), addFolding(right))
		is KeywordTerm -> this
		is LetBinding -> LetBinding(addFolding(bound), addFolding(expression))
		is Record -> Record(contents.mapValues { addFolding(it.value) })
		is RecordProjection -> RecordProjection(addFolding(record), project)
		is IfThenElse -> IfThenElse(addFolding(condition), addFolding(then), addFolding(`else`))
		is Fix -> Fix(addFolding(func)) //TODO add folding
		TypedNamelessTerm.Unit -> TypedNamelessTerm.Unit
		is TypeDef -> TypeDef(name, type, addFolding(body))
		is Variant ->
			if (type is Type.RecursiveType) Fold(type, Variant(slot, addFolding(term), type.unfold()))
			else Variant(slot, addFolding(term), type)
		is Case -> {
			Unfold(null, Case(addFolding(on), cases.map { NamelessCasePattern(it.slot, addFolding(it.term)) }))
		}
		is Assign -> Assign(addFolding(variable), addFolding(term))
		is Read -> Read(addFolding(variable))
		is Ref -> Ref(addFolding(term))
		is Fold -> Fold(type, addFolding(term))
		is Unfold -> Unfold(type, addFolding(term))
	}
}

fun Type.RecursiveType.unfold(): Type = body.replace(binder, this)

fun Type.replace(replace: TypeName, replacement: Type): Type = when (this) {
	is Type.FunctionType -> Type.FunctionType(from.replace(replace, replacement), to.replace(replace, replacement))
	is Type.RecordType -> Type.RecordType(types.mapValues { it.value.replace(replace, replacement) })
	Type.Nat -> this
	Type.Bool -> this
	Type.Unit -> this
	Type.Top -> this
	is Type.Ref -> Type.Ref(of.replace(replace, replacement))
	is Type.Variant -> Type.Variant(variants.mapValues { it.value.replace(replace, replacement) })
	is Type.RecursiveType -> when (binder) {
		replace -> this
		else -> Type.RecursiveType(binder, body.replace(replace, replacement))
	}
	is Type.TypeVariable -> when (name) {
		replace -> replacement
		else -> this
	}
	is Type.UserType -> TODO()
}
