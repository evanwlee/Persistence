package com.evanwlee.data.domain;


import java.sql.ResultSet;


/**
 * Allows for persistence actions on Domain objects.
 * 
 *
 */
public abstract class Domain extends AbstractDomain{
	
	private String created;
	private String lastModified;
	private String deactivated;
	
	public static final String THE_FOUR = "id, created, modified, deactivated";
	public static final String TABLE_PREFIX = "app_";

	

	public String getCreated(){
		return created;
	}
	
	public void setCreated(String created){
		this.created = created;
	}
	
	public String getModified() {
		return lastModified;
	}

	public void setModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public String getDeactivated() {
		return deactivated;
	}

	public void setDeactivated(String deactivated) {
		if("".equals(deactivated)){
			deactivated = null;
		}
		this.deactivated = deactivated;
	}
	
	//Each subclass needs to define how to Insert, Select and Update itself

	public abstract String tableName();
	
	/**
	 * Set the ID, Created, Deactivated and Modified attributes on the Domain
	 * object from the database record
	 * @param record
	 */
	protected abstract void createFromRecord(ResultSet record);
	
	/**
	 * Query that defines set of fields that make up a compisite
	 * key for the domain object so that duplicates can be detected
	 * based on the key.
	 * <br/><br/>
	 * <b>Compiste key</b> a combination of two or more columns in a table that can be used to uniquely identify each row in the table
	 * 
	 * 
	 * @return sql for find matching record based on compisite key. If returns
	 * blank then all the attributes of the domain object will be used to build the query
	 */
	protected abstract String getCompositeKeySelectWhereClause();
	
}