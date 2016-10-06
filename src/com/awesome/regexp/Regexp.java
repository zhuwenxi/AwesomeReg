package com.awesome.regexp;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Regexp {
	private String regexpString;
		
	/*
	 * The context-free grammar which describes regular-expression listed below.
	 * AwesomeReg supports the 3 very basic form of the typical regular-expression:
	 * 1. Alter: a|b
	 * 2. Concat: ab
	 * 3. Repeat: *
	 */
//	static private String[] regexpGrammarStrings = {
//			"Regexp->{Regexp}|{Concat}",
//			"Regexp->{Concat}",
//			"Concat->{Concat}{Repeat}",
//			"Concat->{Repeat}",
//			"Repeat->{Unit}",
//			"Repeat->{Unit}*",
//			"Unit->({Regexp})",
//			"Unit->Char",
//	};
	
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
	
    /*
     * Deterministic Finite Automata:
     */
	private FiniteAutomata dfa;
	
	
	public Regexp(){
		
	}
	
	public Regexp(String regexpString){
		// Set all options as "ON".
//		Config.setAllOptons(true);
//		Config.setAllOptons(false);
		
		this.regexpString = regexpString;
		Logger.tprint(Config.RegexpVerbose, regexpString, "Input parameter passed to Regexp()");		
		
		regexpString = regexpFormatFixup(regexpString);		
		Logger.tprint(Config.RegexpVerbose, regexpString, "After format fixup");
		
		//
		// Construct LR-automata for parsing a regexp, such as "(a|b)*abb".
		// 
		ContextFreeGrammar grammar = new RegularExpressionContextFreeGrammar();
		Logger.tprint(Config.ContextFreeGrammarVerbose, grammar, "Context-free grammar");
		
		this.lrAutomata = new LrAutomata(grammar);
		
		
		//
		// Generate AST.
		//
		this.ast = this.lrAutomata.parse(regexpString);
		Logger.tprint(Config.AstVerbose, new DebugCode() {

			@Override
			public void code() {
				ast.root.printSelf(0);
			}
			
		}, "AST for regular-expression");
		
		// 
		// Generate NFA from AST.
		// 
		FiniteAutomata NFA = new NondeterministicFiniteAutomata(ast);
		Logger.tprint(Config.NfaVerbose, NFA.transDiag, "NFA transform diagram");
		
		//
		// Generate DFA from NFA.
		// 
		this.dfa = new DeterministicFiniteAutomata(NFA);
		Logger.tprint(Config.DfaVerbose, dfa.transDiag, "DFA transform diagram");
		
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
			} else if (this.dfa.isAcceptState(state)){
				state = null;
			} else {
				state = state0;
				stateStack = new Stack<FiniteAutomataState>();
				ret = "";
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
		fixedupRegexp = collectionFixedup(fixedupRegexp);
		
		return fixedupRegexp;
	}
	
	/*
	 * Digit: transform "\\d" to "[0-9]"
	 */
	private String extendedRegexpFixup(String originRegexp) {
		String fixedupRegexp = originRegexp;
		int index = fixedupRegexp.indexOf("\\d");
		
		while (index >= 0) {
			fixedupRegexp = fixedupRegexp.substring(0, index) + "[0-9]" + fixedupRegexp.substring(index + 2);
			
			index = fixedupRegexp.indexOf("\\d");
		}
		return fixedupRegexp;
	}
	
	/*
	 * Collection: transform "[0-9]" to "(0|1|2|3|4|5|6|7|8|9)"
	 */
	private String collectionFixedup(String originRegexp) {
		String fixedupRegexp = originRegexp;
		int collectionStart = 0;
		int collectionEnd = 0;
		boolean isInCollection = false;
		boolean hasCollectionInRegexp = fixedupRegexp.indexOf('[') >= 0;
		
		while (hasCollectionInRegexp) {
			for (int i = 0; i < fixedupRegexp.length(); i ++) {
				char current = fixedupRegexp.charAt(i);
				
				if (current == '[') {
					assert isInCollection == false;
					isInCollection = true;
					collectionStart = i + 1;
					
				} else if (current == ']'){
					assert isInCollection == true;
					isInCollection = false;
					collectionEnd = i;
					
					String collectionStr = fixedupRegexp.substring(collectionStart, collectionEnd);
					String replaceStr = collectionToStandardRegexp(collectionStr);
					fixedupRegexp = fixedupRegexp.substring(0, collectionStart - 1) + replaceStr + fixedupRegexp.substring(collectionEnd + 1);
				} 
			}
			
			hasCollectionInRegexp = fixedupRegexp.indexOf('[') >= 0;
		}
		
		return fixedupRegexp;
	}
	
	private String collectionToStandardRegexp(String collectionStr) {
		ArrayList<Character> collection = new ArrayList<>();
		for (int i = 0; i < collectionStr.length(); i ++) {
			char current = collectionStr.charAt(i);
			
			if (current == '-') {
				if (i != 0 && i != collectionStr.length() - 1) {
					char start = collectionStr.charAt(i - 1);
					char end = collectionStr.charAt(i + 1);
					if (start < end) {
						while (start <= end) {
							if (!collection.contains(start)) {
								collection.add(start);
							}
							start ++;
						}
					} else {
						// Not a valid collection: start < end.
						assert false;
					}
				} else {
					// Not a valid collection: "-" appears at the head or tail of the collection.
					assert false;
				}
			} else {
				if (!collection.contains(current)) {
					collection.add(current);
				}
			}
		}
		
		// Make up the format string, e.ge. convert the collection "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]" to "(0|1|2|3|4|5|6|7|8|9)"
		collectionStr = "";
				
		for (char c : collection) {
			if (collectionStr.length() == 0) {
				collectionStr += c;
			} else {
				collectionStr = collectionStr + "|" + c;
			}
		}
				
		collectionStr = "(" + collectionStr + ")";
				
		return collectionStr;
	}
	
	@Override 
	public String toString(){
		return this.regexpString;
	}
}
