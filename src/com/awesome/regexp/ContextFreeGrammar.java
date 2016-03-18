package com.awesome.regexp;

import java.util.ArrayList;
import java.util.List;

public class ContextFreeGrammar {
	
	protected List<Production> productions;
	
	public ContextFreeGrammar(){
		productions = new ArrayList<Production>();
	}
		
	public ContextFreeGrammar(String[] grammarStrings){
		this();
		for (String grammarItem : grammarStrings)
		{
			Production production =  new Production(grammarItem);
			this.productions.add(production);
		}
	}
	
	public ContextFreeGrammar(List<Production> productions){
		this.productions = productions;
	}
	
	@Override
	public String toString(){
		String retVal = "";
		
		for (Production prod : productions){
			retVal += prod.toString();
			retVal += '\n';
		}
		
		return retVal;
	}
}
