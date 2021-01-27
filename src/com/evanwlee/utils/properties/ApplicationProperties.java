package com.evanwlee.utils.properties;


public enum ApplicationProperties {
	EXLUDED_DOC_MAN_CHARACTERS(),
	UNKNOWN();
	
	
	public static ApplicationProperties determinValue(String value){
		if( "".equals(value)){
			return UNKNOWN;
		}
		
		for (ApplicationProperties act : ApplicationProperties.values()){
    		if(act.toString().equalsIgnoreCase(value)){
    			return act;
    		}
    	}

		return UNKNOWN;
	}

}
