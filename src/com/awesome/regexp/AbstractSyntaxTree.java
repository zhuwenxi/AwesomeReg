package com.awesome.regexp;

public class AbstractSyntaxTree {
	
	public enum Operator {
		ALTER, CONCAT, REPEAT, UNIT
	}
	
	
	public class TreeNode {
		public TreeNode leftOperand;
		public TreeNode rightOperand;
		public Operator operator;
		
		public TreeNode() {
			
		}
		
		public TreeNode(Operator operator, TreeNode left, TreeNode right) {
			this.operator = operator;
			this.leftOperand = left;
			this.rightOperand = right;
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
