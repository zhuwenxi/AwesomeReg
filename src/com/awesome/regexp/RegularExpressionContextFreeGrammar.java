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
		
		production.operator = AbstractSyntaxTree.Operator.ALTER;
		production.leftOperandIndex = 0;
		production.rightOperandIndex = 2;
		production.operatorIndex = 1;
		
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
		
		production.operator = AbstractSyntaxTree.Operator.CONCAT;
		production.leftOperandIndex = 0;
		production.rightOperandIndex = 1;
		
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
		body.add(new ProductionToken("*", true));
		
		production = new Production(head, body);
		
		production.operator = AbstractSyntaxTree.Operator.REPEAT;
		production.leftOperandIndex = 0;
		production.operatorIndex = 1;
		
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
		body.add(new ProductionToken("(", true));
		body.add(new ProductionToken("Regexp", false));
		body.add(new ProductionToken(")", true));
		
		production = new Production(head, body);
		
		production.operator = AbstractSyntaxTree.Operator.UNIT;
		production.leftOperandIndex = 1;
		
		this.productions.add(production);
		
		
		// Unit -> Char
		head = new ProductionToken("Unit", false);
		
		body = new ArrayList<ProductionToken>();
		ProductionToken bodyItem = new ProductionToken("Char");
		body.add(bodyItem);
		
		production = new Production(head, body);
		this.productions.add(production);
	}
}
