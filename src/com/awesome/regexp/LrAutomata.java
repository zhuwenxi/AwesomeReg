package com.awesome.regexp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

public class LrAutomata {
	
	private ContextFreeGrammar grammar;
	
	private List<State> states;
	
	private Stack<State> stateStack;
	
	private LinkedList<Character> inputQueue;
	
	private ActionTable actionTable;
	
	private GotoTable gotoTable;
	
	private List<ProductionToken> symbols;
	
	
	
	
	public LrAutomata(){
		this.stateStack = new Stack<State>();
		this.gotoTable = new GotoTable();
		this.actionTable = new ActionTable();
	}
	
	public LrAutomata(ContextFreeGrammar grammar) {
		this();
		
		initSymbolList(grammar);
		
		this.grammar = addDOTPrefix(grammar);
		
		this.states = constructStates(this.grammar);
		
		constructActionTable();
		
//		printStates();
		System.out.println(this.gotoTable);
//		System.out.println(this.states);
//		System.out.println(this.states.size());
	}
	
	public AbstractSyntaxTree parse(String input) {
		initInputQueue(input);
		this.stateStack.push(this.states.get(0));
		Action action = null;
		
		while (action != null && action != Action.ERROR) {
			State state = this.stateStack.peek();
			action = action(state, null);
		}
		
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
	
	private void constructActionTable() {
		for (State state : this.states) {
			for (Production prod : state.getProductions()) {
				
				ProductionToken dotSymbol = new ProductionToken("DOT", true);
				dotSymbol.isDotSymbol = true;
				
				int indexOfDot = prod.body.indexOf(dotSymbol);
				
				Action action = null;
				
				if (indexOfDot == prod.body.size() - 1) {
					
					if (prod.equals(this.states.get(0).getProductions().get(0))) {
						// state contains the production "Regexp' -> Regexp DOT"
						action = Action.ACCEPT;
						ProductionToken dollarSymbol = new ProductionToken("$", true);
						
						this.actionTable.update(state, dollarSymbol, action);
					} else {
						List<ProductionToken> followSet = follow(prod.head);
						
						for (ProductionToken symbol : followSet) {
							action = Action.REDUCE;
							this.actionTable.update(state, symbol, action);
						}
					}
					
				} else if (indexOfDot >= 0 && indexOfDot < prod.body.size() - 1 ){
					ProductionToken symbolNextToDot = getSymbolNextToDot(prod);
					
					if (symbolNextToDot.isTerminal == true) {
						State shiftTo = nextState(state, symbolNextToDot);
						
						action = Action.SHIFT;
						action.shiftTo = shiftTo;
					}
				} {
					assert false;
				}
			}
		}
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
			
			//Update gotoTable
			this.gotoTable.update(origin, symbol, target);
		}
			
		return target;
	}
	
	private Action action(State state, ProductionToken symbol) {
		return null;
	}
	
	private State nextState (State origin, ProductionToken symbol) {
		return this.gotoTable.nextState(origin, symbol);
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
	
	private List<ProductionToken> follow(ProductionToken symbol) {
		return new ArrayList<ProductionToken>();
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
	
	private void printStates() {
		int stateIndex = 0;
		for (State state : this.states) {
			System.out.println("state " + stateIndex + ":");
			stateIndex ++;
			
			for (Production prod : state.items) {
				System.out.println(prod);
			}
			
			System.out.print("\n");
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




enum Action {
	SHIFT, REDUCE, ACCEPT, ERROR;
	public State shiftTo;
	public Production reduce;
}

class GotoTable extends Table{
	public GotoTable() {
		super();
	}
	
	public void update(State origin, ProductionToken symbol, State target) {
		
		if (!this.tableImpl.containsKey(origin)) {
			this.tableImpl.put(origin, new HashMap<ProductionToken, State>() );
		} 
		
		Map<ProductionToken, State> secondaryMap = this.tableImpl.get(origin);
		
		if (secondaryMap != null) {
			if (secondaryMap.get(symbol) == null) {
				secondaryMap.put(symbol, target);
			} else {
				// Should not get here.
				assert false;
			}
		} else {
			// Should not get here.
			assert false;
		}
	}
	
	public State nextState(State origin, ProductionToken symbol) {
		
		Map<ProductionToken, State> secondaryMap = this.tableImpl.get(origin);
		
		if (secondaryMap != null) {
			return secondaryMap.get(symbol);
		} else {
			// Should not get here.
			assert false;
			return null;
		}
	}
	
	@Override
	public String toString() {
		
		String retStr = "";
		
		for (Entry<State, Map<ProductionToken, State>> hashByStateEntry : this.tableImpl.entrySet()) {
			State origin = hashByStateEntry.getKey();
			Map<ProductionToken, State> hashBySymbol = hashByStateEntry.getValue();
			
			for (Entry<ProductionToken, State> hashBySymbolEntry : hashBySymbol.entrySet()) {
				ProductionToken symbol = hashBySymbolEntry.getKey();
				State target = hashBySymbolEntry.getValue();
				
				retStr += "Origin:\n";
				retStr += origin.toString();
				retStr += "\n";
				
				retStr += "Symbol:\n";
				retStr += symbol.toString();
				retStr += "\n";
				
				retStr += "Target:\n";
				retStr += target.toString();
				retStr += "\n";
				
				retStr += "=================================================================\n";
			}
		}
		return retStr;
	}
	
}

class ActionTable {
	protected Map<State, Map<ProductionToken, Action>> impl;
	
	public ActionTable() {
		this.impl = new HashMap<State, Map<ProductionToken, Action>>();
	}
	
	public void update(State state, ProductionToken symbol, Action action) {
		if (!this.impl.containsKey(state)) {
			this.impl.put(state, new HashMap<ProductionToken, Action>() );
		} 
		
		Map<ProductionToken, Action> secondaryMap = this.impl.get(state);
		
		if (secondaryMap != null) {
			if (secondaryMap.get(symbol) == null) {
				secondaryMap.put(symbol, action);
			} else {
				// Should not get here.
				assert false;
			}
		} else {
			// Should not get here.
			assert false;
		}
	}
}

class Table {
	protected Map<State, Map<ProductionToken, State>> tableImpl;
	
	public Table() {
		this.tableImpl = new HashMap<State, Map<ProductionToken, State>>();
	}
	
	
}
