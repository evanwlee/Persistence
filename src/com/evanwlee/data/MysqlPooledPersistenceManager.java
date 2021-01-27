package com.evanwlee.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.evanwlee.data.domain.IDomain;
import com.evanwlee.utils.logging.LoggerFactory;
import com.evanwlee.utils.properties.PropertyLoader;
import com.evanwlee.utils.security.encryption.DesEncrypter;
import com.evanwlee.utils.token.IReturnToken;
import com.evanwlee.utils.token.ReturnTokenImpl;
import com.evanwlee.utils.token.StatusMessageImpl;

public class MysqlPooledPersistenceManager implements IPersistenceManager {
	private Logger log = LoggerFactory.getLogger(MysqlPooledPersistenceManager.class.getName());
	
	private static final int MAX_POOL_SIZE = 8;


	private volatile static MysqlPooledPersistenceManager currentManager = null;
	public static final String DB_CONFIG = new String("resources.persistence.properties");

	private String dbhost = new String("");
	private String dbport = new String("");
	private String db = new String("");
	private String dbuser = new String("");
	private String dbpass = new String("");

	MysqlPooledPersistenceManager() {
		this("");
	}

	private volatile static Map<String, MysqlPooledPersistenceManager> instances = new HashMap<String, MysqlPooledPersistenceManager>();

	MysqlPooledPersistenceManager(String app) {

		if (!"".equals(app)) {
			app = app + ".";
		}

		Properties dbconfig = PropertyLoader.loadProperties(DB_CONFIG);
		String env = dbconfig.getProperty("DB.ENV", "LOCAL");
		if ("".equals(env) || env.equals(null)) {
			env = "LOCAL";
		}
		dbhost = dbconfig.getProperty("DB." + env + "." + app + "HOST", "localhost");
		dbport = dbconfig.getProperty("DB." + env + "." + app + "PORT", "8889");
		db = dbconfig.getProperty("DB." + env + "." + app + "DB", "per_test");
		dbuser = dbconfig.getProperty("DB." + env + "." + app + "USER", "root");
		dbpass = dbconfig.getProperty("DB." + env + "." + app + "PASS", "root");

		dbpass = new DesEncrypter().decrypt(dbpass);
		log.info("ENV=" + env + ", HOST:" + dbhost + ", PORT:" + dbport + ", DB:" + db + ", USER:" + dbuser);

		initializeConnectionPool();
	}

	/**
	 * Returns the current domain factory manager for the application.
	 */
	public static MysqlPooledPersistenceManager current() {
		currentManager = instances.get("");
		if (currentManager == null) {
			synchronized (MysqlPooledPersistenceManager.class) {
				if (currentManager == null) {
					currentManager = new MysqlPooledPersistenceManager();
					instances.put("", currentManager);
				}
			}
		}
		return currentManager;
	}

	public static MysqlPooledPersistenceManager current(String host) {
		currentManager = instances.get(host);
		if (currentManager == null) {
			synchronized (MysqlPooledPersistenceManager.class) {
				if (currentManager == null) {
					currentManager = new MysqlPooledPersistenceManager(host);
					instances.put(host, currentManager);
				}
			}
		}
		return currentManager;
	}

	public long update(final IDomain domain) {
		String query = domain.createUpdateQuery();
		Statement stmt = null;
		Connection conn = null;
		long id = domain.getId();
		IReturnToken result = new ReturnTokenImpl();
		if (id <= 0) {
			result.setFailure(true);
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot Do Update"));
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error No ID provided"));
			return id;
		}

		try {
			conn = createConnection();
			if (conn != null) {
				stmt = conn.createStatement();
				// System.out.println("update:"+query);
				stmt.executeUpdate(query);
				// int rowsImpacted = stmt.executeUpdate( query );
				// what if no rows impacted
				// id = this.parseIdFromUpdateQuery(query);
			}
		} catch (SQLException ex) {
			this.handleSqlError(ex, query);
		} catch (Exception e) {
			log.error("Update query coult not be executed: " + query + "-" + e.toString());
		} finally {
			safeClose(stmt);
			safeClose(conn);
		}
		return id;
	}

