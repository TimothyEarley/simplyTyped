package de.earley.simplyTyped.types

import de.earley.simplyTyped.terms.TypedNamelessTerm
import java.lang.Error

sealed class TypingResult<out T> {
	data class Ok<T>(val type: T): TypingResult<T>()
	data class Error(val msg: String, val element: TypedNamelessTerm): TypingResult<Nothing>()

	fun <B> map(f: (T) -> B): TypingResult<B> = when (this) {
		is Ok -> Ok(f(type))
		is Error -> this
	}

	fun <B> flatMap(f: (T) -> TypingResult<B>): TypingResult<B> = when (this) {
		is Ok -> f(type)
		is Error -> this
	}
}

fun <T> TypingResult<T>.recover(f: (TypingResult.Error) -> T): TypingResult.Ok<T> = when (this) {
	is TypingResult.Ok -> this
	is TypingResult.Error -> TypingResult.Ok(f(this))
}

fun <T> Collection<TypingResult<T>>.sequence(): TypingResult<Collection<T>> = TODO()
fun <K, T> Map<K, TypingResult<T>>.sequence(): TypingResult<Map<K, T>> {
	val (errors, oks) = entries.partition { it.value is TypingResult.Error }
	return if (errors.isEmpty()) TypingResult.Ok(oks.map { it.key to (it.value as TypingResult.Ok).type }.toMap() )
	else errors.first().value as TypingResult.Error
}