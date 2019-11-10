package lib

/**
 * Output of a parser
 */
sealed class ParserResult<Type:TokenType, out R> {
	data class Ok<Type:TokenType, R>(
		val next: ParserState<Type>,
		val result: R
	) : ParserResult<Type, R>()


	sealed class Error<Type:TokenType>
		: ParserResult<Type, Nothing>() {

		data class LeftRecursion<Type : TokenType>(
			val atToken: Token<Type>?
		) : Error<Type>()

		data class Single<Type : TokenType>(
			val context: ParserContext,
			val expected: Type,
			val expectedValue: String?,
			val actual: Token<Type>?
		) : Error<Type>() {
			override fun toString(): String =
				"Error in context ${context.name}: Expected $expected ${if (expectedValue != null) "with value $expectedValue" else ""} but got $actual"
		}

		data class Multiple<Type : TokenType>(
			val errors: Set<Single<Type>> // all with the same line/col
		) : Error<Type>() {
			override fun toString(): String =
				"Multiple errors: Found ${errors.first().actual} but expected one of: \n${errors.joinToString(separator = "\n", transform = {
					"- In context ${it.context.name}: ${it.expected} ${if (it.expectedValue != null) "with value ${it.expectedValue}" else ""}"
				})}"
		}
	}
}

fun <Type:TokenType, T:Token<Type>, A, B> ParserResult<Type, A>.map(f: (A) -> B): ParserResult<Type, B> = when (this) {
	is ParserResult.Ok -> ParserResult.Ok(next, f(result))
	is ParserResult.Error -> this
}

fun <Type:TokenType, A, B> ParserResult<Type, A>.flatMap(f: (ParserResult.Ok<Type, A>) -> ParserResult<Type, B>): ParserResult<Type, B> = when (this) {
	is ParserResult.Ok -> f(this)
	is ParserResult.Error -> this
}

fun <Type:TokenType, T:Token<Type>, A> ParserResult<Type, A>.flatMapLeft(f: (ParserResult.Error<Type>) -> ParserResult<Type, A>): ParserResult<Type, A> = when (this) {
	is ParserResult.Ok -> this
	is ParserResult.Error -> f(this)
}

private fun ParserResult.Error<*>.getActual() = when (this) {
	is ParserResult.Error.LeftRecursion -> atToken
	is ParserResult.Error.Single -> actual
	is ParserResult.Error.Multiple -> errors.first().actual
}
private val ErrorComparator : Comparator<ParserResult.Error<*>> = Comparator { a, b ->
	val aa = a.getActual()
	val ab = b.getActual()
	compareBy<Token<*>>({it.line}, {it.col}).compare(aa, ab)
}

infix fun <Type:TokenType> ParserResult.Error<Type>.neither(other: ParserResult.Error<Type>): ParserResult.Error<Type> =
	when (ErrorComparator.compare(this, other)) {
		in 1..Int.MAX_VALUE -> this
		0 -> this merge other
		else -> other
	}

private infix fun <Type:TokenType> ParserResult.Error<Type>.merge(other: ParserResult.Error<Type>): ParserResult.Error<Type> =
	ParserResult.Error.Multiple(
		errors = when (this) {
			is ParserResult.Error.LeftRecursion -> emptySet() //TODO think about it
			is ParserResult.Error.Single -> setOf(this)
			is ParserResult.Error.Multiple -> this.errors
		} + when (other) {
			is ParserResult.Error.LeftRecursion -> emptySet()
			is ParserResult.Error.Single -> setOf(other)
			is ParserResult.Error.Multiple -> other.errors
		}
	)

infix fun <Type:TokenType> ParserResult.Error<Type>?.nneither(other: ParserResult.Error<Type>) = when (this) {
	null -> other
	else -> this neither other
}

fun <Type:TokenType, R, A> ParserResult<Type, R>.fold(
	left: (ParserResult.Error<Type>) -> A,
	right: (ParserResult.Ok<Type, R>) -> A ): A = when (this) {
	is ParserResult.Ok -> right(this)
	is ParserResult.Error -> left(this)
}

fun <Type:TokenType, T:Token<Type>, R> ParserResult<Type, R>.orThrow() = fold(
	{
		error(this)
	},
	{it.result}
)