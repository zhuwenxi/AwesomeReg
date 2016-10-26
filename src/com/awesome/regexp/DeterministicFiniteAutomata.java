package com.awesome.regexp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.awesome.regexp.util.TwoStageHashMap;


public class DeterministicFiniteAutomata extends FiniteAutomata{
	
	public List<FiniteAutomataState> states;
	
	private TwoStageHashMap<List<FiniteAutomataState>, InputSymbol, List<FiniteAutomataState>> internalTransDiag;
	private List<List<FiniteAutomataState>> internalStates;
	
	private List<FiniteAutomataState> internalStart;
	private List<List<FiniteAutomataState>> internalEnd;
	
	private Map<List<FiniteAutomataState>, FiniteAutomataState> stateDict;
	
	public DeterministicFiniteAutomata() {
		super();
		this.internalTransDiag = new TwoStageHashMap<List<FiniteAutomataState>, InputSymbol, List<FiniteAutomataState>>();
		this.internalStates = new ArrayList<List<FiniteAutomataState>>();
		
		this.states = new ArrayList<FiniteAutomataState>();
		this.transDiag = new TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState>();
		
		this.stateDict = new HashMap<List<FiniteAutomataState>, FiniteAutomataState>();
		
		this.internalEnd = new ArrayList<List<FiniteAutomataState>>();
	}
	
	public DeterministicFiniteAutomata(FiniteAutomata nfa) {
		this(nfa, true);
	}
	
	public DeterministicFiniteAutomata(FiniteAutomata nfa, boolean minification) {
		this();
		
		nfaToDfa(nfa);
		Logger.tprint(Config.DFA_VERBOSE && Config.DFA_BASIC, this.internalTransDiag, "DFA_BASIC");
		
		renameNfaStateToDfaState();
		Logger.tprint(Config.DFA_VERBOSE && Config.DFA_RENAME_STATE, this.transDiag, "DFA_RENAME_STATE");
		
		if (minification) {
			minifyStates();
		}
	}
	
	/*
	 * Use subset construction to convert NFA to DFA.
	 */
	public void nfaToDfa(FiniteAutomata nfa) {
		// Set symbolSet.
		this.symbolSet = nfa.symbolSet;
		
		List<List<FiniteAutomataState>> workList = new ArrayList<List<FiniteAutomataState>>();
		List<List<FiniteAutomataState>> Q = new ArrayList<List<FiniteAutomataState>>();
		
		List<FiniteAutomataState> dfaState0 = new ArrayList<FiniteAutomataState>();
		dfaState0.add(nfa.start);
		dfaState0 = nfa.epsilonClosure(dfaState0);
		this.internalStates.add(dfaState0);
		this.internalStart = dfaState0;
		
		
		workList.add(dfaState0);
		Q.add(dfaState0);
		
		while (!workList.isEmpty()) {
			List<FiniteAutomataState> dfaState = workList.remove(0);
			
			for (InputSymbol input : this.symbolSet) {
				List<FiniteAutomataState> newDfaState = new ArrayList<FiniteAutomataState>();
				
				for (FiniteAutomataState nfaState: dfaState) {
					if (nfa.transDiag.query(nfaState, input) != null) {
						newDfaState.addAll(nfa.transDiag.query(nfaState, input));
					}
				}
				
				newDfaState = nfa.epsilonClosure(newDfaState);
				
				if (newDfaState != null) {
					
					this.internalTransDiag.update(dfaState, input, newDfaState);
					
					if (!Q.contains(newDfaState)) {
						this.internalStates.add(newDfaState);
						
						assert nfa.end.size() == 1;
						if (newDfaState.contains(nfa.end.get(0))) {
							// Mark as accept state:
							this.internalEnd.add(newDfaState);
						}
						
						workList.add(newDfaState);
						Q.add(newDfaState);
					}
				}
			}
		}
	}
	
	public void renameNfaStateToDfaState() {
		for (List<FiniteAutomataState> nfaStates : this.internalStates) {
			FiniteAutomataState newDfaState = createStates(1).get(0);
			this.stateDict.put(nfaStates, newDfaState);
			
			if (nfaStates == this.internalStart) {
				this.start = newDfaState;
			}
			
			if (this.internalEnd.contains(nfaStates)) {
				this.end.add(newDfaState);
				newDfaState.markAsAcceptState();
			}
			
			this.states.add(newDfaState);
		}
		
		FiniteAutomata that = this;
		Logger.tprint(Config.DFA_RENAME_STATE_START_END, new DebugCode() {

			@Override
			public void code() {
				Logger.println("start state: ");
				Logger.println(that.start);
				Logger.println("end state: ");
				Logger.println(that.end);
			}
			
		}, "DFA_RENAME_STATE_START_END");
		
		for (List<FiniteAutomataState> nfaStates : this.internalTransDiag.getL1KeySet()) {
			for (InputSymbol input : this.internalTransDiag.getL2KeySet(nfaStates)) {
				for (List<FiniteAutomataState> destState : this.internalTransDiag.query(nfaStates, input)) {
					this.transDiag.update(this.stateDict.get(nfaStates), input, this.stateDict.get(destState));
				}
			}
		}
	}
	
