package com.awesome.regexp;

import java.util.ArrayList;
import java.util.List;

public class RegularExpressionContextFreeGrammar extends ContextFreeGrammar{
	
	public RegularExpressionContextFreeGrammar()
	{		
		super();
		initRegularExpressionContextFreeGrammar();	
	}
	
	//
	// Hard code the context-free grammar of regular expression. 
	//
	public void initRegularExpressionContextFreeGrammar()
	{
		Production production;
		ProductionToken head;
		List<ProductionToken> body;
		
		// Regexp' -> Regexp
		head = new ProductionToken("Regexp'", false);
		
		body = new ArrayList<ProductionToken>();
		body.add(new ProductionToken("Regexp", false));
		
		production = new Production(head, body);
		this.productions.add(production);
		
		// Regexp -> Regexp | Concat
		head = new ProductionToken("Regexp", false);
		
		body = new ArrayList<ProductionToken>();
		body.add(new ProductionToken("Regexp", false));
		body.add(new ProductionToken("|", true));
		body.add(new ProductionToken("Concat", false));
		
		production = new Production(head, body);
		this.productions.add(production);
		
		
		// Regexp -> Concat
		head = new ProductionToken("Regexp", false);
		
		body = new ArrayList<ProductionToken>();
		body.add(new ProductionToken("Concat", false));
		
		production = new Production(head, body);
		this.productions.add(production);
		
		
		// Concat -> Concat Repeat
		head = new ProductionToken("Concat", false);
		
		body = new ArrayList<ProductionToken>();
		body.add(new ProductionToken("Concat", false));
		body.add(new ProductionToken("Repeat", false));
		
		production = new Production(head, body);
		this.productions.add(production);
		
		
		// Concat -> Repeat
		head = new ProductionToken("Concat", false);
		
		body = new ArrayList<ProductionToken>();
		body.add(new ProductionToken("Repeat", false));
		
		production = new Production(head, body);
		this.productions.add(production);
		
		
		// Repeat -> Unit *
		head = new ProductionToken("Repeat", false);
		
		body = new ArrayList<ProductionToken>();
		body.add(new ProductionToken("Unit", false));
		body.add(new ProductionToken("*", false));
		
		production = new Production(head, body);
		this.productions.add(production);
		
		
		// Repeat -> Unit
		head = new ProductionToken("Repeat", false);
		
		body = new ArrayList<ProductionToken>();
		body.add(new ProductionToken("Unit", false));
		
		production = new Production(head, body);
		this.productions.add(production);
		
		
		// Unit -> ( Regexp )
		head = new ProductionToken("Unit", false);
		
		body = new ArrayList<ProductionToken>();
		body.add(new ProductionToken("(", false));
		body.add(new ProductionToken("Regexp", false));
		body.add(new ProductionToken(")", false));
		
		production = new Production(head, body);
		this.productions.add(production);
		
		
		// Unit -> [0-1a-zA-Z]
		head = new ProductionToken("Unit", false);
		
		body = new ArrayList<ProductionToken>();
		ProductionToken bodyItem = new ProductionToken("[0-1a-zA-Z]");
		
		bodyItem.isCollection = true;
		bodyItem.collection = new ArrayList<Character>();
		
		for (char c = '0'; c <= '9'; c++){
			bodyItem.collection.add(c);
		}
		
		for (char c = 'a'; c <= 'z'; c++){
			bodyItem.collection.add(c);
		}
		
		for (char c = 'A'; c <= 'Z'; c++){
			bodyItem.collection.add(c);
		}
		
		body.add(bodyItem);
		
		production = new Production(head, body);
		this.productions.add(production);
	}
}
