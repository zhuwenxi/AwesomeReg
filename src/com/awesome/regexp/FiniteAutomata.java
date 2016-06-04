package com.awesome.regexp;

import java.util.ArrayList;
import java.util.List;

import com.awesome.regexp.util.TwoStageHashMap;

public class FiniteAutomata {
	
	public static boolean DEBUG = true;
	
	public int nextStateNumber;
	
	private TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState> transDiag;
	
	public FiniteAutomata() {
		this.nextStateNumber = 0;
		this.transDiag = new TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState>();
	}
	
	/*
	 * Use subset construction to convert NFA to DFA.
	 */
	public static FiniteAutomata nfaToDfa(FiniteAutomata nfa) {
		return null;
	}
	
	public void buildNfa(AbstractSyntaxTree ast) {
		if (ast == null || ast.root == null) {
			return;
		}
		
		buildNfaFromRoot(ast.root);
	}
	
	private TransDiagStartAndEnd buildNfaFromRoot(AbstractSyntaxTree.TreeNode node) {
		AbstractSyntaxTree.TreeNode leftChild = node.leftOperand;
		AbstractSyntaxTree.TreeNode rightChild = node.rightOperand;
		
		TransDiagStartAndEnd leftDiag = null;
		TransDiagStartAndEnd rightDiag = null;
		
		TransDiagStartAndEnd newDiag = null;
		
		if (leftChild != null) {
			leftDiag = buildNfaFromRoot(leftChild);
		}
		
		if (rightChild != null) {
			rightDiag = buildNfaFromRoot(rightChild);
		}
		
		newDiag = genNewNfaTransDiag(node, leftDiag, rightDiag);
		
		return newDiag;
	}
	
	private TransDiagStartAndEnd genNewNfaTransDiag(AbstractSyntaxTree.TreeNode node, TransDiagStartAndEnd leftDiag, TransDiagStartAndEnd rightDiag) {
		AbstractSyntaxTree.Operator operator = node.operator;
		TransDiagStartAndEnd newDiag = null;
		
		
		if (operator == null) {
			// Leaf node:
			printDebugLog("\n<Leaf>: ");
			
			newDiag = contructLeafNode(node.text);
		} else {
			// Interior node:
			
			printDebugLog("\n" + operator.toString());
			
			switch(operator) {
			case ALTER:
				newDiag = constructAlter(leftDiag, rightDiag);
				break;
			case CONCAT:
				newDiag = constructConcat(leftDiag, rightDiag);
				break;
			case REPEAT:
				newDiag = constructRepeat(leftDiag, rightDiag);
				break;
			case UNIT:
				newDiag = constructUnit(leftDiag, rightDiag);
				break;
			}
		}
		
		printDebugLog("=========== Trans Diag Start ==========");
		printDebugLog(this.transDiag.toString()); 
		printDebugLog("=========== Trans Diag End ==========");
		
		return newDiag;
	}
	
	private TransDiagStartAndEnd constructAlter(TransDiagStartAndEnd leftDiag, TransDiagStartAndEnd rightDiag) {
		List<FiniteAutomataState> states = createStates(2);
		
		FiniteAutomataState state1 = states.get(0);
		FiniteAutomataState state2 = states.get(1);
		
		this.transDiag.update(state1, InputSymbol.epsilon, leftDiag.startState);
		this.transDiag.update(state1, InputSymbol.epsilon, rightDiag.startState);
		
		this.transDiag.update(leftDiag.endState, InputSymbol.epsilon, state2);
		this.transDiag.update(rightDiag.endState, InputSymbol.epsilon, state2);

		return new TransDiagStartAndEnd(state1, state2);
	}
	
	private TransDiagStartAndEnd constructConcat(TransDiagStartAndEnd leftDiag, TransDiagStartAndEnd rightDiag) {
		this.transDiag.update(leftDiag.endState, InputSymbol.epsilon, rightDiag.startState);
		
//		printDebugLog("=========== Trans Diag Start ==========");
//		printDebugLog(this.transDiag.toString()); 
//		printDebugLog("=========== Trans Diag End ==========");
		
		return new TransDiagStartAndEnd(leftDiag.startState, rightDiag.endState);
	}
	
	private TransDiagStartAndEnd constructRepeat(TransDiagStartAndEnd leftDiag, TransDiagStartAndEnd rightDiag) {
		assert leftDiag != null;
		assert rightDiag == null;
		
		List<FiniteAutomataState> states = createStates(2);
		assert states.size() == 2;
		
		FiniteAutomataState state1 = states.get(0);
		FiniteAutomataState state2 = states.get(1);
		
		this.transDiag.update(state1, InputSymbol.epsilon, leftDiag.startState);
		this.transDiag.update(state1, InputSymbol.epsilon, state2);
		this.transDiag.update(leftDiag.endState, InputSymbol.epsilon, state2);
		this.transDiag.update(leftDiag.endState, InputSymbol.epsilon, leftDiag.startState);
		
		return new TransDiagStartAndEnd(state1, state2);
	}
	
	private TransDiagStartAndEnd constructUnit(TransDiagStartAndEnd leftDiag, TransDiagStartAndEnd rightDiag) {
		assert leftDiag != null;
		assert rightDiag == null;
		
		return new TransDiagStartAndEnd(leftDiag.startState, leftDiag.endState);
	}
	
	private TransDiagStartAndEnd contructLeafNode(InputSymbol symbol) {
		List<FiniteAutomataState> states = createStates(2);
		assert states.size() == 2;
		
		FiniteAutomataState state1 = states.get(0);
		FiniteAutomataState state2 = states.get(1);
		
		this.transDiag.update(state1, symbol, state2);
//		printDebugLog("=========== Trans Diag Start ==========");
//		printDebugLog(this.transDiag.toString()); 
//		printDebugLog("=========== Trans Diag End ==========");
		
		return new TransDiagStartAndEnd(state1, state2);
	}
	
	private List<FiniteAutomataState> createStates(int stateCount) {
		List<FiniteAutomataState> states = new ArrayList<>();
		
		for (int i = 0; i < stateCount; i ++) {
			states.add(new FiniteAutomataState(this.nextStateNumber));
			this.nextStateNumber ++;
		}
		
		return states;
	}
		
	private void printDebugLog(String log) {
		if (FiniteAutomata.DEBUG) {
			System.out.println(log);
		}
	}
	
}

class FiniteAutomataState {
	public int stateNumber;
	
	public FiniteAutomataState() {
		this.stateNumber = -1;
	}
	
	public FiniteAutomataState(int stateNumber) {
		this.stateNumber = stateNumber;
	}
	
	@Override
	public boolean equals(Object that) {
		return that != null && this.stateNumber == ((FiniteAutomataState)that).stateNumber;
	}
	
	@Override
	public int hashCode() {
		return this.stateNumber;
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.stateNumber);
	}
}

final class TransDiagStartAndEnd {
	public FiniteAutomataState startState;
	public FiniteAutomataState endState;
	
	public TransDiagStartAndEnd(FiniteAutomataState start, FiniteAutomataState end) {
		this.startState = start;
		this.endState = end;
	}
}
