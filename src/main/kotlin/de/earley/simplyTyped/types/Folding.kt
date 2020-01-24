package de.earley.simplyTyped.types

import de.earley.parser.SourcePosition
import de.earley.simplyTyped.terms.NamelessCasePattern
import de.earley.simplyTyped.terms.TypedNamelessTerm
import de.earley.simplyTyped.terms.TypedNamelessTerm.*

fun addFolding(namelessTerm: TypedNamelessTerm): TypedNamelessTerm = with(namelessTerm) {
	when (this) {
		is Variable -> this
		is Abstraction -> Abstraction(argType, addFolding(body), src)
		is App -> App(addFolding(left), addFolding(right), src)
		is KeywordTerm -> this
		is LetBinding -> LetBinding(addFolding(bound), addFolding(expression), src)
		is Record -> Record(contents.mapValues { addFolding(it.value) }, src)
		is RecordProjection -> RecordProjection(addFolding(record), project, src)
		is IfThenElse -> IfThenElse(addFolding(condition), addFolding(then), addFolding(`else`), src)
		is Fix -> Fix(addFolding(func), src) //TODO add folding
		is TypedNamelessTerm.Unit -> TypedNamelessTerm.Unit(src)
		is TypeDef -> TypeDef(name, type, addFolding(body), src)
		is Variant ->
			if (type is Type.RecursiveType) Fold(type, Variant(slot, addFolding(term), type.unfold(), src), SourcePosition.Synth)
			else Variant(slot, addFolding(term), type, src)
		is Case -> {
			Unfold(null, Case(addFolding(on), cases.map { NamelessCasePattern(it.slot, addFolding(it.term)) }, src), SourcePosition.Synth)
		}
		is Assign -> Assign(addFolding(variable), addFolding(term), src)
		is Read -> Read(addFolding(variable), src)
		is Ref -> Ref(addFolding(term), src)
		is Fold -> Fold(type, addFolding(term), src)
		is Unfold -> Unfold(type, addFolding(term), src)
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
