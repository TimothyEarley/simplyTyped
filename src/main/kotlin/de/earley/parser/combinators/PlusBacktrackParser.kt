package de.earley.parser.combinators

import de.earley.parser.*

//TODO the cache screws with backtracking!
// create a new class for PlusBacktrack

infix fun <Type: TokenType, A, B> Parser<Type, A>.plusBacktrack(next: Parser<Type, B>): Parser<Type, Pair<A, B>> {
	val plainParser = (this + next) //TODO laziness?
	fun backtracked(from: Parser<Type, A>): Parser<Type, Pair<A, B>> {
		return lazy {
			val back = from.backtrack()
			if (back != null) {
				val recursiveBacktracked = backtracked(back)
				((back + next) or recursiveBacktracked).rename("[${back.name}+${next.name}]")
			} else TODO()
		}.rename("backtracking")
	}

	return (plainParser or backtracked(this)).rename("[${this.name}+${next.name}]")
}