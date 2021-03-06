package com.awesome.regexp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

public class LrAutomata {	
	private ContextFreeGrammar originGrammar;
	
	private ContextFreeGrammar grammar;

	private List<State> states;

	private Stack<State> stateStack;

	private LinkedList<InputSymbol> inputQueue;

	private ActionTable actionTable;

	private GotoTable gotoTable;

	private List<ProductionToken> symbols;
	
	private Stack<ProductionToken> traceFirstFunc;
	
	public LrAutomata() {
		this.stateStack = new Stack<State>();
		this.gotoTable = new GotoTable();
		this.actionTable = new ActionTable();
		
		this.traceFirstFunc = new Stack<ProductionToken>();
	}

	public LrAutomata(ContextFreeGrammar grammar) {
		this();

		initSymbolList(grammar);
		
		this.originGrammar = (ContextFreeGrammar)grammar.clone();
		this.grammar = addDotPrefix(grammar);
				
		this.states = constructStates(this.grammar);

		constructActionTable();
		
		List<State> states = this.states;
		Logger.tprint(Config.LRAUTOMATA_VERBOSE && Config.LRAUTOMATA_ACTIONTABLE, this.actionTable, "Action table");
		Logger.tprint(Config.LRAUTOMATA_VERBOSE, new DebugCode() {

			@Override
			public void code() {
				for (int i = 0; i < states.size(); i++) {
					String stateNo = String.format("state %1$2s: ", i);
					State state = states.get(i);
					
					for (int j = 0; j < state.items.size(); j ++) {
						if (j == 0) {
							Logger.print(stateNo);
						} else {
							Logger.printSpaces(stateNo.length());
						}
						
						Logger.println(state.items.get(j));
					}
					
					
				}
			}
			
		}, "States of LrAutomata");
		Logger.tprint(Config.LRAUTOMATA_VERBOSE && Config.LRAUTOMATA_GOTOTABLE, this.gotoTable, "Goto table");
		
	}

	public AbstractSyntaxTree parse(String input) {
		this.inputQueue = initInputQueue(input);
		this.stateStack.push(this.states.get(0));
		
		Stack<InputSymbol> symbolStack = new Stack<InputSymbol>();
		Stack<AbstractSyntaxTree.TreeNode> astStack = new Stack<AbstractSyntaxTree.TreeNode>();
		
		Action action = null;
		
		// Let a be the first symbol of input symbol queue.
		InputSymbol next = inputQueue.poll();

		while (true) {
			
			State state = this.stateStack.peek();
			
			action = this.actionTable.nextAction(state, next.type);
			
			if (action == null) {
				throw new RuntimeException("Not a valid regular expression: " + input);
			}
			
			if (action.shift) {
				State shiftTo = action.shiftTo;
				this.stateStack.push(shiftTo);
				
				symbolStack.push(next);
								
				// Update astStack.
				AbstractSyntaxTree.TreeNode newNode = new AbstractSyntaxTree.TreeNode(next);
				astStack.push(newNode);
				
				next = inputQueue.poll();
			} else if (action.reduce) {
				Production prodToReduce = action.prodToReduce;
				
				AbstractSyntaxTree.TreeNode newNode = new AbstractSyntaxTree.TreeNode();
				
				Stack<AbstractSyntaxTree.TreeNode> tmpAstStack = new Stack<AbstractSyntaxTree.TreeNode>();
				
				for (int i = prodToReduce.body.size() - 2; i >= 0 ; i --) {
					this.stateStack.pop();
					
					symbolStack.pop();
					
					// Update astStack.
					if (prodToReduce.operator != null) {
						tmpAstStack.push(astStack.pop());
					}
				}
				
				// Update astStack.
				if (prodToReduce.operator != null) {
					newNode.operator = prodToReduce.operator;
					
					if (prodToReduce.leftOperandIndex > -1) {
						newNode.leftOperand = tmpAstStack.get(tmpAstStack.size() - prodToReduce.leftOperandIndex - 1);
					}
					
					if (prodToReduce.rightOperandIndex > -1 ) {
						newNode.rightOperand = tmpAstStack.get(tmpAstStack.size() - prodToReduce.rightOperandIndex - 1);
					}
					
					astStack.push(newNode);
				}
				
				
				
				
				State topState = this.stateStack.peek();
				this.stateStack.push(this.gotoTable.nextState(topState, prodToReduce.head));
				
				symbolStack.push(new InputSymbol(prodToReduce.head.text, prodToReduce.head));
								
				// Output thr production A -> B
			} else if (action.accept) {
				break;
			} else if (action.error) {
				// Call error-recovery routine.
				break;
			}
		}
		
		
		return new AbstractSyntaxTree(astStack.pop());
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

		// System.out.println(this.symbols);

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

			for (int i = 0; i < states.size(); i++) {
				State originState = states.get(i);

				for (ProductionToken symbol : this.symbols) {
					State targetState = transfor(originState, symbol);

					if (targetState != null && !states.contains(targetState)) {
						states.add(targetState);
					}
				}
			}
		} while (states.size() != lastSize);

