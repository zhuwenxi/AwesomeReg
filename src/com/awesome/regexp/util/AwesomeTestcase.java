package com.awesome.regexp.util;

public class AwesomeTestcase {
	public Object result;
	public Object expectedValue;
	public boolean isPass;
	
	public AwesomeTestcase() {
		
	}
	
	public AwesomeTestcase(Object result) {
		this();
		this.result = result;
	}
	
	public AwesomeTestcase(Object result, String expectedValue) {
		this(result);
		this.expertToBe(expectedValue);
	}
	
	public void expertToBe(String expectedValue) {
		this.expectedValue = expectedValue;
		
		String resultString = (String)(this.result);
		
		this.isPass = resultString == expectedValue || resultString.equals(expectedValue);
	}
}
