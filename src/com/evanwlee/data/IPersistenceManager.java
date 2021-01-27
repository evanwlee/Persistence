package com.evanwlee.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.evanwlee.data.domain.IDomain;
import com.evanwlee.utils.token.IReturnToken;

public interface IPersistenceManager {
	
	
	public void safeClose(Connection conn);

	public void safeClose(ResultSet res);

	public void safeClose(Statement st);
	
	/**
	 * User specified sql
	 * @param query string of standard sql
	 * @return Token containing fetched rows and/or messages.
	 */
	public IReturnToken doModificationQuery(final String query);
	/**
	 * @param query
	 * @return List<HashMap of Column Name to Column String value (with best guess at conversion)>
	 */
	public IReturnToken doSelectQuery(final String query);

	
	public Connection createConnection();
	
	
	/**
	 * @param query string of standard select sql
	 * @return A database resultset representing the fetch rows.
	 */
	//public ResultSet select(String query);
	public ResultSet select(IDomain domainObject);
	public ResultSet select(IDomain domainObject, int max);
	
	public ResultSet selectByCompositeKey(IDomain domainObject);

	/**
	 * @param query is the actaul sql to be executed. 
	 * The caller needs to clean the sql before passing.
	 * 
	 * @return result of insert operation
	 */
	//public IReturnToken insert(String query);
	/**
	 * @param domainObject - Domain object to insert as record. This
	 * function uses a <strong>PerparedStatement</strong> so it is SQL safe.
	 * @return result of insert operation
	 */
	public IReturnToken insert(IDomain domainObject);
	/**
	 * @param query with update sql
	 * @return result of update operation
	 */
	//public long update(String query);
	public long update(IDomain domainObject);
	/**
	 * @param query with standard DELETE record sql
	 * @return result of delete operation
	 */
	//public IReturnToken delete(String query);
	public IReturnToken delete(IDomain domainObject);
}
