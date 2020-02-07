package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.Keyword
import de.earley.simplyTyped.terms.TypedNamelessTerm
import java.util.concurrent.atomic.AtomicInteger

class ConstraintType(
	val type: Type,
	val constraints: List<Constraint>,
	/**
	 * Key is a supertype of value
	 */
	val superTypeConstraints: Map<Type, Type>
)

data class Constraint(
	val left: Type,
	val right: Type,
	val cause: TypedNamelessTerm
)

object NamePool {
	private val i = AtomicInteger(0)
	fun freeName() =
		Type.TypeVariable("_" + i.getAndIncrement())
}

fun TypedNamelessTerm.createConstraints(env: TypeEnvironment): TypingResult<ConstraintType> = when (this) {
	is TypedNamelessTerm.Variable -> env[this.number]?.let {
		TypingResult.Ok(
			ConstraintType(
				it,
				emptyList(),
				emptyMap()
			)
		)
	} ?: TypingResult.Error(
		"Variable $number not found in the type env $env",
		this
	)

	is TypedNamelessTerm.Abstraction -> body.createConstraints(env + argType).map {
		ConstraintType(
			Type.FunctionType(argType, it.type),
			it.constraints,
			it.superTypeConstraints
		)
	}
	is TypedNamelessTerm.App -> left.createConstraints(env).flatMap { a ->
		right.createConstraints(env).map { b ->
			val type = NamePool.freeName()
			ConstraintType(
				type,
				a.constraints + b.constraints + Constraint(
					a.type,
					Type.FunctionType(b.type, type),
					this
				),
				a.superTypeConstraints meet b.superTypeConstraints
			).partialUnify()
		}
	}
	is TypedNamelessTerm.KeywordTerm -> TypingResult.Ok(
		ConstraintType(
			when (this.keyword) {
				Keyword.Arithmetic.Succ -> Type.FunctionType(
					Type.Nat,
					Type.Nat
				)
				Keyword.Arithmetic.Pred -> Type.FunctionType(
					Type.Nat,
					Type.Nat
				)
				Keyword.Arithmetic.IsZero -> Type.FunctionType(
					Type.Nat,
					Type.Bool
				)
				Keyword.Arithmetic.Zero -> Type.Nat
				Keyword.Bools.True -> Type.Bool
				Keyword.Bools.False -> Type.Bool
			}, emptyList(), emptyMap()
		)
	)

	is TypedNamelessTerm.LetBinding -> bound.createConstraints(env).flatMap { boundT ->
		expression.createConstraints(env + boundT.type).map { exprT ->
			ConstraintType(
				exprT.type,
				boundT.constraints + exprT.constraints,
				boundT.superTypeConstraints meet exprT.superTypeConstraints
			).partialUnify()
		}
	}
	is TypedNamelessTerm.Record -> contents.mapValues { it.value.createConstraints(env) }.sequence().map { types ->
		ConstraintType(
			Type.RecordType(types.mapValues { it.value.type }),
			types.flatMap { it.value.constraints },
			types.entries.fold(emptyMap()) { acc, it ->
				acc meet it.value.superTypeConstraints
			}
		).partialUnify()
	}
	is TypedNamelessTerm.RecordProjection -> //TODO how to constrain type this?
		record.createConstraints(env).map { recT ->
			val resultT = NamePool.freeName()
			val recTVar = NamePool.freeName()
			ConstraintType(
				resultT,
				recT.constraints + Constraint(
					recTVar,
					recT.type,
					this
				),
				recT.superTypeConstraints + mapOf(
					recTVar to Type.RecordType(
						mapOf(project to resultT)
					)
				)
			)
		}
	is TypedNamelessTerm.IfThenElse -> condition.createConstraints(env).flatMap { condT ->
		then.createConstraints(env).flatMap { thenT ->
			`else`.createConstraints(env).map { elseT ->
				ConstraintType(
					thenT.type,
					condT.constraints + thenT.constraints + elseT.constraints + Constraint(
						condT.type,
						Type.Bool,
						this
					) + Constraint(
						thenT.type,
						elseT.type,
						this
					),
					condT.superTypeConstraints meet thenT.superTypeConstraints meet elseT.superTypeConstraints
				).partialUnify()
			}
		}
	}
	is TypedNamelessTerm.Fix -> func.createConstraints(env).map {
		val from = NamePool.freeName()
		val to = NamePool.freeName()
		ConstraintType(
			from,
			it.constraints + Constraint(
				it.type,
				Type.FunctionType(from, to),
				this
			) + Constraint(from, to, this),
			it.superTypeConstraints
		)
	}
	is TypedNamelessTerm.Unit ->
		TypingResult.Ok(
			ConstraintType(
				Type.Unit,
				emptyList(),
				emptyMap()
			)
		)
	is TypedNamelessTerm.TypeDef -> body.createConstraints(env)
	is TypedNamelessTerm.Variant -> term.createConstraints(env).map {
		val type = NamePool.freeName()
		ConstraintType(
			type,
			it.constraints,
			it.superTypeConstraints + mapOf(
				type to Type.Variant(
					mapOf(slot to it.type)
				)
			)
		)
	}
	is TypedNamelessTerm.Case -> on.createConstraints(env).flatMap { onT ->
		cases.map { it.slot to it.term.createConstraints(env) }.toMap()
			.sequence().map { casesT ->
				val type = NamePool.freeName()
				val variantT = Type.Variant(casesT.map {
					it.key to NamePool.freeName() //TODO this is the type that should be passed in the env
				}.toMap())
				ConstraintType(
					type,
					onT.constraints + casesT.flatMap { it.value.constraints } +
							// the case branches match each other and the return type
							(casesT.values.map { it.type } + type).zipWithNext().map { (a, b) -> Constraint(a, b, this) },
					onT.superTypeConstraints meet mapOf(onT.type to variantT)
				)
		}

	}
	is TypedNamelessTerm.Assign -> TODO()
	is TypedNamelessTerm.Read -> TODO()
	is TypedNamelessTerm.Ref -> TODO()
	is TypedNamelessTerm.Fold -> TODO()
	is TypedNamelessTerm.Unfold -> TODO()
}