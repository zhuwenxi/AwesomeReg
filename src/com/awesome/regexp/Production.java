package com.awesome.regexp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Production {

	private String productionString;

	private ProductionToken head;
	private List<ProductionToken> body;

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

	}

	public Production(String productionString) {

		this.productionString = productionString;

		generateProduction(productionString);
	}

	private void generateProduction(String productionString) {

		int cur = 0;
		State state = State.START;

		int start = 0;
		int end = 0;

		while (state != State.ERROR && !State.isAccept(state)) {

			char currentChar = productionString.charAt(cur);

			switch (state) {
			case START:
				break;
			default:
				System.out.println("Oops!");
				break;
			}
		}
	}

	@Override
	public String toString() {
		return this.productionString;
	}
}

enum State {
	START, 
	HEAD, 
	HEAD_ACCEPT(true), 
	ARROW_DASH, 
	ARROW_ACCEPT(true), 
	BODY_TOKEN, 
	BODY_TOKEN_ACCEPT(true), 
	PIPE_SYMBOL(true), 
	COLLECTION_LEFT_SQUARE_BRACKET, 
	COLLECTION_START_CHAR, 
	COLLECTION_DASH_CHAR, 
	COLLECTION_END_CHAR, 
	COLLECTION_ACCEPT(true), 
	ERROR;

	public final boolean isAccept;

	private State() {
		this(false);
	}

	private State(boolean isAccept) {
		this.isAccept = isAccept;
	}

	public static boolean isAccept(State self) {
		return self.isAccept;
	}
}
