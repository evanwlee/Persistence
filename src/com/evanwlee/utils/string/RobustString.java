package com.evanwlee.utils.string;


public class RobustString{

	private String containedString = null;
	

	public RobustString(String val){
		this.containedString = val;
	}

	/**
	 * Purpose: To check if a string is null or empty after trim().
	 * 
	 * @return boolean false if not null or empty, true otherwise.
	 */
	public boolean isEmpty() {
		if (containedString == null || containedString.trim().length() == 0) {
			return true;
		}
		return false;
	}

	public boolean containsSpaces() {
		if (this.isEmpty()) {
			return false;
		}
		if (containedString.trim().indexOf(' ') > -1) {
			return true;
		}
		return false;
	}

	/**
	 * @param string
	 * @return String
	 */
	public String trim() {
		if (this.isEmpty()) {
			return "";
		}
		return containedString.trim();
	}
	
	public String getWrappedString(){
		return containedString;
	}
	public String toString(){
		return containedString;
	}

	  
	
	public static void main(String[] args){
		RobustString string = new RobustString(" Hello World ");
		System.out.println("Original Value: '" +string+"'");
		System.out.println("Does it contain embedded spaces: " +string.containsSpaces());
		System.out.println("Original Value trimmed: '" +string.trim()+"'");
		System.out.println("Is empyt: " +string.isEmpty());
		System.out.println("zero fill: " +StringUtils.prefill("EVAN", "E",20));
		
	}

}
