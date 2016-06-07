package com.awesome.regexp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.awesome.regexp.util.TwoStageHashMap;


public class DeterministicFiniteAutomata extends FiniteAutomata{
	
	public TwoStageHashMap<FiniteAutomataState, InputSymbol, FiniteAutomataState> transDiag;
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
		this();
		nfaToDfa(nfa);
		renameNfaStateToDfaState();
		
//		minifyStateNumber();
		
//		printDebugLog("STATES: ");
//		printDebugLog(this.internalStates);
//		printDebugLog("DIAGS: ");
//		printDebugLog(this.internalTransDiag);
		
		printDebugLog(this.stateDict);
		printDebugLog(this.transDiag);
		
//		printDebugLog(this.start + "," + this.end);
	}
	
	/*
	 * Use subset construction to convert NFA to DFA.
	 */
	private void nfaToDfa(FiniteAutomata nfa) {
		// Set symbolSet.
		this.symbolSet = nfa.symbolSet;
		
		List<List<FiniteAutomataState>> workList = new ArrayList<List<FiniteAutomataState>>();
		List<List<FiniteAutomataState>> Q = new ArrayList<List<FiniteAutomataState>>();
		
		List<FiniteAutomataState> dfaState0 = new ArrayList<FiniteAutomataState>();
		dfaState0.add(nfa.start);
		dfaState0 = nfa.epsilonClosure(dfaState0);
		this.internalStates.add(dfaState0);
		this.internalStart = dfaState0;
		
		printDebugLog(dfaState0);
		
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
						
						if (newDfaState.contains(nfa.end)) {
							// Mark as accept state:
//							assert this.internalEnd == null;
							this.internalEnd.add(newDfaState);
						}
						
						workList.add(newDfaState);
						Q.add(newDfaState);
//						printDebugLog("New state:");
//						printDebugLog(newDfaState);
//						
//						printDebugLog("after Q:");
//						printDebugLog(Q);
//						printDebugLog("");
						
						
//						printDebugLog("~~~~~~~~~~~~~ diag start ~~~~~~~~~~~");
//						printDebugLog(this.internalTransDiag);
//						printDebugLog("~~~~~~~~~~~~~~ diag end ~~~~~~~~~~");
						
					}
				}
			}
		}
	}
	
	private void renameNfaStateToDfaState() {
		for (List<FiniteAutomataState> nfaStates : this.internalStates) {
			FiniteAutomataState newDfaState = createStates(1).get(0);
			this.stateDict.put(nfaStates, newDfaState);
			
			if (nfaStates == this.internalStart) {
				this.start = newDfaState;
			}
			
			if (this.internalEnd.contains(newDfaState)) {
				this.end.add(newDfaState);
			}
		}
		
		for (List<FiniteAutomataState> nfaStates : this.internalTransDiag.getL1KeySet()) {
			for (InputSymbol input : this.internalTransDiag.getL2KeySet(nfaStates)) {
				for (List<FiniteAutomataState> destState : this.internalTransDiag.query(nfaStates, input)) {
					this.transDiag.update(this.stateDict.get(nfaStates), input, this.stateDict.get(destState));
				}
			}
		}
	}
	
	private void minifyStateNumber() {
		List<List<FiniteAutomataState>> partitions = new ArrayList<List<FiniteAutomataState>>();
		List<FiniteAutomataState> acceptPartition = new ArrayList<FiniteAutomataState>();
		List<FiniteAutomataState> nonAcceptPartition = new ArrayList<FiniteAutomataState>();
		for (FiniteAutomataState state : this.states) {
			if (state == this.end) {
				acceptPartition.add(state);
			} else {
				nonAcceptPartition.add(state);
			}
		}
		partitions.add(acceptPartition);
		partitions.add(nonAcceptPartition);
		
		List<List<FiniteAutomataState>> lastPartitions = new ArrayList<List<FiniteAutomataState>>();
		
		while (lastPartitions.size() != partitions.size()) {
			
			lastPartitions = partitions;
			partitions = new ArrayList<List<FiniteAutomataState>>();
			
			for (int i = 0; i < lastPartitions.size(); i ++) {
				List<FiniteAutomataState> partition = lastPartitions.get(i);
				
				for(int j = 0; j < partition.size(); j ++) {
					FiniteAutomataState state = partition.get(j);
					
				}
			}
		}
	}
	
	private List<List<FiniteAutomataState>> splitPartition(List<FiniteAutomataState> partition, List<List<FiniteAutomataState>> lastPartitions) {
		List<List<FiniteAutomataState>> newPartitions = new ArrayList<List<FiniteAutomataState>>();
		TwoStageHashMap<List<FiniteAutomataState>, InputSymbol, FiniteAutomataState> partitionMap = new TwoStageHashMap<List<FiniteAutomataState>, InputSymbol, FiniteAutomataState>();
		
		for (InputSymbol symbol : this.symbolSet) {
			for (FiniteAutomataState state : partition) {
				List<FiniteAutomataState> destStates = this.transDiag.query(state, symbol);
				
				if (destStates != null) {
					
				}
			}
		}
		return newPartitions;
	}
	
	
}
