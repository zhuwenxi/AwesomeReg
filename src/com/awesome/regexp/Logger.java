package com.awesome.regexp;

public class Logger {
	public static void print(boolean config, Object msg) {
		if (config) {
			print(msg);
		}
	}
	
	public static void print(Object msg) {
		System.out.print(msg);
	}
}
