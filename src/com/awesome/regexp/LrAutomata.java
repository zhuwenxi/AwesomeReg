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
		
//		System.out.println(this.originGrammar);
//		System.out.println(this.grammar);
		
		this.states = constructStates(this.grammar);

		constructActionTable();
		
//		System.out.println("======================== action table ==========================\n");
//		System.out.println(this.actionTable);
//		System.out.println("======================== action table end ======================\n");
//		System.out.println("======================== states ==========================\n");
//		printStates();
//		System.out.println("======================== goto table ============================\n");
//		System.out.println(this.gotoTable);
//		System.out.println("======================== goto table end ============================\n");
		
//		System.out.println(follow(new ProductionToken("Regexp", false)));
	}

	public AbstractSyntaxTree parse(String input) {
		this.inputQueue = initInputQueue(input);
		this.stateStack.push(this.states.get(0));
		
		Stack<InputSymbol> symbolStack = new Stack<InputSymbol>();
		Stack<AbstractSyntaxTree.TreeNode> astStack = new Stack<AbstractSyntaxTree.TreeNode>();
		
		Action action = null;
		
		// Let a be the first symbol of input symbol queue.
		InputSymbol next = inputQueue.poll();

		while (true /*action != Action.ACCEPT && action != Action.ERROR*/) {
			
//			System.out.println("============================= parse() ===========================");
			State state = this.stateStack.peek();
//			System.out.println("state: " + state);
//			System.out.println("input: " + next.face);
			
			action = this.actionTable.nextAction(state, next.type);
			
			if (action == null) {
				throw new RuntimeException("Not a valid regular expression: " + input);
			}
			
			if (action.shift) {
				State shiftTo = action.shiftTo;
				this.stateStack.push(shiftTo);
				
				symbolStack.push(next);
				
//				System.out.println("shift: " + shiftTo);
//				System.out.println(symbolStack);
				
				// Update astStack.
				AbstractSyntaxTree.TreeNode newNode = new AbstractSyntaxTree.TreeNode(next);
//				System.out.println("shift node:");
//				newNode.printSelf(1);
				astStack.push(newNode);
				
				next = inputQueue.poll();
			} else if (action.reduce) {
				Production prodToReduce = action.prodToReduce;
				
				AbstractSyntaxTree.TreeNode newNode = new AbstractSyntaxTree.TreeNode();
				
				Stack<AbstractSyntaxTree.TreeNode> tmpAstStack = new Stack<AbstractSyntaxTree.TreeNode>();
				
				for (int i = prodToReduce.body.size() - 2; i >= 0 ; i --) {
					ProductionToken lastProductionToken = prodToReduce.body.get(i);
//					Symbol symbol = this.inputQueue.pop();
//					assert symbol.type == lastProductionToken;
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
//					if (prodToReduce.operator == AbstractSyntaxTree.Operator.UNIT) {
//						System.out.println("~~~~~~~~~~~~~~");
//					}
					
					if (prodToReduce.leftOperandIndex > -1) {
						newNode.leftOperand = tmpAstStack.get(tmpAstStack.size() - prodToReduce.leftOperandIndex - 1);
					}
					
					if (prodToReduce.rightOperandIndex > -1 ) {
						newNode.rightOperand = tmpAstStack.get(tmpAstStack.size() - prodToReduce.rightOperandIndex - 1);
					}
					
//					System.out.println("reduce node:");
//					System.out.println("~~~~~~~~~~~~~~~AST start~~~~~~~~~~~~~~~");
//					newNode.printSelf(1);
//					System.out.println("~~~~~~~~~~~~~~~AST end~~~~~~~~~~~~~~~");
					astStack.push(newNode);
				}
				
				
				
				
				State topState = this.stateStack.peek();
				this.stateStack.push(this.gotoTable.nextState(topState, prodToReduce.head));
				
				symbolStack.push(new InputSymbol(prodToReduce.head.text, prodToReduce.head));
				
//				System.out.println("reduce: " + action.prodToReduce);
//				System.out.println(symbolStack);
				
				// Output thr production A -> B
			} else if (action.accept) {
//				System.out.println("accept");
				break;
			} else if (action.error) {
				// Call error-recovery routine.
				break;
			}
//			System.out.println("===================== Divider ========================\n");
		}
		
		
//		AbstractSyntaxTree.TreeNode astNode = astStack.peek();
//		astNode.printSelf(0);
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

					// System.out.println("origin:" + originState);
					// System.out.println("symbol:" + symbol);
					// System.out.println("target:" + targetState);

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
						
//						if (symbolNextToDot.text.equals(")")) {
//							System.out.println(symbolNextToDot);
//						}
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

			// Update gotoTable
			this.gotoTable.update(origin, symbol, target);
		}

		return target;
	}

	private State nextState(State origin, ProductionToken symbol) {
		return this.gotoTable.nextState(origin, symbol);
	}

	private State closure(State state) {
		// return state;
		// System.out.println("before: " + state);
		for (int i = 0; i < state.items.size(); i++) {
			Production item = state.items.get(i);
			ProductionToken symbolNextToDot = this.getSymbolNextToDot(item);

			// System.out.println("symbol: " + symbolNextToDot);

			for (Production grammarItem : this.grammar.productions) {
				// System.out.println("grammar item:" + grammarItem);
				if (grammarItem.head.equals(symbolNextToDot) && !state.contains(grammarItem)) {
					// System.out.println("grammarItem:" + grammarItem);
					state.add(grammarItem);
				}
			}
		}

		// System.out.println("closure:" + state + "\n");
		return state;
	}

	private List<ProductionToken> follow(ProductionToken symbol) {
//		System.out.println("follow():\nsymbol:" + symbol);
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
		
//		System.out.println("follow(symbol): \n" + followSet);
		return followSet;
	}

	private List<ProductionToken> first(ProductionToken symbol) {
//		System.out.println("first():\nsymbol: " + symbol);
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
			
			if (ch >= '0' && ch <= '9' || ch >= 'a' && ch <='z' || ch >= 'A' && ch <= 'Z') {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.ch));
			} else if (ch == '*') {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.star));
			} else if (ch == '(') {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.leftParenthesis));
			} else if (ch == ')') {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.rightParenthesis));
			} else if (ch == '|') {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.verticalBar));
			} else {
				this.inputQueue.add(new InputSymbol(ch, ProductionToken.verticalBar));
//				assert false;
			}
