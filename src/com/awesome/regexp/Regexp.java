package com.awesome.regexp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private static LrAutomata cachedLrAutomata;
	
	//
	// Abstract syntax tree:
	//
	private AbstractSyntaxTree ast;
	
    /*
     * Deterministic Finite Automata:
     */
	private FiniteAutomata dfa;
	
	/*
	 * DFA cache.
	 */
	private static Map<String, FiniteAutomata> dfaCache;
	
	static {
		dfaCache = new HashMap<String, FiniteAutomata>();
	}
	
	
	public Regexp(){
		
	}
	
	public Regexp(String regexpString){
		// Set all options as "ON".
		// Config.setAllOptons(true);
		
		// 
		// DFA cache look up. If look up hit, it's not necessary to re-calculate the DFA.
		//
		if (dfaCache.containsKey(regexpString)) {
			this.dfa = dfaCache.get(regexpString);
			return;
		}
		
		//
		// Construct LR-automata for parsing a regexp, such as "(a|b)*abb".
		// 
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.start(Statistic.Tag.Automata);
			}
			
		});
		
		ContextFreeGrammar grammar = new RegularExpressionContextFreeGrammar();
		Logger.tprint(Config.CONTEXT_FREE_GRAMMAR_VERBOSE, grammar, "Context-free grammar");
		
		if (Regexp.cachedLrAutomata == null) {
			Regexp.cachedLrAutomata = new LrAutomata(grammar);
		}
//		this.lrAutomata = Regexp.cachedLrAutomata;
		
		
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.pause(Statistic.Tag.Automata);
			}
			
		});
		
		// Original input regexp string.
		this.regexpString = regexpString;
		Logger.tprint(Config.REGEXP_VERBOSE, regexpString, "Input parameter passed to Regexp()");		
		
		// Do some modification to the input regexp string. i.e. convert [0-9] to (0|1|2|3|4|5|6|7|8|9)
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.start(Statistic.Tag.FormatFixup);
			}
			
		});
		
		regexpString = regexpFormatFixup(regexpString);		
		Logger.tprint(Config.REGEXP_VERBOSE, regexpString, "After format fixup");
		
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.pause(Statistic.Tag.FormatFixup);
			}
			
		});
		
		//
		// Generate AST.
		//
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.start(Statistic.Tag.AST);
			}
			
		});
		
		this.ast = Regexp.cachedLrAutomata.parse(regexpString);
		
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.pause(Statistic.Tag.AST);
			}
			
		});
		
		Logger.tprint(Config.AST_VERBOSE, new DebugCode() {

			@Override
			public void code() {
				ast.root.printSelf(0);
			}
			
		}, "AST for regular-expression");
		
		// 
		// Generate NFA from AST.
		// 
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.start(Statistic.Tag.NFA);
			}
			
		});
		
		FiniteAutomata NFA = new NondeterministicFiniteAutomata(ast);
		Logger.tprint(Config.NFA_VERBOSE, NFA.transDiag, "NFA transform diagram");
		
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.pause(Statistic.Tag.NFA);
			}
			
		});
		
		//
		// Generate DFA from NFA.
		// 
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.start(Statistic.Tag.DFA);
			}
			
		});
		
		this.dfa = new DeterministicFiniteAutomata(NFA);
		// Update the DFA cache.
		Regexp.dfaCache.put(this.regexpString, this.dfa);
		Logger.tprint(Config.DFA_VERBOSE, dfa.transDiag, "DFA transform diagram");
		
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.pause(Statistic.Tag.DFA);
			}
			
		});
	}
	
	public String match(String input) {
		Debug.run(Config.STAT, new DebugCode() {

			@Override
			public void code() {
				Statistic.start(Statistic.Tag.FuncMatch);
			}
			
		});
		
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
			
			Debug.run(Config.STAT, new DebugCode() {

				@Override
				public void code() {
					Statistic.pause(Statistic.Tag.FuncMatch);
				}
				
			});
			
			return ret;
		} else {
			
			Debug.run(Config.STAT, new DebugCode() {

				@Override
				public void code() {
					Statistic.pause(Statistic.Tag.FuncMatch);
				}
				
			});
			
			return null;
		}
	}
	
	public FiniteAutomata getDfa() {
		return this.dfa;
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
		fixedupRegexp = plusQuantifierFixup(fixedupRegexp);
		
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
	
	/*
	 * X+: X, one or more times. i.e. transform "(ab)+" to "(ab)(ab)*"
	 */
	private String plusQuantifierFixup(String originRegexp) {
		
		// This stack is to help find the corresponding left-parentheses.
		Stack<Character> stack = new Stack<>();
		
		int plusQuantifierIndex = originRegexp.indexOf("+");
		char charBeforePlus = 0;
		
		while (plusQuantifierIndex != -1) {
			charBeforePlus = originRegexp.charAt(plusQuantifierIndex - 1);
			
			if (charBeforePlus == ')') {
				int leftParenthesesIndex = -1;
				
				for (int i = plusQuantifierIndex - 1; i >= 0; i --) {
					char currentChar = originRegexp.charAt(i);
					if (currentChar == ')') {
						stack.push(currentChar);
					} else if (currentChar == '(') {
						if (!stack.isEmpty() &&  stack.peek() == ')') {
							stack.pop();
						}
					}
					
					if (stack.isEmpty()) {
						leftParenthesesIndex = i;
						break;
					}
				}
				
				if (leftParenthesesIndex != -1) {
					originRegexp = originRegexp.substring(0, plusQuantifierIndex) + originRegexp.substring(leftParenthesesIndex, plusQuantifierIndex) + "*" + originRegexp.substring(plusQuantifierIndex + 1);
				}
			} else if (charBeforePlus == ']') {
				int leftBracketIndex = -1;
				
				for (int i = plusQuantifierIndex - 1; i >= 0; i --) {
					char currentChar = originRegexp.charAt(i);
					if (currentChar == ']') {
						stack.push(currentChar);
					} else if (currentChar == '[') {
						if (!stack.isEmpty() &&  stack.peek() == ']') {
							stack.pop();
						}
					}
					
					if (stack.isEmpty()) {
						leftBracketIndex = i;
						break;
					}
				}
				
				if (leftBracketIndex != -1) {
					originRegexp = originRegexp.substring(0, plusQuantifierIndex) + originRegexp.substring(leftBracketIndex, plusQuantifierIndex) + "*" + originRegexp.substring(plusQuantifierIndex + 1);
				}
			} else {
				originRegexp = originRegexp.substring(0, plusQuantifierIndex) + charBeforePlus + "*" + originRegexp.substring(plusQuantifierIndex + 1);
			}
			
			plusQuantifierIndex = originRegexp.indexOf("+");
		}
		
		return originRegexp;
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