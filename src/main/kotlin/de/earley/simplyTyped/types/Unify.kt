package de.earley.simplyTyped.types

fun ConstraintType.unify(): Type {
	val replacement = unify(this)

	val result = solveSuperTypes(replacement)

	require(result.constraints.isEmpty())
	require(result.superTypeConstraints.isEmpty())
	//TODO check free type variables

	return result.type
}

fun ConstraintType.partialUnify(): ConstraintType = this

private fun unify(
	constraintType: ConstraintType
): ConstraintType {
	if (constraintType.constraints.isEmpty()) return constraintType

	val (left, right, cause) = constraintType.constraints.first()
	val tail = ConstraintType(
		constraintType.type, constraintType.constraints.drop(1), constraintType.superTypeConstraints
	)

	fun replAndUnify(replace: Type.TypeVariable, with: Type): ConstraintType {
		return unify(tail.replace(replace.name, with))
	}

	return when {
		left == right -> unify(tail)
		left is Type.TypeVariable -> replAndUnify(left, right)
		right is Type.TypeVariable -> replAndUnify(right, left)
		left is Type.FunctionType && right is Type.FunctionType ->
			unify(tail +
						Constraint(left.from, right.from, cause) +
						Constraint(left.to, right.to, cause)
			)
		left is Type.RecordType && right is Type.RecordType -> {
			unify(tail + left.types.mapNotNull { (name, value) ->
				val other = right.types[name]
				if (other != null) Constraint(
					value,
					other,
					cause
				)
				else null
			})
		}

		else -> TODO()
	}
}


fun solveSuperTypes(constraintType: ConstraintType): ConstraintType {
	val entries = constraintType.superTypeConstraints.entries

	if (entries.isEmpty()) return constraintType

	val (a, b) = entries.first()
	val tail = ConstraintType(
		constraintType.type,
		constraintType.constraints,
		entries.drop(1).map { it.key to it.value }.toMap()
	)

	// a >: b
	return when {
		b isSubtype a -> solveSuperTypes(tail)

		a is Type.RecordType && b is Type.RecordType -> {
			val newConstraints = a.types.entries.fold(emptyMap<Type,Type>()) { acc, (slot, type) ->
				val smaller = b.types.getValue(slot)
				acc meet mapOf(type to smaller)
			}
			solveSuperTypes(tail + newConstraints)
		}

		a is Type.Variant && b is Type.Variant -> {
			val newConstraints = a.variants.entries.fold(emptyMap<Type, Type>()) {
				acc, (slot, type) ->
				val smaller = b.variants.getValue(slot)
				acc meet mapOf(type to smaller)
			}
			solveSuperTypes(tail + newConstraints)
		}

		b is Type.TypeVariable -> {
			//TODO is this wrong?
			solveSuperTypes(tail.replace(b.name, a))
		}
		a is Type.TypeVariable ->
			solveSuperTypes(tail.replace(a.name, b))

		else -> error("super type mismatch: $a >: $b")
	}
}

private operator fun ConstraintType.plus(m: Map<Type, Type>) = ConstraintType(
	type,
	constraints,
	superTypeConstraints meet m
)

private operator fun ConstraintType.plus(constraint: Constraint) = ConstraintType(
	type, constraints + constraint, superTypeConstraints
)

private operator fun ConstraintType.plus(constraint: List<Constraint>) = ConstraintType(
	type, constraints + constraint, superTypeConstraints
)

fun ConstraintType.replace(replace: TypeName, with: Type): ConstraintType =
	ConstraintType(
		type.replace(replace, with),
		constraints.map { it.replace(replace, with) },
		superTypeConstraints
			.entries
			.map {  (k, v) ->
				k.replace(replace, with) to v.replace(replace, with)
			}.fold(emptyMap()) { acc, next ->
				acc meet mapOf(next)
			}
	)

fun Constraint.replace(replace: TypeName, with: Type): Constraint =
	Constraint(
		left.replace(replace, with),
		right.replace(replace, with),
		cause
	)