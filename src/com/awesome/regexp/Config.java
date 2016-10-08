package com.awesome.regexp;

public class Config {
	
	public static boolean RegexpVerbose = false;
	
	public static boolean ContextFreeGrammarVerbose = false;
	
	public static boolean LrAutomataVerbose = false;
	
	public static boolean ActionTableVerbose = false;
	
	public static boolean GotoTableVerbose = false;
	
	public static boolean AstVerbose = false;
	
	public static boolean NfaVerbose = false;
	
	public static boolean DfaVerbose = false;
	
	public static boolean Stat = true;
	
	public static void setAllOptons(boolean option) {
		Config.RegexpVerbose = option;
		Config.ContextFreeGrammarVerbose = option;
		Config.LrAutomataVerbose = option;
		Config.ActionTableVerbose = option;
		Config.GotoTableVerbose = option;
		Config.AstVerbose = option;
		Config.NfaVerbose = option;
		Config.DfaVerbose = option;
		Config.Stat = option;
	}
}
