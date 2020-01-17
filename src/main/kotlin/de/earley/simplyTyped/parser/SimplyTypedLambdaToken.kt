package de.earley.simplyTyped.parser

import de.earley.parser.TokenType

//TODO keywords need their own token, not just identifier
enum class SimplyTypedLambdaToken(r: String): TokenType {
	Lambda("λ"),
	Dot("\\."),
	OpenParen("\\("),
	ClosedParen("\\)"),
	OpenBracket("\\{"),
	ClosedBracket("\\}"),
	Comma(","),
	Colon(":"),
	Arrow("->"),
	Equals("="),
	Identifier("\\w+"),
	WS("\\s"),
	EOF("\\z"); //TODO better EOF handling

	override val regex = Regex(r)
}