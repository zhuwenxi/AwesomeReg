package com.awesome.regexp;

import java.util.List;
import java.util.Stack;

public class Regexp {
	
	private boolean DEBUG_REGEX = true;
	
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
	
	private String input;
	private int index;
	
	//
	// LR Automata for the regular expression:
	//
	private LrAutomata lrAutomata;
	
	//
	// Abstract syntax tree:
	//
	private AbstractSyntaxTree ast;
	
	private FiniteAutomata dfa;
	
	
	public Regexp(){
		
	}
	
	public Regexp(String regexpString){
		regexpString = regexpFormatFixup(regexpString);
		this.regexpString = regexpString;
		
		//
		// Construct LR-automata:
		// 
		ContextFreeGrammar grammar = new RegularExpressionContextFreeGrammar();
//		System.out.println(grammar);
		this.lrAutomata = new LrAutomata(grammar);
		
		//
		// Generate AST:
		//
		this.ast = this.lrAutomata.parse(regexpString);
		
//		System.out.println("Print AST:");
//		this.ast.root.printSelf(1);
		
		FiniteAutomata NFA = new NondeterministicFiniteAutomata(ast);
		this.dfa = new DeterministicFiniteAutomata(NFA);
		
	}
	
	public String match(String input) {
		this.input = input;
		this.index = 0;
		
		String ret = "";
		
		FiniteAutomataState state0 = this.dfa.start;
		FiniteAutomataState state = state0;
		
		Stack<FiniteAutomataState> stateStack = new Stack<FiniteAutomataState>();
		
		InputSymbol next = nextChar();
		
		while (state != null && next != null) {
			ret = ret + next;
			
			
			stateStack.push(state);
			
			List<FiniteAutomataState> nextStates = this.dfa.transDiag.query(state, next);
			if (nextStates != null) {
				state = this.dfa.transDiag.query(state, next).get(0);
			} else {
				state = null;
			}
			
			
			next = nextChar();
		}
		
		
		while (!this.dfa.isAcceptState(state) && !stateStack.isEmpty()) {
			state = stateStack.pop();
			ret = ret.substring(0, ret.length() - 1);
		}
		
		if (this.dfa.isAcceptState(state)) {
			return ret;
		} else {
			return null;
		}
	}
	
	private InputSymbol nextChar() {
		
		if (this.index < this.input.length()) {
			char ch = this.input.charAt(this.index);
			this.index ++;
			
			return new InputSymbol(ch, ProductionToken.ch);
		} else {
			return null;
		}
		
	}
	
	/*
	 * Formaat fix up to make a regular expression standard one. 
	 * This could be used to extend the abilities of regular expression. For example:
	 * 1. Collection: transform "[0-9]" to "(0|1|2|3|4|5|6|7|8|9)"
	 * 2. Digit: transform "\\d" to "[0-9]"
	 */
	private String regexpFormatFixup(String originRegexp) {
		String fixedupRegexp = originRegexp;
		
		fixedupRegexp = extendedRegexpFixup(fixedupRegexp);
		debugPrint("after extendedRegexpFixup: " + fixedupRegexp);
		fixedupRegexp = collectionFixedup(fixedupRegexp);
		
		return originRegexp;
	}
	
	/*
	 * Digit: transform "\\d" to "[0-9]"
	 */
	private String extendedRegexpFixup(String originRegexp) {
		return originRegexp.indexOf("\\d") >= 0 ? originRegexp.replaceAll("\\\\d", "[0-9]") : originRegexp;
	}
	
	private String collectionFixedup(String originRegexp) {
		String fixedupRegexp = originRegexp;
		return originRegexp;
	}
	
	@Override 
	public String toString(){
		return this.regexpString;
	}
	
	private void debugPrint(String log) {
		if (DEBUG_REGEX) {
			System.out.println(log);
		}
	}
}