	private void minifyStates() {
		List<List<FiniteAutomataState>> partitions = new ArrayList<List<FiniteAutomataState>>();
		List<FiniteAutomataState> acceptPartition = new ArrayList<FiniteAutomataState>();
		List<FiniteAutomataState> nonAcceptPartition = new ArrayList<FiniteAutomataState>();
		for (FiniteAutomataState state : this.states) {
			if (this.end.contains(state)) {
				acceptPartition.add(state);
			} else {
				nonAcceptPartition.add(state);
			}
		}
		partitions.add(nonAcceptPartition);
		partitions.add(acceptPartition);
		
		
		List<List<FiniteAutomataState>> lastPartitions = new ArrayList<List<FiniteAutomataState>>();
		
		while (lastPartitions.size() != partitions.size()) {
			
			lastPartitions = partitions;
			partitions = new ArrayList<List<FiniteAutomataState>>();
			
			for (int i = 0; i < lastPartitions.size(); i ++) {
				List<FiniteAutomataState> partition = lastPartitions.get(i);
				
				List<List<FiniteAutomataState>> newPartitions = splitPartition(partition, lastPartitions);
										
				partitions.addAll(newPartitions);
			}
		}
				
		renameParitionToState(partitions);
	}
	
	private List<List<FiniteAutomataState>> splitPartition(List<FiniteAutomataState> partition, List<List<FiniteAutomataState>> lastPartitions) {
		List<List<FiniteAutomataState>> newPartitions = new ArrayList<List<FiniteAutomataState>>();
		List<FiniteAutomataState> s1 = null;
		List<FiniteAutomataState> s2 = null;
		
		List<FiniteAutomataState> partitionS1BelongTo = null;
		for (InputSymbol symbol : this.symbolSet) {
			s1 = new ArrayList<FiniteAutomataState>();
			s2 = new ArrayList<FiniteAutomataState>();
			
			for (FiniteAutomataState state : partition) {
				List<FiniteAutomataState> destStates = this.transDiag.query(state, symbol);
				
				if (destStates != null) {
					assert destStates.size() == 1;
					FiniteAutomataState destState = destStates.get(0);
					
					if (partitionS1BelongTo == null) {
						partitionS1BelongTo = whichPartition(destState, lastPartitions);
					}
					
					if (partitionS1BelongTo.contains(destState)) {
						s1.add(state);
					} else {
						s2.add(state);
					}
					
				} else {
					s2.add(state);
				}
			}
			
			if (s1.size() > 0 && s2.size() > 0) {
				newPartitions.add(s1);
				newPartitions.add(s2);
							
				return newPartitions;
			}
		}
		
		newPartitions.add(partition);
		return newPartitions;
	}
	
	private List<FiniteAutomataState> whichPartition(FiniteAutomataState state, List<List<FiniteAutomataState>> lastPartitions) {
		for (List<FiniteAutomataState> partition : lastPartitions ) {
			if (partition.contains(state)) {
				return partition;
			}
		}
		
		assert false;
		return null;
	}
	
	private void renameParitionToState(List<List<FiniteAutomataState>> partitions) {
		printDebugLog(partitions);
		resetStates();
		Map<FiniteAutomataState, FiniteAutomataState> stateDict = new HashMap<FiniteAutomataState, FiniteAutomataState>();
		
		List<FiniteAutomataState> newStates = new ArrayList<FiniteAutomataState>();
		FiniteAutomataState startState = null;
		List<FiniteAutomataState> endStates = new ArrayList<FiniteAutomataState>();
		TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState> newTransDiag = new TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState>();
		
		for (List<FiniteAutomataState> partition : partitions) {
			FiniteAutomataState newState = createStates(1).get(0);
			
			for (FiniteAutomataState originState : partition) {
				stateDict.put(originState, newState);
			}
			
			
			if (partition.contains(this.start)) {
				startState = newState;
			}
			
			for (FiniteAutomataState endState: this.end) {
				if (partition.contains(endState)) {
					endStates.add(newState);
					newState.markAsAcceptState();
					break;
				}
			}
			
			newStates.add(newState);
		}
		
		for (FiniteAutomataState originState : this.states) {
			for (InputSymbol input : this.symbolSet) {
				List<FiniteAutomataState> destState = this.transDiag.query(originState, input);
				
				if (destState != null) {
					assert destState.size() == 1;
					assert stateDict.get(originState) != null && stateDict.get(destState.get(0)) != null;
					newTransDiag.update(stateDict.get(originState), input, stateDict.get(destState.get(0)));
				}
			}
		}
		
		this.start = startState;
		this.end = endStates;
		this.states = newStates;
		this.transDiag = newTransDiag;
	}
	
	
}
