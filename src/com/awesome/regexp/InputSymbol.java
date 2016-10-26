package com.awesome.regexp;

class InputSymbol {
	public static InputSymbol epsilon;
	
	public boolean isEpsilon;
	
	public String face;
	
	public ProductionToken type;
	
	static {
		InputSymbol.epsilon = new InputSymbol();
		InputSymbol.epsilon.isEpsilon = true;
		InputSymbol.epsilon.face = "EPSILON";
	}
	public InputSymbol() {
		this.isEpsilon = false;
	}
	
	public InputSymbol(char ch, ProductionToken symbolType) {
		this();
		this.face = Character.toString(ch);
		this.type = symbolType;
	}
	
	public InputSymbol(String face, ProductionToken symbolType) {
		this();
		this.face = face;
		this.type = symbolType;
	}
	
	@Override
	public String toString() {
		return face;
	}
	
	@Override
	public boolean equals(Object o) {
		InputSymbol that = (InputSymbol)o;
		return that != null && this.face.equals(that.face) && this.isEpsilon == that.isEpsilon;
	}
	
	@Override
	public int hashCode() {
		// Dirty implementation ...
		return 100;
	}
}

