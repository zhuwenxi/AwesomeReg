package com.awesome.regexp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Statistic {
	
	public static enum Tag  {
		AllSpecs,
		Automata, 
		FormatFixup,
		AST,
		NFA,
		DFA,
		FuncMatch,
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
		TimeStamp ts = timeCounter.get(tag);
		
		if (ts == null) {
			ts = new TimeStamp();
			timeCounter.put(tag, ts);
		}
		
		ts.lastUpdateTime = new Date();
	}
	
	public static void pause(Tag tag) {
		TimeStamp ts = timeCounter.get(tag);
		ts.update();		
	} 
	
	public static void print(Tag tag) {
		TimeStamp ts = timeCounter.get(tag);
		
		if (ts == null) return;
		
		Logger.print(String.format("Cost on %1$-15s: ", tag.name()));
		Logger.println(ts.timeInterval);
	}
	
	public static void print() {
		Logger.tprint(true, new DebugCode() {

			@Override
			public void code() {
				for (Tag t : Tag.values()) {
					print(t);
				}
			}
			
		}, "Stat for all specs");
		
	}
	
	public static void printPercent(long total) {
		Logger.tprint(true, new DebugCode() {

			@Override
			public void code() {
				for (Tag t : Tag.values()) {
					TimeStamp ts = timeCounter.get(t);
					
					if (ts == null) return;
					
					Logger.print(String.format("Cost on %1$-15s: ", t.name()));
					Logger.print(String.format("%1$5d", ts.timeInterval));
					Logger.println(" [" + ((float)ts.timeInterval / total) * 100 + "%]");
					
				}
			}
			
		}, "Stat for all specs");
	}
	
	public static void printPercent() {
		long total = timeCounter.get(Tag.AllSpecs).timeInterval;
		printPercent(total);
	}
}

class TimeStamp {
	long timeInterval;
	Date lastUpdateTime;
	
	public TimeStamp() {
		this(0, null);
	}
	
	public TimeStamp(long timeInterval, Date lastUpdateTime) {
		this.timeInterval = timeInterval;
		this.lastUpdateTime = lastUpdateTime;
	}
	
	public void update() {
		Date currentDate = new Date();
		long newInterval = currentDate.getTime() - lastUpdateTime.getTime();
		this.timeInterval =  newInterval + this.timeInterval;
		this.lastUpdateTime = currentDate;
	}
}