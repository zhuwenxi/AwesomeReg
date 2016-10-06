package com.awesome.regexp;

import java.util.List;

public class ProductionToken {
	
	public String text;
	
	public boolean isTerminal;
	
	public boolean isNonTerminal;
	
	public boolean isCollection;
	
	public boolean isDotSymbol;
	
	public List<Character> collection;
	
	static public final ProductionToken epsilon;
	
	static public final ProductionToken dollar;
	
	static public final ProductionToken dot;
	
	static public final ProductionToken ch;
	
	static public final ProductionToken star;
	
	static public final ProductionToken leftParenthesis;
	
	static public final ProductionToken rightParenthesis;
	
	static public final ProductionToken verticalBar;
	
	static {
		epsilon = new ProductionToken ("EPSILON", true);
		
		dollar = new ProductionToken ("$", true);
		
		dot = new ProductionToken("DOT", true);
		dot.isDotSymbol = true;
		
//		ch = new ProductionToken("[0-1a-zA-Z]");
		ch = new ProductionToken("Char");
		
//		ch.isCollection = true;
//		ch.collection = new ArrayList<Character>();
//		
//		for (char c = '0'; c <= '9'; c++){
//			ch.collection.add(c);
//		}
//		
//		for (char c = 'a'; c <= 'z'; c++){
//			ch.collection.add(c);
//		}
//		
//		for (char c = 'A'; c <= 'Z'; c++){
//			ch.collection.add(c);
//		}
		
		star = new ProductionToken("*", true);
		
		leftParenthesis = new ProductionToken("(", true);
		
		rightParenthesis = new ProductionToken(")", true);
		
		verticalBar = new ProductionToken("|", true);
	}
	
	
	public ProductionToken(){
		this.isTerminal = true;
		this.isNonTerminal = false;
		this.isCollection = false;
		this.isDotSymbol = false;
	}
	
	public ProductionToken(String text){
		this();
		this.text = text;
	}
	
	public ProductionToken(String text, boolean isTerminal){
		this();
		this.text = text;
		this.isTerminal = isTerminal;
		this.isNonTerminal = !isTerminal;
	}
	
	@Override
	public String toString(){
		return this.text;
	}
	
	@Override
	public boolean equals(Object o) {
		
		ProductionToken that = (ProductionToken)o;
		
		if (o == null) {
			return false;
		}
		return this.isTerminal == that.isTerminal && this.isNonTerminal == that.isNonTerminal && this.isCollection == that.isCollection && this.isDotSymbol == that.isDotSymbol && this.text.equals(that.text);
	}
	
	@Override
	public int hashCode() {
		return this.text.hashCode();
	}
	
}
