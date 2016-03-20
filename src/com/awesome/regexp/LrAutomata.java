package com.awesome.regexp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class LrAutomata {
	
	private ContextFreeGrammar grammar;
	
	private List<State> states;
	
	private Stack<State> stateStack;
	
	private LinkedList<Character> inputQueue;
	
	private ActionTable action;
	
	private GotoTable transfor;
	
	
	
	
	public LrAutomata(){
		
	}
	
	public LrAutomata(ContextFreeGrammar grammar) {
		
//		this.grammar = grammar;
		
		this.grammar = addDOTPrefix(grammar);
//		System.out.println(this.grammar);
		
		constructStates();
	}
	
	public AbstractSyntaxTree parse(String input) {
		initInputQueue(input);
		
		return null;
	}
	
	private List<State> constructStates() {
		State state0 = new State();
		state0.add(grammar.productions.get(0));
		
		List<State> states = new ArrayList<State>();
		states.add(state0);
		
		int size = states.size();
		
		while (states.size() != size) {
			
		}
		
		return states;
	}
	
	private List<State> closure(List<State> states) {
		return states;
	}
	
	private void initInputQueue(String input){
		
	}
	
	private ContextFreeGrammar addDOTPrefix(ContextFreeGrammar grammar){
		for (Production prod : grammar.productions) {
			List<ProductionToken> body = prod.body;
			
			ProductionToken dotPrefix = new ProductionToken("DOT", true);
			dotPrefix.isDotSymbol = true;
			body.add(0, dotPrefix);
		}
		
		return grammar;
	}
}

class State {
	private ContextFreeGrammar grammar;
	
	public State() {
		
	}
	
	public void add(Production prod) {
		grammar.productions.add(prod);
	}
}

class ActionTable {
	
}

class GotoTable {
	
}
