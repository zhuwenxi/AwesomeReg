package com.awesome.regexp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.awesome.regexp.util.TwoStageHashMap;

public class FiniteAutomata {
	
	public static boolean DEBUG = false;
	
	public int nextStateNumber;
	
	public List<FiniteAutomataState> states;
	
	public FiniteAutomataState start;
	public List<FiniteAutomataState> end;
	
	public List<InputSymbol> symbolSet;
	
	protected TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState> transDiag;
	
	public FiniteAutomata() {
		this.nextStateNumber = 0;
		this.transDiag = new TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState>();
		this.states = new ArrayList<FiniteAutomataState>();
		
		this.symbolSet = new ArrayList<InputSymbol>();
		
		this.end = new ArrayList<FiniteAutomataState>();
	}
	
	public boolean isAcceptState(FiniteAutomataState state) {
		
		for (FiniteAutomataState acceptState : this.end) {
			if (acceptState.equals(state)) {
				return true;
			}
		}
		
		return false;
	}
	
	protected List<FiniteAutomataState> createStates(int stateCount) {
		List<FiniteAutomataState> states = new ArrayList<>();
		
		for (int i = 0; i < stateCount; i ++) {
			FiniteAutomataState newState = new FiniteAutomataState(this.nextStateNumber);
			states.add(newState);
			this.nextStateNumber ++;
		}
		
		this.states.addAll(states);
		
		return states;
	}
		
	protected void printDebugLog(Object log) {
		if (FiniteAutomata.DEBUG) {
			System.out.println(log.toString());
		}
	}
	
	protected List<FiniteAutomataState> epsilonClosure(List<FiniteAutomataState> dfaState) {		
		if (dfaState.size() == 0 || dfaState == null) {
			return null;
		}
		
		List<FiniteAutomataState> closureSet = new ArrayList<FiniteAutomataState>();
		List<FiniteAutomataState> workList = new ArrayList<FiniteAutomataState>();
		
		workList.addAll(dfaState);
		closureSet.addAll(dfaState);
		
		while(!workList.isEmpty()) {
			FiniteAutomataState nfaState = workList.remove(0);
			
			List<FiniteAutomataState> dest = this.transDiag.query(nfaState, InputSymbol.epsilon);
			
			if (dest != null) {
				for(FiniteAutomataState s : dest) {
					if (!closureSet.contains(s)) {
						workList.add(s);
						closureSet.add(s);
					}
				}
			}
			
		}
		
		closureSet.sort(new Comparator<FiniteAutomataState>() {
			public int compare(FiniteAutomataState s1, FiniteAutomataState s2) {
				return s1.stateNumber - s2.stateNumber;
			}
		});
		
		return closureSet;
	}
	
	protected void resetStates() {
		this.nextStateNumber = 0;
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