	public IReturnToken insert(final IDomain domain) {
		String query = domain.createInsertQuery();
		Statement stmt = null;
		Connection conn = null;
		long id = 0;
		ResultSet rs = null;

		IReturnToken result = new ReturnTokenImpl();

		try {
			conn = createConnection();
			if (conn != null) {
				stmt = conn.createStatement();
				// System.out.println("insert:"+query);
				// select max id from table
				stmt.executeUpdate(query);
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				while (rs.next()) {
					id = rs.getLong(1);
				}
				result.setResult(id);
				// System.out.println("ID "+id+" records in the database.");
			}
		} catch (Exception e) {
			result.setFailure(true);
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot Insert " + query));
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error " + e.getMessage()));

			log.error("Insert query coult not be executed: " + query + "-" + e.toString());
			// e.printStackTrace();
		} finally {
			safeClose(stmt);
			safeClose(conn);
		}

		return result;
	}

	public IReturnToken delete(final IDomain domain) {
		String query = domain.createDeleteQuery();
		Statement stmt = null;
		Connection conn = null;

		IReturnToken result = new ReturnTokenImpl();

		if (!"".equals(query)) {
			try {
				conn = createConnection();
				if (conn != null) {
					stmt = conn.createStatement();
					// System.out.println("insert:"+query);
					// select max id from table
					stmt.executeUpdate(query);
					log.debug("Delete query was executed: " + query);
				}
			} catch (Exception e) {
				result.setFailure(true);
				result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot Delete " + query));
				result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error " + e.getMessage()));

				log.error("Delete query coult not be executed: " + query + "=" + e.toString());
				// e.printStackTrace();
			} finally {
				safeClose(stmt);
				safeClose(conn);
			}
		}

		return result;
	}

