package de.earley.simplyTyped.parser

import de.earley.parser.TokenType

//TODO keywords need their own token, not just identifier
enum class SimplyTypedLambdaToken(r: String): TokenType {
	Lambda("λ"),
	// keywords (all hard at the moment, maybe some can be soft later)
	If("\\bif\\b"),
	Then("\\bthen\\b"),
	Else("\\belse\\b"),
	True("\\btrue\\b"),
	False("\\bfalse\\b"),
	Succ("\\bsucc\\b"),
	Pred("\\bpred\\b"),
	IsZero("\\biszero\\b"),
	LetRec("\\bletrec\\b"),
	Let("\\blet\\b"),
	In("\\bin\\b"),
	Unit("\\bunit\\b"),
	TypeDef("\\btype\\b"),
	RecTypeDef("\\brectype\\b"),
	Case("\\bcase\\b"),
	Ref("\\bref\\b"),
	// other tokens
	Dot("\\."),
	OpenParen("\\("),
	ClosedParen("\\)"),
	OpenBracket("\\{"),
	ClosedBracket("\\}"),
	OpenAngle("<"),
	ClosedAngle(">"),
	Pipe("\\|"),
	Comma(","),
	Colon(":"),
	SemiColon(";"),
	Exclamation("!"),
	Arrow("->"),
	Equals("="),
	Number("\\d+"),
	Identifier("\\w+"),
	WS("\\s"),
	COMMENT("#.*?(\n|$)"),
	EOF("\\z"); //TODO better EOF handling

	override val regex = Regex(r)
}