		return states;
	}

	private void constructActionTable() {
		for (State state : this.states) {
			
			for (Production prod : state.getProductions()) {

				ProductionToken dotSymbol = ProductionToken.dot;

				int indexOfDot = prod.body.indexOf(dotSymbol);

				Action action = null;
				
				if (indexOfDot == prod.body.size() - 1) {
					if (prod.head.text.equals("Regexp'")) {
						// state contains the production "Regexp' -> Regexp DOT"
						action = new Action("ACCEPT");
						ProductionToken dollarSymbol = ProductionToken.dollar;

						this.actionTable.update(state, dollarSymbol, action);
					} else {
						List<ProductionToken> followSet = follow(prod.head);
										
						for (ProductionToken symbol : followSet) {
							action = new Action("REDUCE");
							action.prodToReduce = prod;
							
							
							this.actionTable.update(state, symbol, action);
						}
					}

				} else if (indexOfDot >= 0 && indexOfDot < prod.body.size() - 1) {
					ProductionToken symbolNextToDot = getSymbolNextToDot(prod);
						
					if (symbolNextToDot.isTerminal == true) {
						State shiftTo = nextState(state, symbolNextToDot);
						
						action = new Action("SHIFT");
						action.shiftTo = shiftTo;
						
						this.actionTable.update(state, symbolNextToDot, action);
						
					}
				}
			}
		}
		
	}

	private State transfor(State origin, ProductionToken symbol) {

		State target = null;

		for (Production production : origin.getProductions()) {

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

			// Update gotoTable
			this.gotoTable.update(origin, symbol, target);
		}

		return target;
	}

	private State nextState(State origin, ProductionToken symbol) {
		return this.gotoTable.nextState(origin, symbol);
	}

	private State closure(State state) {
		for (int i = 0; i < state.items.size(); i++) {
			Production item = state.items.get(i);
			ProductionToken symbolNextToDot = this.getSymbolNextToDot(item);

			for (Production grammarItem : this.grammar.productions) {
				if (grammarItem.head.equals(symbolNextToDot) && !state.contains(grammarItem)) {
					state.add(grammarItem);
				}
			}
		}

		return state;
	}

	private List<ProductionToken> follow(ProductionToken symbol) {
		List<ProductionToken> followSet = new ArrayList<ProductionToken>();

		// 1. Place $ in FOLLOW(S), where S is the start symbol, and $ is the
		// input right mark.
		if (symbol.isNonTerminal) {
			if (!followSet.contains(ProductionToken.dollar)) {
				followSet.add(ProductionToken.dollar);
			}
		}

		for (Production prod : this.originGrammar.productions) {

			if (prod.body.contains(symbol)) {
				ProductionToken nextSymbol = getOneSymbolNextToAnother(prod, symbol);
				
				if (nextSymbol != null) {
					// 2. If there is a production A -> aBb, then everything in
					// FIRST(b) except epsilon is in FOLLOW(B).
					List<ProductionToken> firstB = first(nextSymbol);
					
					for (ProductionToken firstBItem : firstB) {
						ProductionToken epsilon = ProductionToken.epsilon;
						
						if (!firstBItem.equals(epsilon)) {
							if (!followSet.contains(firstBItem)) {
								followSet.add(firstBItem);
							}
						} else {
							// 3. A production A -> aBb, where FIRST(b) contains
							// epsilon, then everything in FOLLOW(A) is in
							// FOLLOW(B).
							List<ProductionToken> followA = follow(prod.head);

							for (ProductionToken followAItem : followA) {
								if (!followSet.contains(followAItem)) {
									followSet.add(followAItem);
								}
							}
						}
					}
				} else {
					// 3. If there is a production A -> aB, where FIRST(b) contains
					// epsilon, then everything in FOLLOW(A) is in FOLLOW(B).
					List<ProductionToken> followA = follow(prod.head);

					for (ProductionToken followAItem : followA) {
						if (!followSet.contains(followAItem)) {
							followSet.add(followAItem);
						}
					}
				}
			}
			

		}
		
		return followSet;
	}

	private List<ProductionToken> first(ProductionToken symbol) {
		List<ProductionToken> firstSet = new ArrayList<ProductionToken>();
		
		if (this.traceFirstFunc.contains(symbol)) {
			return firstSet;
		} else {
			this.traceFirstFunc.push(symbol);
		}
		
		if (symbol.isTerminal) {
			// symbol is a terminal.
			if (!firstSet.contains(symbol)) {
				firstSet.add(symbol);
			}
			
		} else {
			// symbol is a non-terminal.
			
			
			for (Production prod : this.originGrammar.productions) {
				if (symbol.equals(prod.head)) {
					
					List<ProductionToken> body = prod.body;
					
					for (int i = 0; i < body.size(); i++) {
						ProductionToken symbolY = body.get(i);
						
						List<ProductionToken> firstY = first(symbolY);
								
						for (ProductionToken symbolYItem : firstY) {
							if (!firstSet.contains(symbolYItem)) {
								firstSet.add(symbolYItem);
							}
							
						}
						
						ProductionToken epsilon = ProductionToken.epsilon;
						if (!firstY.contains(epsilon)) {
							break;
						}
					}
				}
			}
		}
		
		this.traceFirstFunc.pop();
		
		return firstSet;
	}

	private LinkedList<InputSymbol> initInputQueue(String input) {
		this.inputQueue = new LinkedList<InputSymbol>();
		
		for (int i = 0; i < input.length(); i ++) {
			char ch = input.charAt(i);
			
			if (ch == '*') {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.star));
			} else if (ch == '(') {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.leftParenthesis));
			} else if (ch == ')') {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.rightParenthesis));
			} else if (ch == '|') {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.verticalBar));
			} else {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.ch));
			}
		}
		
		this.inputQueue.add(new InputSymbol(ProductionToken.dollar.text, ProductionToken.dollar));
		return inputQueue;
	}

	private ContextFreeGrammar addDotPrefix(ContextFreeGrammar grammar) {
		for (Production prod : grammar.productions) {
			List<ProductionToken> body = prod.body;

			ProductionToken dotPrefix = ProductionToken.dot;
			
			body.add(0, dotPrefix);
		}

		return grammar;
	}

	private ProductionToken getSymbolNextToDot(Production production) {
		ProductionToken dotSymbol = ProductionToken.dot;
		return getOneSymbolNextToAnother(production, dotSymbol);
	}

	private ProductionToken getOneSymbolNextToAnother(Production production, ProductionToken symbol) {
		List<ProductionToken> body = production.body;

		int indexOfDot = body.indexOf(symbol);
		if (indexOfDot < body.size() - 1 && indexOfDot >= 0) {
			return body.get(indexOfDot + 1);
		} else {
			return null;
		}
	}

	private void switchDotSymbolWithNext(Production production) {
		List<ProductionToken> body = production.body;

		ProductionToken dotSymbol = ProductionToken.dot;

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
		State that = (State) o;

		if (this.items == null || that.items == null) {
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
			cloneItems.add((Production) p.clone());
		}

		cloneState.items = cloneItems;

		return cloneState;
	}
	
	@Override
	public int hashCode() {
		return this.items.hashCode();
	}
}