	public IReturnToken doModificationQuery(final String query) {
		Statement stmt = null;
		Connection conn = null;

		IReturnToken result = new ReturnTokenImpl();

		if (!"".equals(query)) {
			try {
				conn = createConnection();
				if (conn != null) {
					stmt = conn.createStatement();
					// System.out.println("insert:"+query);
					// select max id from table
					stmt.executeUpdate(query);
					log.debug("Modification query was executed: " + query);
				}
			} catch (Exception e) {
				result.setFailure(true);
				result.addMessage(
						new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot execute user query: " + query));
				result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error " + e.getMessage()));

				log.error("MODIFICATION query coult not be executed: " + query + "=" + e.toString());
				// e.printStackTrace();
			} finally {
				safeClose(stmt);
				safeClose(conn);
			}
		}

		return result;
	}
	
	public IReturnToken doSelectQuery(final String query) {
		Statement stmt = null;
		Connection conn = null;
		ResultSet rs = null;

		IReturnToken result = new ReturnTokenImpl();
		
		List<Map<String,String>> records = new ArrayList<Map<String,String>>();

		if (!"".equals(query)) {
			try {
				conn = createConnection();
				if (conn != null) {
					stmt = conn.createStatement();
					// System.out.println("insert:"+query);
					// select max id from table
					rs = stmt.executeQuery(query);
					log.debug("User query was executed: " + query);
					ResultSetMetaData metaData = rs.getMetaData();
					int count = metaData.getColumnCount(); //number of column

					while(rs.next()){
						Map<String,String> resultMap = new HashMap<String,String>();

						for (int i = 1; i <= count; i++){
						   String column = metaData.getColumn(i);
						   Object columnValue = rs.getObject(column);
						   
						   String stringValue = null;
						   if(null != columnValue){
							   stringValue = columnValue.toString();
						   }
						   
						   resultMap.put(column, stringValue);
						}
						records.add(resultMap);
					}
				}
				
			} catch (Exception e) {
				result.setFailure(true);
				result.addMessage(
						new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot execute user query: " + query));
				result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error " + e.getMessage()));

				log.error("Query coult not be executed: " + query + "= " + e.toString());
				// e.printStackTrace();
			} finally {
				safeClose(stmt);
				safeClose(conn);
			}
		}

		result.setResult(records);
		return result;
	}
	
	public IReturnToken doSelectQueryFromTempTable(final String tempTableCreate, final String tempTableSelect, final String tempTableDrop) {
		Statement stmt = null;
		Connection conn = null;
		ResultSet rs = null;

		IReturnToken result = new ReturnTokenImpl();
		List<Map<String,String>> records = new ArrayList<Map<String,String>>();

		try {
			if (!"".equals(tempTableCreate)) {
				
					conn = createConnection();
					if (conn != null) {
						stmt = conn.createStatement();
						// System.out.println("insert:"+query);
						// select max id from table
						stmt.executeUpdate(tempTableCreate);
						log.debug("Temp table creation query was executed: " + tempTableCreate);
					}
				
					
					
					if (!"".equals(tempTableSelect)) {
						rs = stmt.executeQuery(tempTableSelect);
						log.debug("User query was executed: " + tempTableSelect);
						ResultSetMetaData metaData = rs.getMetaData();
						int count = metaData.getColumnCount(); //number of column

						while(rs.next()){
							Map<String,String> resultMap = new HashMap<String,String>();

							for (int i = 1; i <= count; i++){
							   String column = metaData.getColumn(i);
							   Object columnValue = rs.getObject(column);
							   
							   String stringValue = null;
							   if(null != columnValue){
								   stringValue = columnValue.toString();
							   }
							   
							   resultMap.put(column, stringValue);
							}
							records.add(resultMap);
						}
					}
					
					stmt.executeUpdate(tempTableDrop);
					log.debug("Temp table creation query was executed: " + tempTableDrop);
			}
		} catch (Exception e) {
			result.setFailure(true);
			result.addMessage(
					new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot execute temp table select queries: " + tempTableCreate +","+tempTableSelect));
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error " + e.getMessage()));

			log.error("TEMP TABLE CREATE and/or SELECT query coult not be executed: " + tempTableCreate +","+tempTableSelect + "=" + e.toString());

		} finally {
			safeClose(stmt);
			safeClose(conn);
		}
		

		result.setResult(records);
		return result;
	}

	public void safeClose(Connection conn) {
		if (conn != null) {
			try {
				this.returnConnectionToPool(conn);
			} catch (Exception e) {
				log.warn("Failed to return the connection to the pool", e);
			}
		}
	}

	public void safeClose(ResultSet res) {
		if (res != null) {
			try {
				res.close();
			} catch (SQLException e) {
				log.warn("Failed to close databse resultset", e);
			}
		}
	}

	public void safeClose(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				log.warn("Failed to close databse statment", e);
			}
		}
	}

	public Connection createConnection() {
		Connection conn = null;
		try {
			conn = this.getConnectionFromPool();
		} catch (SQLException ex) {
			this.handleSqlError(ex);
		} catch (Exception ex) {
			// handle any errors
			log.error("Exception: " + ex.getMessage());
		}

		return conn;
	}

	@Override
	public ResultSet select(final IDomain domain) {
		return this.select(domain, -1);
	}

	public ResultSet select(final IDomain domain, int max) {
		String query = domain.createSelectQuery();

		if (max > 0) {
			query = query + " limit " + max;
		}

		return this.doSelect(query);
	}

	public ResultSet selectByCompositeKey(final IDomain domain) {
		String query = domain.createCompositeKeySelectQuery();
		return this.doSelect(query);
	}

	private ResultSet doSelect(String query) {
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			// System.out.println("select:"+query);
			conn = createConnection();
			statement = conn.createStatement();
			rs = statement.executeQuery(query);

		} catch (SQLException ex) {
			this.handleSqlError(ex, query);
		} catch (Exception ex) {
			// handle any errors
			log.error("Select query could not be executed: " + query + ex.toString());
		}finally{
			//try{safeClose( conn );}catch(Exception e){}
		}

		return rs;

	}

	private void handleSqlError(SQLException ex) {
		this.handleSqlError(ex, "");
	}

