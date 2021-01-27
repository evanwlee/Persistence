package com.evanwlee.data;

import java.sql.Connection;
import java.sql.DriverManager;
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




public class MysqlPersistenceManager implements IPersistenceManager {
	private Logger log = LoggerFactory.getLogger(MysqlPersistenceManager.class.getName());

	private volatile static MysqlPersistenceManager currentManager = null;
	public static final String DB_CONFIG = new String("resources.persistence.properties");
	
	private String dbhost = new String("");
	private String dbport = new String("");
	private String db = new String("");
	private String dbuser = new String("");
	private String dbpass = new String("");
	
	MysqlPersistenceManager(){
		this("");
	}
	
	private volatile static Map<String,MysqlPersistenceManager> instances = new HashMap<String,MysqlPersistenceManager>();
	
	MysqlPersistenceManager(String app){
		
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
		log.info("ENV="+env+", HOST:" + dbhost + ", PORT:" + dbport + ", DB:" + db + ", USER:" + dbuser);
	}
	
	
	/**
	 * Returns the current domain factory manager for the application.
	 */
	public static MysqlPersistenceManager current() {
		currentManager = instances.get("");
		if (currentManager == null) {
			synchronized (MysqlPersistenceManager.class) {
				if (currentManager == null) {
					currentManager = new MysqlPersistenceManager();
					instances.put("", currentManager);
				}
			}
		}
		return currentManager;
	}
	
	public static MysqlPersistenceManager current(String host) {
		currentManager = instances.get(host);
		if (currentManager == null ) {
			synchronized (MysqlPersistenceManager.class) {
				if (currentManager == null) {
					currentManager = new MysqlPersistenceManager(host);
					instances.put(host, currentManager);
				}
			}
		}
		return currentManager;
	}
	
	public long update(final IDomain domain){
		   String query = domain.createUpdateQuery();
	       Statement stmt =  null;
	       Connection conn = null;
	       long id = domain.getId();
	       IReturnToken result = new ReturnTokenImpl();
			if (id <= 0) {
				result.setFailure(true);
				result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot Do Update"));
				result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error No ID provided"));
				return id;
			}

	        try{
				conn = createConnection();
				if( conn != null){
			        stmt = conn.createStatement();
			        //System.out.println("update:"+query);
			        stmt.executeUpdate( query );
		        	//int rowsImpacted = stmt.executeUpdate( query );
		        	//what if no rows impacted
		        	//id = this.parseIdFromUpdateQuery(query);
		        }  
	        } catch (SQLException ex) {
	        	this.handleSqlError(ex, query);
	        }catch(Exception e){
	        	log.error("Update query coult not be executed: "+query+ "-" + e.toString());
	        }finally{
	               safeClose(stmt);
	               safeClose(conn);
	        }
	        return id;
		}
	
//	private long parseIdFromUpdateQuery(String updateQuery){
//		long id = 0;
//		if(!"".equals(updateQuery)){
//			
//			String searchString = "";
//			int whereIndex = updateQuery.indexOf(" WHERE ");
//			if( whereIndex > -1){
//				searchString = updateQuery.substring( whereIndex + 6 ).toLowerCase();
//			}
//			
//			if(!"".equals(searchString)){
//				String searchFor = " id=";
//				
//				int index = searchString.indexOf(searchFor);
//				if( index == -1){
//					searchFor = " id =";
//					index = searchString.indexOf(searchFor);
//				}
//		
//				if(index != -1){//was found, so parse
//					String idS = searchString.substring(index);
//					if(idS.length() > -1){
//						idS = idS.substring(idS.indexOf(searchFor));
//						if(idS.indexOf("and") > -1){
//							idS = idS.substring(idS.indexOf(searchFor), idS.indexOf(" and"));
//						}
//					}
//					idS = idS.replace(searchFor, "");
//					id = Long.parseLong(idS);
//				}else{
//					id = 0;
//				}
//			}
//		}
//    	return id;
//	}
	public IReturnToken insert(final IDomain domain){
	   String query = domain.createInsertQuery();
       Statement stmt =  null;
       Connection conn = null;
       long id = 0;
       ResultSet rs = null;
       
       IReturnToken result = new ReturnTokenImpl();

        try{
			conn = createConnection();
			if( conn != null){
		        stmt = conn.createStatement();
		       // System.out.println("insert:"+query);
		        //select max id from table
	        	stmt.executeUpdate( query );
	        	rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
	        	while(rs.next()){
	        		id = rs.getLong(1);
	        	}
	        	result.setResult(id);
	        	//System.out.println("ID "+id+" records in the database.");
	        }  
        }catch(Exception e){
        	result.setFailure(true);
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot Insert " + query));
			result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Error " + e.getMessage()));

			log.error("Insert query coult not be executed: "+query+ "-" + e.toString());
        	//e.printStackTrace();
        }finally{
            safeClose(stmt);
            safeClose(conn);
        }
        
        return result;
	}
	
	
	public IReturnToken delete(final IDomain domain){
	   String query = domain.createDeleteQuery();
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
		        	log.debug("Delete query was executed: "+query);
		        }  
	        }catch(Exception e){
	        	result.setFailure(true);
				result.addMessage(new StatusMessageImpl(IReturnToken.Status.ERROR, "Cannot Delete " + query));
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

	
	public Connection createConnection(){
		Connection conn = null;
		try {
			String jdbcConnUrl = "jdbc:mysql://"+dbhost+":"+dbport+"/"+db+"?" +"user="+dbuser+"&password="+dbpass;
			String showJdbcConnUrl = "jdbc:mysql://"+dbhost+":"+dbport+"/"+db+"?" +"user="+dbuser+"&password=xxxxxxxxx";
			log.trace("jdbc simple connection:"+showJdbcConnUrl);
		    conn = DriverManager.getConnection(jdbcConnUrl);
		} catch (SQLException ex) {
			this.handleSqlError(ex);
		} catch (Exception ex) {
		    // handle any errors
			log.error("Exception: " + ex.getMessage());
		}
		
		return conn;
	}

	@Override
	public ResultSet select(final IDomain domain){
		return this.select(domain, -1);
	}
	
	public ResultSet select(final IDomain domain, int max){
		String query = domain.createSelectQuery();

		if( max > 0 ){
			query = query + " limit " + max;
		}
		
		return this.doSelect(query);
	}
	
	public ResultSet selectByCompositeKey(final IDomain domain){
		String query = domain.createCompositeKeySelectQuery();
		return this.doSelect(query);
	}
	
	private ResultSet doSelect(String query){

		Connection conn = null;
		Statement statement= null;
		ResultSet rs = null;
		try {
			 //System.out.println("select:"+query);
		    conn = createConnection();
		    statement= conn.createStatement();
            rs = statement.executeQuery(query);

		} catch (SQLException ex) {
			this.handleSqlError(ex, query);
		} catch (Exception ex) {
		    // handle any errors
			log.error("Select query could not be executed: "+query+ex.toString());
		}
		
		return rs;
		
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



}
