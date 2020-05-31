package de.earley.simplyTyped.parser

import de.earley.parser.TokenType

//TODO keywords need their own token, not just identifier
enum class SimplyTypedLambdaToken(r: String, override val symbol : String): TokenType {
	Lambda("λ", "λ"),
	// keywords (all hard at the moment, maybe some can be soft later)
	If("\\bif\\b", "if"),
	Then("\\bthen\\b", "then"),
	Else("\\belse\\b", "else"),
	True("\\btrue\\b", "true"),
	False("\\bfalse\\b", "false"),
	Succ("\\bsucc\\b", "succ"),
	Pred("\\bpred\\b", "pred"),
	IsZero("\\biszero\\b", "iszero"),
	LetRec("\\bletrec\\b", "letrec"),
	Let("\\blet\\b", "let"),
	In("\\bin\\b", "in"),
	Unit("\\bunit\\b", "unit"),
	TypeDef("\\btype\\b", "type"),
	RecTypeDef("\\brectype\\b", "rectype"),
	Case("\\bcase\\b", "case"),
	Ref("\\bref\\b", "ref"),
	// other tokens
	Dot("\\.", "."),
	OpenParen("\\(", "("),
	ClosedParen("\\)", ")"),
	OpenBracket("\\{", "{"),
	ClosedBracket("\\}", "}"),
	OpenAngle("<", "<"),
	ClosedAngle(">", ">"),
	Pipe("\\|", "|"),
	Comma(",", ","),
	Colon(":", ":"),
	SemiColon(";", ";"),
	Exclamation("!", "!"),
	Arrow("->", "->"),
	Equals("=", "="),
	Number("\\d+", "[number]"),
	Identifier("\\w+", "[identifier]"),
	WS("\\s", "[WS]"),
	COMMENT("#.*?(\n|$)", "[COMMENT]"),
	EOF("\\z", "[EOF]"); //TODO better EOF handling

	override val regex = Regex(r)
}