class Action {
	public boolean shift;
	public boolean reduce; 
	public boolean accept; 
	public boolean error;
	
	public State shiftTo;
	public Production prodToReduce;
	
	public Action() {
		this.shift = false;
		this.reduce = false;
		this.accept = false;
		this.error = false;
	}
	
	public Action(String action) {
		action = action.toUpperCase();
		
		switch(action) {
		case "SHIFT":
			this.shift = true;
			break;
		case "REDUCE":
			this.reduce = true;
			break;
		case "ACCEPT":
			this.accept = true;
			break;
		case "ERROR":
			this.error = true;
			break;
		}
	}
	
	@Override
	public String toString() {
		String ret = new String();
		
		if (this.shift) {
			ret += "SHIFT -> ";
			ret += shiftTo.toString();
		} else if (this.reduce){
			ret += "REDUCE -> ";
			ret += prodToReduce.toString();
		} else if (this.accept) {
			ret += "ACCEPT";
		} else if (this.error) {
			ret += "ERROR";
		} else {
			assert false;
		}
		return ret;
	}
}

class GotoTable extends Table {
	public GotoTable() {
		super();
	}

	public void update(State origin, ProductionToken symbol, State target) {

		if (!this.tableImpl.containsKey(origin)) {
			this.tableImpl.put(origin, new HashMap<ProductionToken, State>());
		}

		Map<ProductionToken, State> secondaryMap = this.tableImpl.get(origin);

		if (secondaryMap != null) {
			if (secondaryMap.get(symbol) == null) {
				secondaryMap.put(symbol, target);
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
				
				retStr += String.format("( %s, %s ) = ( %s)", origin.toString(), symbol.toString(), target.toString());
				retStr += "\n";
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
		assert action != null;
		
		if (!this.impl.containsKey(state)) {
			this.impl.put(state, new HashMap<ProductionToken, Action>());
		}

		Map<ProductionToken, Action> secondaryMap = this.impl.get(state);

		if (secondaryMap != null) {
			if (secondaryMap.get(symbol) == null) {
				secondaryMap.put(symbol, action);
			} else {
				// Should not get here.
				assert false;
				System.out.println("======================== Meet a conflict in actionTable =======================");
				System.out.println("state:");
				System.out.println(state);
				System.out.println("symbol;");
				System.out.println(symbol);
				System.out.println("Origin action:");
				System.out.println(secondaryMap.get(symbol));
				System.out.println("Current action:");
				System.out.println(action);
			}
		} else {
			// Should not get here.
			assert false;
		}
	}
	
	public Action nextAction(State state, ProductionToken symbol) {
		Map<ProductionToken, Action> secondaryMap = this.impl.get(state);

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

		for (Entry<State, Map<ProductionToken, Action>> hashByStateEntry : this.impl.entrySet()) {
			State state = hashByStateEntry.getKey();
			Map<ProductionToken, Action> hashBySymbol = hashByStateEntry.getValue();

			for (Entry<ProductionToken, Action> hashBySymbolEntry : hashBySymbol.entrySet()) {
				ProductionToken symbol = hashBySymbolEntry.getKey();
				Action target = hashBySymbolEntry.getValue();

				retStr += String.format("( %s, %s ) = ( %s )", state.toString(), symbol.toString(), target.toString());
				retStr += "\n";
			}
		}
		return retStr;
	}
}

class Table {
	protected Map<State, Map<ProductionToken, State>> tableImpl;

	public Table() {
		this.tableImpl = new HashMap<State, Map<ProductionToken, State>>();
	}

}