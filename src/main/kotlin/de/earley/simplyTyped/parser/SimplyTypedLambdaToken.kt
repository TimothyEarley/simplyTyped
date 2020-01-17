package de.earley.simplyTyped.parser

import de.earley.parser.TokenType

//TODO keywords need their own token, not just identifier
enum class SimplyTypedLambdaToken(r: String): TokenType {
	Lambda("λ"),
	// keywords (all hard at the moment, maybe some can be soft later)
	If("if"),
	Then("then"),
	Else("else"),
	True("true"),
	False("false"),
	LetRec("letrec"),
	Let("let"),
	In("in"),
	Unit("unit"),
	// other tokens
	Dot("\\."),
	OpenParen("\\("),
	ClosedParen("\\)"),
	OpenBracket("\\{"),
	ClosedBracket("\\}"),
	Comma(","),
	Colon(":"),
	Arrow("->"),
	Equals("="),
	Number("\\d+"),
	Identifier("\\w+"),
	WS("\\s"),
	EOF("\\z"); //TODO better EOF handling

	override val regex = Regex(r)
}