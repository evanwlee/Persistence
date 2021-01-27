package com.evanwlee.data.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import com.evanwlee.data.IPersistenceManager;
import com.evanwlee.utils.token.IReturnToken;

public interface IDomain {
	public List<IDomain> fetch();
	public List<IDomain> fetch(int max);
	public IDomain fetchOrCreate();
	public IDomain save();
	public IDomain update();
	public IReturnToken delete();
	
	public PreparedStatement createInsertPreparedStatement(Connection conn) throws Exception;
	public PreparedStatement createSelectPreparedStatement(Connection conn, int limit, boolean byCompositeKey) throws Exception;
	public PreparedStatement createUpdatePreparedStatement(Connection conn) throws Exception;
	
	
	public IReturnToken modificationStatus();
	public String csvResults();
	public void setPersistenceManager(IPersistenceManager mgr);
	
	
	//creates
	public String createInsertQuery();
	public String createSelectQuery();
	public String createUpdateQuery();
	public String createDeleteQuery();
	
	public String createCompositeKeySelectQuery();
	
	public String tableName();
	
	public Long getId();
}
