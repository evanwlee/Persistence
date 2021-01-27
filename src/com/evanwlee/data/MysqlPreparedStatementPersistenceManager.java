package com.evanwlee.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.evanwlee.data.domain.IDomain;
import com.evanwlee.utils.logging.LoggerFactory;
import com.evanwlee.utils.properties.PropertyLoader;
import com.evanwlee.utils.security.encryption.DesEncrypter;
import com.evanwlee.utils.token.IReturnToken;
import com.evanwlee.utils.token.ReturnTokenImpl;
import com.evanwlee.utils.token.StatusMessageImpl;




public class MysqlPreparedStatementPersistenceManager implements IPersistenceManager {
	private Logger log = LoggerFactory.getLogger(MysqlPreparedStatementPersistenceManager.class.getName());

	private volatile static IPersistenceManager currentManager = null;
	public static final String DB_CONFIG = new String("resources.persistence.properties");
	
	private String dbhost = new String("");
	private String dbport = new String("");
	private String db = new String("");
	private String dbuser = new String("");
	private String dbpass = new String("");
	
	MysqlPreparedStatementPersistenceManager(){
		this("");
	}
	
	private volatile static Map<String,IPersistenceManager> instances = new HashMap<String,IPersistenceManager>();
	
	MysqlPreparedStatementPersistenceManager(String app){
		
		if(!"".equals(app)){
			app = app+".";
		}

		Properties dbconfig = PropertyLoader.loadProperties(DB_CONFIG);
		String env = dbconfig.getProperty("DB.ENV", "LOCAL");
		if( "".equals(env) || env.equals(null)){
			env = "LOCAL";
		}
		dbhost = dbconfig.getProperty("DB."+env+"."+app+"HOST", "localhost");
		dbport = dbconfig.getProperty("DB."+env+"."+app+"PORT", "8889");
		db = dbconfig.getProperty("DB."+env+"."+app+"DB", "per_test");
		dbuser = dbconfig.getProperty("DB."+env+"."+app+"USER", "root");
		dbpass = dbconfig.getProperty("DB."+env+"."+app+"PASS", "root");
		
		dbpass = new DesEncrypter().decrypt(dbpass);
		log.debug("ENV="+env+", HOST:" + dbhost + ", PORT:" + dbport + ", DB:" + db + ", USER:" + dbuser);
	}
	
	
	/**
	 * Returns the current domain factory manager for the application.
	 */
	public static IPersistenceManager current() {
		currentManager = instances.get("");
		if (currentManager == null) {
			synchronized (MysqlPreparedStatementPersistenceManager.class) {
				if (currentManager == null) {
					currentManager = new MysqlPreparedStatementPersistenceManager();
					instances.put("", currentManager);
				}
			}
		}
		return currentManager;
	}
	
	public static IPersistenceManager current(String host) {
		currentManager = instances.get(host);
		if (currentManager == null ) {
			synchronized (MysqlPreparedStatementPersistenceManager.class) {
				if (currentManager == null) {
					currentManager = new MysqlPreparedStatementPersistenceManager(host);
					instances.put(host, currentManager);
				}
			}
		}
		return currentManager;
	}
	
	
	public IReturnToken insert(final IDomain domain){
	   PreparedStatement stmt =  null;
       Connection conn = null;
       long id = 0;
       ResultSet rs = null;
       
       IReturnToken result = new ReturnTokenImpl();

        try{
			conn = this.createConnection();
			if( conn != null){
		        stmt = domain.createInsertPreparedStatement(conn);
	        	stmt.executeUpdate();
	        	rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
	        	while(rs.next()){
	        		id = rs.getLong(1);
	        	}
	        	result.setResult(id);
	        }  
        }catch(Exception e){
        	result.setFailure(true);
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot Do Insert Perpared Statement"));
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error " + e.getMessage()));

			log.error("INSERT perpared statement coult not be executed:" + e.toString());
        	//e.printStackTrace();
        }finally{
			if (stmt != null) {
				try{stmt.close();}catch(Exception e){}
			}
			if (conn != null) {
				try{conn.close();}catch(Exception e){}
			}
        }
        
        return result;
	}
	
