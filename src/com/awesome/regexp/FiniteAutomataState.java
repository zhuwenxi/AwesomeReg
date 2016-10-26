package com.awesome.regexp;

public class FiniteAutomataState {
	
	public int stateNumber;
	public boolean isAcceptState;
	public String regexp;
	
	public FiniteAutomataState() {
		this.stateNumber = -1;
		this.isAcceptState = false;
		this.regexp = null;
	}
	
	public FiniteAutomataState(int stateNumber) {
		this();
		this.stateNumber = stateNumber;
	}
	
	public void markAsAcceptState() {
		this.isAcceptState = true;
	}
	
	@Override
	public boolean equals(Object that) {
		return that != null && this.stateNumber == ((FiniteAutomataState)that).stateNumber && this.isAcceptState == ((FiniteAutomataState)that).isAcceptState;
	}
	
	@Override
	public int hashCode() {
		return this.stateNumber;
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.stateNumber) + (this.isAcceptState ? "*" : "");
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

