package com.awesome.regexp;

public class AbstractSyntaxTree {
	
	public static enum Operator {
		ALTER, CONCAT, REPEAT, UNIT;
		
		public String toString() {
			switch(this){
			case ALTER:
				return "|";
			case CONCAT:
				return ".";
			case REPEAT:
				return "*";
			case UNIT:
				return "()";
			default:
				assert false;
				return "UNKNOWN";
			}
		}
		
	}
	
	
	public static class TreeNode {
		public TreeNode leftOperand;
		public TreeNode rightOperand;
		public Operator operator;
		
		public InputSymbol text;
		
		public TreeNode() {
			this.leftOperand = null;
			this.rightOperand = null;
			this.operator = null;
			this.text = null;
		}
		
		public TreeNode(Operator operator, TreeNode left, TreeNode right) {
			this();
			this.operator = operator;
			this.leftOperand = left;
			this.rightOperand = right;
		}
		
		public TreeNode(InputSymbol text) {
			this();
			this.text = text;
		}
		
		public void printSelf(int level) {
			
			if (this.operator != null) {
				System.out.println(this.operator);
			}
			
			if (this.text != null) {
				System.out.println(this.text);
			}
			
			if (this.leftOperand != null) {
				for (int i = 0; i < level; i ++) {
					if (i == level - 1) {
						System.out.print(">>>>>> ");
					} else {
						System.out.print("          ");
					}
				}
				
				this.leftOperand.printSelf(level + 1);
			}
			
			if (this.rightOperand != null) {
				for (int i = 0; i < level; i ++) {
					if (i == level - 1) {
						System.out.print(">>>>>> ");
					} else {
						System.out.print("          ");
					}
				}
				this.rightOperand.printSelf(level + 1);
			}
		}
		
	}	
	
	
	
	
	public TreeNode root;

	public AbstractSyntaxTree(){
		
	}
	
	public AbstractSyntaxTree(TreeNode root) {
		this();
		this.root = root;
	}
}
