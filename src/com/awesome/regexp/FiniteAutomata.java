package com.awesome.regexp;

import java.util.HashMap;
import java.util.Map;

public class FiniteAutomata {
	private Map<FiniteAutomataState, Map<InputSymbol, FiniteAutomataState>> transDiag;
	
	public FiniteAutomata() {
		this.transDiag = new HashMap<FiniteAutomataState, Map<InputSymbol, FiniteAutomataState>>();
	}
	
	public FiniteAutomata(AbstractSyntaxTree ast) {
		this();
		buildFiniteAutomata(ast);
	}
	
	private void buildFiniteAutomata(AbstractSyntaxTree ast) {
		if (ast == null || ast.root == null) {
			return;
		}
		
		buildFiniteAutomataFromRoot(ast.root);
	}
	
	private TransDiagStartAndEnd buildFiniteAutomataFromRoot(AbstractSyntaxTree.TreeNode node) {
		return null;
	}
}

class FiniteAutomataState {
	public int stateNumber;
	
	public FiniteAutomataState() {
		this.stateNumber = -1;
	}
	
	@Override
	public boolean equals(Object that) {
		return that != null && this.stateNumber == ((FiniteAutomataState)that).stateNumber;
	}
}

final class TransDiagStartAndEnd {
	public FiniteAutomataState startState;
	public FiniteAutomataState endState;
	
}
