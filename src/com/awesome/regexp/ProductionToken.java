package com.awesome.regexp;

import java.util.ArrayList;
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
	
	static {
		epsilon = new ProductionToken ("EPSILON", true);
		
		dollar = new ProductionToken ("$", true);
		
		dot = new ProductionToken("DOT", true);
		dot.isDotSymbol = true;
		
		ch = new ProductionToken("[0-1a-zA-Z]");
		
		ch.isCollection = true;
		ch.collection = new ArrayList<Character>();
		
		for (char c = '0'; c <= '9'; c++){
			ch.collection.add(c);
		}
		
		for (char c = 'a'; c <= 'z'; c++){
			ch.collection.add(c);
		}
		
		for (char c = 'A'; c <= 'Z'; c++){
			ch.collection.add(c);
		}
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
//		int hashCode = 0;
//			
//		for (int i = 0; i < this.text.length(); i ++) {
//			char ch = this.text.charAt(i);
//			hashCode += (10 * hashCode + ch); 
//		}
//		
//		return hashCode;
		return this.text.hashCode();
	}
	
}
