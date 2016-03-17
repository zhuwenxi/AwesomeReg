package com.awesome.regexp;

import java.util.List;

public class Regexp {
	
	private String regexpString;
	
	private List<Production> produtions;
	
	static private String[] regexpGrammarStrings = {
			"Regexp->{Regexp}|{Concat}",
			"Regexp->{Concat}",
			"Concat->{Concat}{Repeat}",
			"Concat->{Repeat}",
			"Repeat->{Unit}",
			"Repeat->{Unit}*",
			"Unit->({Regexp})",
			"Unit->[a-zA-Z0-9]",
	};
	
	//
	// LR Automata for the regular expression:
	//
	private LrAutomata lrAutomata;
	
	//
	// Abstract syntax tree:
	//
	private AbstractSyntaxTree ast;
	
	
	
	
	public Regexp(){
		
	}
	
	public Regexp(String regexpString){
		this.regexpString = regexpString;
		
		System.out.println(regexpString);
		//
		// Construct LR-automata:
		// 
		ContextFreeGrammar grammar = new ContextFreeGrammar(regexpGrammarStrings);
		this.lrAutomata = new LrAutomata(grammar);
		
		//
		// Generate AST:
		//
		this.ast = this.lrAutomata.parse(regexpString);
		
	}
	
	@Override 
	public String toString(){
		return this.regexpString;
	}
}
