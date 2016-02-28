package com.awesome.regexp;

import java.util.ArrayList;
import java.util.List;

public class ContextFreeGrammar {
	
	private List<Production> productions;
	
	public ContextFreeGrammar(){
		productions = new ArrayList<Production>();
	}
	
	/*public ContextFreeGrammar(List<Production> produtions)
	{
		this();
		this.productions = produtions;
	}*/
	
	public ContextFreeGrammar(String[] grammarStrings){
		this();
		for (String grammarItem : grammarStrings)
		{
			Production production =  new Production(grammarItem);
			this.productions.add(production);
		}
	}
	
	public List<Production> getProductions(){
		return productions;
	}
	
	@Override
	public String toString(){
		return super.toString();
	}
}
