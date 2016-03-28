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
	
	private List<ProductionToken> symbols;
	
	
	
	
	public LrAutomata(){
		
	}
	
	public LrAutomata(ContextFreeGrammar grammar) {
		
		initSymbolList(grammar);
		
		this.grammar = addDOTPrefix(grammar);
		
		constructStates();
	}
	
	public AbstractSyntaxTree parse(String input) {
		initInputQueue(input);
		
		return null;
	}
	
	private List<ProductionToken> initSymbolList(ContextFreeGrammar grammar) {
		
		this.symbols = new ArrayList<ProductionToken>();
		
		for (Production production : grammar.productions) {
			
			ProductionToken head = production.head;
			List<ProductionToken> body = production.body;
			
			// Scan the head of production, add symbol to symbol list.
			ProductionToken symbol = head;
			
			if (!this.symbols.contains(symbol)) {
				this.symbols.add(symbol);
			}
			
			// Scan the head of production, add symbol to symbol list.
			for (ProductionToken s : body) {
				if (!this.symbols.contains(s)) {
					this.symbols.add(s);
				}
			}
			
		}
		
//		System.out.println(this.symbols);
		
		return this.symbols;
	}
	
	private List<State> constructStates() {
		State state0 = new State();
		state0.add(grammar.productions.get(0));
		
		List<State> states = new ArrayList<State>();
		states.add(state0);
		
		int lastSize = states.size();
		
		do {
			for (State originState : states) {
				for (ProductionToken symbol : this.symbols) {
					State targetState = transfor(originState, symbol);
					
					System.out.println(targetState);
					
					if (!states.contains(targetState)) {
						states.add(targetState);
					}
				}
			}
		}
		while (states.size() != lastSize);
		
		return states;
	}
	
	private State transfor(State origin, ProductionToken symbol) {
		
		State target = new State();
		List<Production> items = null;
		
		for (Production production : origin.getProductions()) {
			List<ProductionToken> body = production.body;
			
			ProductionToken dotSymbol = new ProductionToken("DOT", true);
			dotSymbol.isDotSymbol = true;
			
			int indexOfDot = body.indexOf(dotSymbol);
		}
		
		target.items = closure(items);
		
		return target;
	}
	
	private List<Production> closure(List<Production> productions) {
		return null;
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
	public List<Production> items;
	
	public State() {
		this.items = new ArrayList<>();
	}
	
	public State(List<Production> productions) {
		this();
		this.items = productions;
	}
	
	public void add(Production prod) {
		this.items.add(prod);
	}
	
	public List<Production> getProductions() {
		return this.items;
	}
	
	@Override 
 	public boolean equals(Object o) {
		State that = (State)o;
		
		if(this.items == null || that.items == null) {
			return true;
		}
		
		if (this.items != null && that.items != null) {
			return this.items.equals(that.items);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		if (this.items != null) {
			return this.items.toString();
		} else {
			return null;
		}
		
	}
}

class ActionTable {
	
}

class GotoTable {
	
}
