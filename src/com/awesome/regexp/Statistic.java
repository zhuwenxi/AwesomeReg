package com.awesome.regexp;

import java.util.HashMap;
import java.util.Map;

public class Statistic {
	
	public static enum Tag  {
		
		Automata, 
		DFA,
		
	}
	
	private static Map<Tag, TimeStamp> timeCounter;
	
	static {
		//
		// Initialize the time counter hash-table, set keys to the tags in enum "Tag", and values to -1. 
		//
		timeCounter = new HashMap<>();
		
		for (Tag t : Tag.values()) {
			timeCounter.put(t, null);
		}
	}
	
	public static void start(Tag tag) {
		timeCounter.put(tag, new TimeStamp());
	}
	
	public static void increase(Tag tag, int time) {
		TimeStamp ts = timeCounter.get(tag);
		ts.update(time);
	}
}

class TimeStamp {
	int start;
	int end;
	
	public TimeStamp() {
		this(0, 0);
	}
	
	public TimeStamp(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	public void update(int end) {
		this.end = end;
	}
}