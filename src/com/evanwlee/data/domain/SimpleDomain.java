package com.evanwlee.data.domain;

import java.sql.ResultSet;

import com.evanwlee.data.IPersistenceManager;
import com.evanwlee.data.PersistenceManagerFactory;

/**
 * Allows for persistence actions on Simple Domain objects, i.e. those without
 * any common column (save for id). E.g. no deactivated or lastmodified.
 * 
 * @author evan.l
 *
 */
public abstract class SimpleDomain extends AbstractDomain{
	
	
	public static final String THE_FOUR = "id, created";
	public static final String TABLE_PREFIX = "";
		
	protected IPersistenceManager pManager = PersistenceManagerFactory.current().getPersistenceManager();

	
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