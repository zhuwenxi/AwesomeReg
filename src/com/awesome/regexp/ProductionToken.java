package com.awesome.regexp;

import java.util.List;

public class ProductionToken {
	
	public String text;
	
	public boolean isTerminal;
	
	public boolean isNonTerminal;
	
	public boolean isCollection;
	
	public boolean isDotSymbol;
	
	public List<Character> collection;
	
	
	
	
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
		return this.isTerminal == that.isTerminal && this.isNonTerminal == that.isNonTerminal && this.isCollection == that.isCollection && this.isDotSymbol == this.isDotSymbol && this.text.equals(that.text);
	}
	
}
