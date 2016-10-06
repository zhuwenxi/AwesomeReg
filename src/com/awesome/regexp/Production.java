package com.awesome.regexp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Production {

	public String productionString;

	public ProductionToken head;
	public List<ProductionToken> body;
	
	public AbstractSyntaxTree.Operator operator;
	public int operatorIndex;
	public int leftOperandIndex;
	public int rightOperandIndex;
	
	private static List<Character> letters;
	private static List<Character> numbers;

	private static Map<State, Map<Character, State>> transitionMap;

	static {
		//
		// Initialize the "letters" list. Valid letters are:
		// 'a'-'z', 'A'-'Z'.
		//
		letters = new ArrayList<Character>();
		char letter;

		// a-z:
		for (letter = 'a'; letter <= 'z'; letter++) {
			Production.letters.add(letter);
		}

		// A-Z
		for (letter = 'A'; letter <= 'Z'; letter++) {
			Production.letters.add(letter);
		}

		//
		// Initialize the "numbers" list. Valid nubmers are '0'-'9'
		//
		numbers = new ArrayList<Character>();
		char number;

		// 0-9:
		for (number = '0'; number <= '9'; number++) {
			Production.numbers.add(number);
		}

		//
		// Initialize the "transitionMap"
		//
		Production.transitionMap = new HashMap<>();
	}

	public Production() {
		this.body = new ArrayList<ProductionToken>();
		
		this.operator = null;
		this.operatorIndex = -1;
		this.leftOperandIndex = -1;
		this.rightOperandIndex = -1;
	}

	public Production(String productionString) {
		this.productionString = productionString;
	}
	
	public Production(ProductionToken head, List<ProductionToken> body){
		this();
		this.head = head;
		this.body = body;
	}

	@Override
	public String toString() {
		String retVal = "";
		
		retVal += this.head.toString();
		retVal += "->";
		
		for(ProductionToken productionToken : this.body){
			retVal += productionToken.toString();
			retVal += ' ';
		}
		
		return retVal;
	}
	
	@Override
	public Production clone() {
		Production cloneObj = new Production();
		
		cloneObj.head = (ProductionToken)this.head;
		
		for (ProductionToken pt : this.body) {
			cloneObj.body.add(pt);
		}
		
		cloneObj.operator = this.operator;
		cloneObj.leftOperandIndex = this.leftOperandIndex;
		cloneObj.rightOperandIndex = this.rightOperandIndex;
		cloneObj.operatorIndex = this.operatorIndex;
		
		return cloneObj;
	}
	
	@Override 
	public boolean equals(Object o) {
		Production that = (Production)o;
		
		return this.head.equals(that.head) && this.body.equals(that.body);
	}
	
	@Override
	public int hashCode() {
		return this.head.hashCode() + this.body.hashCode();
	}
}