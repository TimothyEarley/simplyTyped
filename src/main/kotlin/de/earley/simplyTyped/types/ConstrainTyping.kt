package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.Keyword
import de.earley.simplyTyped.terms.TypedNamelessTerm
import de.earley.simplyTyped.terms.VariableName
import java.util.concurrent.atomic.AtomicInteger

fun constraintType(term: TypedNamelessTerm): TypedNamelessTerm {
	val result = term.createConstraints(emptyMap()).recover {
		error(it.msg + " at " + it.element.src)
	}.type

	result.type.unify(result.constraints).also {
		println(it)
	}

	return term
}

private fun Type.unify(constraints: List<Constraint>): Type {
	if (constraints.isEmpty()) return this

	val (left, right, cause) = constraints.first()
	val tail = constraints.drop(1)

	return when {
		left == right -> unify(tail)
		left is ConstraintExpr.TypeExpr && right is ConstraintExpr.TypeExpr -> when {
			left.type is Type.TypeVariable -> this.replace(left.type.name, right.type).unify(tail.map { it.replace(left.type.name, right.type) })

			right.type is Type.TypeVariable -> this.replace(right.type.name, left.type).unify(tail.map { it.replace(right.type.name, left.type) })


			left.type is Type.FunctionType && right.type is Type.FunctionType ->
				unify(tail +
					Constraint(left.type.from, right.type.from, cause) +
					Constraint(left.type.to, right.type.to, cause)
				)

			left.type is Type.RecordType && right.type is Type.RecordType -> {
				unify(tail + left.type.types.mapNotNull { (name, value) ->
					val other = right.type.types[name]
					if (other != null) Constraint(value, other, cause)
					else null
				})
			}

			else -> TODO()
		}

		else -> TODO()
	}
}

private fun Constraint.replace(replace: TypeName, with: Type): Constraint = Constraint(
	left.replace(replace, with),
	right.replace(replace, with),
	cause
)

private fun ConstraintExpr.replace(replace: TypeName, with: Type): ConstraintExpr = when (this) {
	is ConstraintExpr.TypeExpr -> ConstraintExpr.TypeExpr(type.replace(replace, with))
}


private class ConstraintType(
	val type: Type,
	val constraints: List<Constraint>
)
private data class Constraint(
	val left: ConstraintExpr,
	val right: ConstraintExpr,
	val cause: TypedNamelessTerm
) {

	constructor(left: Type, right: Type, cause: TypedNamelessTerm): this(
		ConstraintExpr.TypeExpr(left),
		ConstraintExpr.TypeExpr(right),
		cause
	)
}

private sealed class ConstraintExpr {
	data class TypeExpr(
		val type: Type
	) : ConstraintExpr()
}

object NamePool {
	private val i = AtomicInteger(0)
	fun freeName() = Type.TypeVariable("_" + i.getAndIncrement())
}

private fun TypedNamelessTerm.createConstraints(env: TypeEnvironment): TypingResult<ConstraintType> = when (this) {
	is TypedNamelessTerm.Variable -> env[this.number]?.let {
		TypingResult.Ok(ConstraintType(it, emptyList()))
	} ?: TypingResult.Error("Variable $number not found in the type env $env", this)

	is TypedNamelessTerm.Abstraction -> body.createConstraints(env + argType).map {
		ConstraintType(
			Type.FunctionType(argType, it.type),
			it.constraints
		)
	}
	is TypedNamelessTerm.App -> left.createConstraints(env).flatMap { a ->
		right.createConstraints(env).map { b ->
			val type = NamePool.freeName()
			ConstraintType(
				type,
				a.constraints + b.constraints + Constraint(a.type, Type.FunctionType(b.type, type), this)
			)
		}
	}
	is TypedNamelessTerm.KeywordTerm -> TypingResult.Ok(ConstraintType(when (this.keyword) {
		Keyword.Arithmetic.Succ -> Type.FunctionType(Type.Nat, Type.Nat)
		Keyword.Arithmetic.Pred -> Type.FunctionType(Type.Nat, Type.Nat)
		Keyword.Arithmetic.IsZero -> Type.FunctionType(Type.Nat, Type.Bool)
		Keyword.Arithmetic.Zero -> Type.Nat
		Keyword.Bools.True -> Type.Bool
		Keyword.Bools.False -> Type.Bool
	}, emptyList()))
	is TypedNamelessTerm.LetBinding -> bound.createConstraints(env).flatMap { boundT ->
		expression.createConstraints(env + boundT.type).map { exprT ->
			ConstraintType(
				exprT.type,
				boundT.constraints + exprT.constraints
			)
		}
	}
	is TypedNamelessTerm.Record -> contents.mapValues { it.value.createConstraints(env) }.sequence().map { types ->
		ConstraintType(
			Type.RecordType(types.mapValues { it.value.type }),
			types.flatMap { it.value.constraints }
		)
	}
	is TypedNamelessTerm.RecordProjection -> //TODO how to constrain type this?
		record.createConstraints(env).map { recT ->
			val innerT = NamePool.freeName()
			ConstraintType(
				innerT,
				recT.constraints
			/*
			+ Constraint(
					recT.type,
					Type.RecordType(mapOf(project to innerT)),
					this
				)
			*/
			)
		}
	is TypedNamelessTerm.IfThenElse -> condition.createConstraints(env).flatMap { condT ->
		then.createConstraints(env).flatMap { thenT ->
			`else`.createConstraints(env).map { elseT ->
				ConstraintType(
					thenT.type,
					condT.constraints + thenT.constraints + elseT.constraints + Constraint(condT.type, Type.Bool, this) + Constraint(thenT.type, elseT.type, this)
				)
			}
		}
	}
	is TypedNamelessTerm.Fix -> func.createConstraints(env).map {
		val from = NamePool.freeName()
		val to = NamePool.freeName()
		ConstraintType(
			from,
			it.constraints + Constraint(it.type, Type.FunctionType(from, to), this) + Constraint(from, to, this)
		)
	}
	is TypedNamelessTerm.Unit -> TypingResult.Ok(ConstraintType(Type.Unit, emptyList()))
	is TypedNamelessTerm.TypeDef -> body.createConstraints(env)
	is TypedNamelessTerm.Variant -> term.createConstraints(env).map {
		val type = NamePool.freeName()
		ConstraintType(
			type,
			it.constraints /*
			+ Constraint(type, Type.Variant(mapOf(slot to it.type)), this)
			*/
		)
	}
	is TypedNamelessTerm.Case -> on.createConstraints(env).map {
		val type = NamePool.freeName()
		ConstraintType(
			type,
			it.constraints //TODO add constraints
		)
	}
	is TypedNamelessTerm.Assign -> TODO()
	is TypedNamelessTerm.Read -> TODO()
	is TypedNamelessTerm.Ref -> TODO()
	is TypedNamelessTerm.Fold -> TODO()
	is TypedNamelessTerm.Unfold -> TODO()
}