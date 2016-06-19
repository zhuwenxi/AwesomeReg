package com.awesome.regexp;

import java.util.List;
import java.util.Stack;

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
	
	@Override 
	public String toString(){
		return this.regexpString;
	}
}