//			
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
		//
		// List<ProductionToken> body = production.body;
		//
		// int indexOfDot = body.indexOf(dotSymbol);
		// if (indexOfDot < body.size() - 1) {
		// return body.get(indexOfDot + 1);
		// } else {
		// return null;
		// }
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

	private void printStates() {
		int stateIndex = 0;
		for (State state : this.states) {
			System.out.println("state " + stateIndex + ":");
			stateIndex++;

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
//		return 100;
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
			ret += "SHIFT:\n";
			ret += shiftTo.toString();
		} else if (this.reduce){
			ret += "REDUCE:\n";
			ret += prodToReduce.toString();
		} else if (this.accept) {
			ret += "ACCEPT:\n";
		} else if (this.error) {
			ret += "ERROR:\n";
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
			} else {
//				assert false;
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
		assert action != null;
		
//		if (action == Action.REDUCE) {
//			System.out.println("++++++++++++++++++++++++++++++");
//			System.out.println("update():");
//			System.out.println(state);
//			System.out.println(symbol);
//			System.out.println(action);
//			System.out.println("++++++++++++++++++++++++++++++");
//		}
		
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
//			for (ProductionToken key : secondaryMap.keySet()) {
//				assert key.equals(symbol);
//				assert key.hashCode() == symbol.hashCode();
//				assert secondaryMap.containsKey(symbol);
//			}
//			assert secondaryMap.keySet().contains(symbol);
//			assert secondaryMap.containsKey(symbol);
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

				retStr += "State:\n";
				retStr += state.toString();
				retStr += "\n";

				retStr += "Symbol:\n";
				retStr += symbol.toString();
				retStr += "\n";

				retStr += "Action:\n";
				retStr += target.toString();
				retStr += "\n";

				retStr += "=================================================================\n";
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

class InputSymbol {
	public static InputSymbol epsilon;
	
	public boolean isEpsilon;
	
	public String face;
	
	public ProductionToken type;
	
	static {
		InputSymbol.epsilon = new InputSymbol();
		InputSymbol.epsilon.isEpsilon = true;
		InputSymbol.epsilon.face = "EPSILON";
	}
	public InputSymbol() {
		this.isEpsilon = false;
	}
	
	public InputSymbol(char ch, ProductionToken symbolType) {
		this();
		this.face = Character.toString(ch);
		this.type = symbolType;
	}
	
	public InputSymbol(String face, ProductionToken symbolType) {
		this();
		this.face = face;
		this.type = symbolType;
	}
	
	@Override
	public String toString() {
		return face;
	}
	
	@Override
	public boolean equals(Object o) {
		InputSymbol that = (InputSymbol)o;
		return that != null && this.face.equals(that.face) && this.isEpsilon == that.isEpsilon;
	}
	
	@Override
	public int hashCode() {
		// Dirty implementation ...
		return 100;
	}
}
