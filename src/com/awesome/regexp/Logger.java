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
	
	public static void println(boolean config, Object msg) {
		print(config, msg);
		print(config, "\n");
	}
}
