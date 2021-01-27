package com.evanwlee.utils.properties;

public interface ApplicationPropertiesCacheMBean {
	
	/**
	 * Purge the branch time zone conversion info cached.
	 * The info will be lazily re-read in.
	 */
	public String purgeCache(); 

}