	public void safeClose(Connection conn) {
        if (conn != null) {
             try {
            	 conn.close();
             }
             catch (Exception e) {
                  log.warn("Failed to close the connection to the database", e);
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


	@Override
	public ResultSet select(final IDomain domain){
		return this.select(domain, -1);
	}
	@Override
	public ResultSet select(final IDomain domain, int max){
		return this.select(domain, max, false);
	}
	
	protected ResultSet select(final IDomain domain, int max, boolean byCompositeKey){
		
		   PreparedStatement stmt =  null;
	       Connection conn = null;
	       ResultSet rs = null;
	       
	       //IReturnToken result = new ReturnTokenImpl();

	        try{
				conn = this.createConnection();
				if( conn != null){
			        stmt = domain.createSelectPreparedStatement(conn,max,byCompositeKey);
		        	rs = stmt.executeQuery();
		        }  
			} catch (SQLException ex) {
				this.handleSqlError(ex);
			} catch (Exception ex) {
			    // handle any errors
				log.error("SELECT Prepared Statment query could not be executed: "+ex.toString());
			}
	        
	        return rs;
	}
	
	public long update(final IDomain domain){
		   PreparedStatement stmt =  null;
	       Connection conn = null;
	       
	       long id = domain.getId();
	       IReturnToken result = new ReturnTokenImpl();
			if (id <= 0) {
				result.setFailure(true);
				result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot Do Update Perpared Statement"));
				result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error No ID provided"));
				return id;
			}
			
	        try{
				conn = this.createConnection();
				if( conn != null){
			        stmt = domain.createUpdatePreparedStatement(conn);
			        stmt.executeUpdate();
		        	//int rowsImpacted = stmt.executeUpdate();
		        	//What if no impact
		        }  
			} catch (SQLException ex) {
				this.handleSqlError(ex);
			} catch (Exception ex) {
			    // handle any errors
				log.error("UPDATE Prepared Statment query could not be executed: "+ex.toString());
			}finally{
				if (stmt != null) {
					try{stmt.close();}catch(Exception e){}
				}
				if (conn != null) {
					try{conn.close();}catch(Exception e){}
				}
			}
	        
	       return id;
	}

	
	
	public IReturnToken delete(final IDomain domain) {
		PreparedStatement stmt = null;
		Connection conn = null;
		IReturnToken result = new ReturnTokenImpl();

		if (domain.getId() <= 0) {
			result.setFailure(true);
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot Do DELETE Perpared Statement"));
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error No ID provided"));
			return result;
		}

		String deleteSQL = "DELETE FROM " + domain.tableName() + " WHERE ID = ?";
		log.debug(deleteSQL);
		try {
			conn = this.createConnection();
			if (conn != null) {
				stmt = conn.prepareStatement(deleteSQL);
				stmt.setLong(1, domain.getId());
				stmt.executeUpdate();
			}
		} catch (Exception ex) {
			result.setFailure(true);
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot Do DELETE Perpared Statement"));
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error " + ex.toString()));

			log.error("DELETE perpared statement coult not be executed:" + ex.toString());
		}finally{
			if (stmt != null) {
				try{stmt.close();}catch(Exception e){}
			}
			if (conn != null) {
				try{conn.close();}catch(Exception e){}
			}
		}

		return result;
	}
	
	public IReturnToken doModificationQuery(final String query){
	       Statement stmt =  null;
	       Connection conn = null;
	       
	       IReturnToken result = new ReturnTokenImpl();

	       if(!"".equals(query)){
		        try{
					conn = createConnection();
					if( conn != null){
				        stmt = conn.createStatement();
				       // System.out.println("insert:"+query);
				        //select max id from table
			        	stmt.executeUpdate( query );
			        	log.debug("User Created query was executed: "+query);
			        }  
		        }catch(Exception e){
		        	result.setFailure(true);
					result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot execute user query: " + query));
					result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error " + e.getMessage()));

					log.error("Delete query coult not be executed: "+query + "=" + e.toString());
		        	//e.printStackTrace();
		        }finally{
		               safeClose(stmt);
		               safeClose(conn);
		        }
	       }
	        
	        return result;
	}
	
	public IReturnToken doSelectQuery(final String query){
	       Statement stmt =  null;
	       Connection conn = null;
	       
	       IReturnToken result = new ReturnTokenImpl();

	       if(!"".equals(query)){
		        try{
					conn = createConnection();
					if( conn != null){
				        stmt = conn.createStatement();
				       // System.out.println("insert:"+query);
				        //select max id from table
			        	stmt.executeUpdate( query );
			        	log.debug("User Created query was executed: "+query);
			        }  
		        }catch(Exception e){
		        	result.setFailure(true);
					result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot execute user query: " + query));
					result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error " + e.getMessage()));

					log.error("Delete query coult not be executed: "+query + "=" + e.toString());
		        	//e.printStackTrace();
		        }finally{
		               safeClose(stmt);
		               safeClose(conn);
		        }
	       }
	        
	        return result;
	}

	
	public Connection createConnection(){
		Connection conn = null;

		try {
		    conn =
		       DriverManager.getConnection("jdbc:mysql://"+dbhost+":"+dbport+"/"+db+"?" +"user="+dbuser+"&password="+dbpass);
		} catch (SQLException ex) {
			this.handleSqlError(ex);
		} catch (Exception ex) {
		    // handle any errors
			log.error("Exception: " + ex.getMessage());
		}
		
		return conn;
	}
	
	private void handleSqlError(SQLException ex){
		 this.handleSqlError( ex, "");
	}
	
	private void handleSqlError(SQLException ex, String query){

		 // handle any errors
		StringBuilder sb = new StringBuilder();
		sb.append("Select query could not be executed:  " + query);
		sb.append("\nSQLException: " + ex.getMessage());
		sb.append("\nSQLState: " + ex.getSQLState());
		sb.append("\nVendorError: " + ex.getErrorCode());
		sb.append("\nMessage: " + ex.toString());
		
		log.error(sb.toString());

	}


	@Override
	public ResultSet selectByCompositeKey(IDomain domainObject) {
		return this.select(domainObject,-1, true);
	}

}