	private void handleSqlError(SQLException ex, String query) {

		// handle any errors
		StringBuilder sb = new StringBuilder();
		sb.append("Select query could not be executed:  " + query);
		sb.append("\nSQLException: " + ex.getMessage());
		sb.append("\nSQLState: " + ex.getSQLState());
		sb.append("\nVendorError: " + ex.getErrorCode());
		sb.append("\nMessage: " + ex.toString());

		log.error(sb.toString());

	}

	Vector<Connection> connectionPool = new Vector<Connection>();

	private void initializeConnectionPool() {
		while (connectionPool.size() < 4 && connectionPool.size() < MAX_POOL_SIZE) {
			log.trace("Connection Pool is NOT full. Proceeding with adding new connections");
			// Adding new connection instance until the pool is full
			connectionPool.addElement(createNewConnectionForPool());
		}
		log.trace("Connection Pool is full.");
	}

	private synchronized boolean isConnectionPoolFull() {

		// Check if the pool size
		if (connectionPool.size() < MAX_POOL_SIZE) {
			log.trace("Pool is not full: current size " + connectionPool.size() + ", Max size: "+ MAX_POOL_SIZE);
			return false;
		}

		log.trace("Pool is not full: current size " + connectionPool.size() + ", Max size: "+ MAX_POOL_SIZE);

		return true;
	}

	// Creating a connection
	private Connection createNewConnectionForPool() {
		Connection connection = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			String jdbcConnUrl = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + db + "?autoReconnect=true&" + "user=" + dbuser + "&password=" + dbpass;
			String showJdbcConnUrl = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + db + "?autoReconnect=true&" + "user=" + dbuser + "&password=xxxxxxxxx";
			log.trace("jdbc new pooled connection:" + showJdbcConnUrl);
			connection = DriverManager.getConnection(jdbcConnUrl);
		} catch (SQLException sqle) {
			log.error("SQLException: " + sqle);
			return null;
		} catch (ClassNotFoundException cnfe) {
			log.error("ClassNotFoundException: " + cnfe);
			return null;
		}

		return connection;
	}

	public synchronized Connection getConnectionFromPool() throws SQLException {
		Connection connection = null;
		log.trace("The current connection pool size is: "+connectionPool.size());

		// Check if there is a connection available. There are times when all
		// the connections in the pool may be used up
		if (connectionPool.size() > 0) {
			connection = (Connection) connectionPool.firstElement();
			log.trace(connection);
			connectionPool.removeElementAt(0);
			log.trace("Remove a conn from connection pool, new size is: "+connectionPool.size());
			
			
			if( connection == null ) {
				connection = createNewConnectionForPool();
				connectionPool.addElement(connection);
				log.trace(connection);
				log.trace("Existing conn is null so adding new conn to connection pool, new size is: "+connectionPool.size());
			}else if ( connection.isClosed() || !connection.isValid(60)) {
				try{
					connection.close();
					log.warn("Note: closed an invalid connection before adding new to connection pool.");
				}catch(Exception e) {
					//This may be more work than is necessary if already closed, but shouldn't happen too often
				}
				connection = createNewConnectionForPool();
				connectionPool.addElement(connection);
				log.trace("Existing conn is closed or invalid so adding new conn to connection pool, new size is: "+connectionPool.size());
				log.trace(connection);

			}
		}else{
			connection = createNewConnectionForPool();
			connectionPool.addElement(connection);
			log.trace("Connection pool is empty so adding a new connection, new size is: "+connectionPool.size());
			log.trace(connection);

		}

		// Giving away the connection from the connection pool
		return connection;
	}

	public synchronized void returnConnectionToPool(Connection connection) {
		//log.info("Returning connection to pool, size is: "+connectionPool.size());
		if( connection != null){
			// Adding the connection from the client back to the connection pool
			if(!isConnectionPoolFull()) {
				connectionPool.addElement(connection);
				log.trace("Returned connection to pool, new size is: "+connectionPool.size());
			}
		}
	}
}
