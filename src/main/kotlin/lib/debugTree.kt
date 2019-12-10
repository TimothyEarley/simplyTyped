package lib

import lib.combinators.*
import java.lang.Integer.max

data class Tree<T>(
	val value: T,
	val children: List<Tree<T>>
) {
	override fun toString(): String = toTreeString("", true)

	private fun toTreeString(indent: String, isLast: Boolean): String {
		val myLine = indent + ( if (isLast) "└ " else "├ " ) + value.toString() + "\n"
		val newIndent = indent + if (isLast) "  " else "│ "
		val childLines = children.mapIndexed { i, c ->
			c.toTreeString(newIndent, i == children.lastIndex)
		}
		return myLine + childLines.joinToString(separator = "")
	}
}

fun <Type : TokenType> ParserState<Type>.toTree(): Tree<String> = toTree(chain).first

private fun toTree(chain: List<Parser<*, *>>): Pair<Tree<String>, List<Parser<*, *>>> {
	require(chain.isNotEmpty())
	val top = chain.first()
	if (chain.size == 1) return Tree(top.name, emptyList()) to emptyList()
	val tail = chain.drop(1)
	return when (top) {
		is IsAParser, is MatchParser<*> -> Tree(top.name, emptyList()) to tail
		is LazyParser, is MapParser<*,*,*>, is OrParser /* Or parser does one choice*/, is RenamingParser -> {
			// one child
			val child = toTree(tail)
			Tree(top.name, listOf(child.first)) to child.second
		}
		is PlusParser<*,*,*> /*is FlatMapParser<*,*,*>*/ -> {
			// two children
			val child1 = toTree(chain.drop(1))
			if (child1.second.isEmpty()) {
				// we are not yet at the second branch:
				Tree(top.name, listOf(child1.first)) to emptyList()
			} else {
				val child2 = toTree(child1.second)
				Tree(top.name, listOf(child1.first, child2.first)) to child2.second
			}
		}
		else -> TODO(top.javaClass.toGenericString())
	}
}

fun main() {


	Tree(
		"a",
		listOf(
			Tree(
				"b",
				listOf(
					Tree(
						"c",
						listOf(
							Tree("d", emptyList()),
							Tree("e", emptyList())
						)
					),
					Tree("f", emptyList())
				)
			)
		)
	).also {
		println(it)
	}

}