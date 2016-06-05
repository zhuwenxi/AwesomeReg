package com.awesome.regexp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TwoStageHashMap<L1Key, L2Key, Value> {
	public Map<L1Key, Map<L2Key, List<Value>>> impl;
	
	public TwoStageHashMap() {
		this.impl = new HashMap<L1Key, Map<L2Key, List<Value>>>();
	}
	
	public void update(L1Key key1, L2Key key2, Value value) {
		if (key1 == null || key2 == null) {
			return;
		}
		
		if (!this.impl.containsKey(key1)) {
			this.impl.put(key1, new HashMap<L2Key, List<Value>>());
		}
		
		Map<L2Key, List<Value>> secondStageHashMap = this.impl.get(key1);
		assert secondStageHashMap != null;
		
		if (secondStageHashMap.get(key2) == null) {
			List<Value> valueList = new ArrayList<Value>();
			valueList.add(value);
			secondStageHashMap.put(key2, valueList);
		} else {
			List<Value> valueList = secondStageHashMap.get(key2);
			valueList.add(value);
		}
	}
	
	public Set<L1Key> getL1KeySet() {
		return this.impl.keySet();
	}
	
	public Set<L2Key> getL2KeySet(L1Key key1) {
		if (key1 == null) {
			return null;
		}
		
		Map<L2Key, List<Value>> secondStageHashMap = this.impl.get(key1);
		
		return secondStageHashMap != null ? secondStageHashMap.keySet() : null;
	}
	
	public List<Value> query(L1Key key1, L2Key key2) {
		if (key1 == null || key2 == null) {
			return null;
		}
		
		Map<L2Key, List<Value>> secondStageHashMap = this.impl.get(key1);
		
		if (secondStageHashMap != null) {
			return secondStageHashMap.get(key2);
		} else {
			// Should not get here.
			return null;
		}
		
	}
	
	@Override
	public String toString() {
		StringBuffer retStr = new StringBuffer();
		
		Set<L1Key> stageOneKeys = this.impl.keySet();
		
		for (L1Key stageOneKey : stageOneKeys) {
			Map<L2Key, List<Value>> secondStageHashMap = this.impl.get(stageOneKey);
			assert secondStageHashMap != null;
			
			Set<L2Key> stageTwoKeys = secondStageHashMap.keySet();
			
			for (L2Key stageTwoKey : stageTwoKeys) {
				retStr.append("(" + stageOneKey + ", " + stageTwoKey + "): ");
				List<Value> valueList = secondStageHashMap.get(stageTwoKey);
				
				for (Value value : valueList) {
					retStr.append(value + " ");
				}
				
				retStr.append("\n");
			}
			
		}
		return retStr.toString();
	}
}
