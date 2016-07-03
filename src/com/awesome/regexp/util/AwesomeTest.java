package com.awesome.regexp.util;

import java.util.ArrayList;
import java.util.List;

public class AwesomeTest {
	public List<AwesomeTestcase> testcases;
	private List<AwesomeTestcase> failurecase;
	
	public AwesomeTest() {
		this.testcases = new ArrayList<AwesomeTestcase>();
		this.failurecase = new ArrayList<AwesomeTestcase>();
	}
		
	public void run() {
		for (AwesomeTestcase testcase : this.testcases) {
			if (testcase != null) {
				boolean isPass = testcase.isPass;
				
				if (!isPass) {
					printError();
					this.failurecase.add(testcase);
				} else {
					printPass();
				}
			}
		}
		
		System.out.print("\n");
		
		if (this.failurecase.size() == 0) {
			System.out.println("All " + this.testcases.size() + " testcases passed.");
		} else {
			System.out.println("Fail cases:");
			for (AwesomeTestcase testcase : this.failurecase) {
				String strToPrint = String.format("Expect \"%1s\" to be %2s", testcase.result, testcase.expectedValue);
				System.out.println(strToPrint);
			}
		}
	}
	
	public void addCase(AwesomeTestcase testcase) {
		this.testcases.add(testcase);
	}
	
	public void addCase(AwesomeTestcase[] testcases) {
		for (AwesomeTestcase testcase : testcases) {
			this.testcases.add(testcase);
		}
	}
	
	public void printError() {
		System.out.print("X");
	}
	
	public void printPass() {
		System.out.print("*");
	}
	
}
