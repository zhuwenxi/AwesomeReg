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
		
		this.states = constructStates(this.grammar);
		
		System.out.println(this.states);
		System.out.println(this.states.size());
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
		
		// Dirty implementation. Remove the symbol "Regexp'"
		this.symbols.remove(0);
		
//		System.out.println(this.symbols);
		
		return this.symbols;
	}
	
	private List<State> constructStates(ContextFreeGrammar grammar) {
		State state0 = new State();
		state0.add(grammar.productions.get(0));
		state0 = closure(state0);
		
		List<State> states = new ArrayList<State>();
		states.add(state0);
		
		int lastSize = -1;
		
		do {
			lastSize = states.size();
			
			for (int i = 0; i < states.size(); i ++) {
				State originState = states.get(i);
				
				for (ProductionToken symbol : this.symbols) {
					State targetState = transfor(originState, symbol);
					
//					System.out.println("origin:" + originState);
//					System.out.println("symbol:" + symbol);
//					System.out.println("target:" + targetState);
					
					if (targetState != null && !states.contains(targetState)) {
						states.add(targetState);
					}
				}
			}
		}
		while (states.size() != lastSize);
		
		return states;
	}
	
	private State transfor(State origin, ProductionToken symbol) {
		
		State target = null;
		
		for (Production production : origin.getProductions()) {
			List<ProductionToken> body = production.body;
			
			ProductionToken symbolNextToDot = getSymbolNextToDot(production);
			if (symbolNextToDot != null && symbolNextToDot.equals(symbol)) {
				if (target == null) {
					target = new State();
				}
				
				Production item = production.clone();
				switchDotSymbolWithNext(item);
				
				target.items.add(item);
			}
		}
		
		if (target != null) {
			target = closure(target);
		}
			
		return target;
	}
	
	private State closure(State state) {
//		return state;
//		System.out.println("before: " + state);
		for (int i = 0; i < state.items.size(); i ++) {
			Production item = state.items.get(i);
			ProductionToken symbolNextToDot = this.getSymbolNextToDot(item);
			
//			System.out.println("symbol: " + symbolNextToDot);
			
			for (Production grammarItem : this.grammar.productions) {
//				System.out.println("grammar item:" + grammarItem);
				if (grammarItem.head.equals(symbolNextToDot) && !state.contains(grammarItem)) {
//					System.out.println("grammarItem:" + grammarItem);
					state.add(grammarItem);
				}
			}
		}
		
//		System.out.println("closure:" + state + "\n");
		return state;
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
	
	private ProductionToken getSymbolNextToDot(Production production) {
		ProductionToken dotSymbol = new ProductionToken("DOT", true);
		dotSymbol.isDotSymbol = true;
		
		List<ProductionToken> body = production.body;
		
		int indexOfDot = body.indexOf(dotSymbol);
		if (indexOfDot < body.size() - 1) {
			return body.get(indexOfDot + 1);
		} else {
			return null;
		}
	}
	
	private void switchDotSymbolWithNext(Production production) {
		List<ProductionToken> body = production.body;
		
		ProductionToken dotSymbol = new ProductionToken("DOT", true);
		dotSymbol.isDotSymbol = true;
		
		int indexOfDot = body.indexOf(dotSymbol);
		if (indexOfDot < body.size() - 1) {
			ProductionToken nextSymbol = body.get(indexOfDot + 1);
			
			body.set(indexOfDot, nextSymbol);
			body.set(indexOfDot + 1, dotSymbol);
		} 
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
	
	public boolean contains(Production prod) {
		return this.items.contains(prod);
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
	
	@Override 
	public State clone() {
		State cloneState = new State();
		List<Production> cloneItems = new ArrayList<>();
		
		for (Production p : this.items) {
			cloneItems.add((Production)p.clone());
		}
		
		cloneState.items = cloneItems;
		
		return cloneState;
	}
}

class ActionTable {
	
}

class GotoTable {
	
}
