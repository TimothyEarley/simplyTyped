package simplyTyped.parser

import lib.TokenType

//TODO keywords need their own token, not just identifier
enum class SimplyTypedLambdaToken(r: String): TokenType {
	Lambda("λ"),
	Dot("\\."),
	OpenParen("\\("),
	ClosedParen("\\)"),
	Colon(":"),
	Arrow("->"),
	Equals("="),
	Identifier("\\w+"),
	WS("\\s"),
	EOF("\\z"); //TODO better EOF handling

	override val regex = Regex(r)
}