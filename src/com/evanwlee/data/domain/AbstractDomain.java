package com.evanwlee.data.domain;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.evanwlee.data.*;
import com.evanwlee.data.domain.annotation.CompositeAttribute;
import com.evanwlee.data.domain.annotation.UsesDataAnnotations;
import com.evanwlee.utils.logging.LoggerFactory;
import com.evanwlee.utils.token.IReturnToken;
import com.evanwlee.utils.token.ReturnTokenImpl;

/**
 * Allows for persistence actions on Domain objects.
 * 
 * @author evan.l
 *
 */
public abstract class AbstractDomain implements Cloneable, IDomain{
	
	private Logger log = LoggerFactory.getLogger(AbstractDomain.class.getName());

	
	public static final String THE_FOUR = "id, created, modified, deactivated";
	public static final String TABLE_PREFIX = "b2c_";
	private Long id = 0l;
	
	private IReturnToken result = new ReturnTokenImpl();
	
	protected IPersistenceManager pManager = PersistenceManagerFactory.current().getPersistenceManager();




	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	protected boolean isEmpty(String val){
		if( val == null || "".equalsIgnoreCase(val.trim())){
			return true;
		}
		return false;
	}

	
	/**
	 * For future, init Domain object based on all declared public setters.
	 * 
	 * @param rs d record used to init the Domain Object, column names
	 * should match setters: e.g. setCountryCode (setter) has corresponding column 'country_code'.
	 * If no setter is found it will nothing will be mapped.
	 */
	protected void initFromRecord(ResultSet rs){
		try{
		    Class<?> cls = this.getClass();
		    
		    Object ob = this;
            for (Method method : cls.getMethods())
            {
                if ((method.getName().startsWith("set")))
                {
                    // MZ: Method found, run it
                    try{
                        method.setAccessible(true);
                        
                        String name = createColumnNameFromGetter(method.getName());
                        if( !"persistence_manager".equals(name) && !"modification_status".equals(name) ){
                        	String paramType = method.getGenericParameterTypes()[0].getTypeName().toLowerCase();
                            if(paramType.endsWith("integer")){
                                method.invoke(ob,rs.getInt(name));
                            }else if(paramType.endsWith("long")){
                            	Long val = rs.getLong(name);
                            	if( val == 0){
                            		Object nullval = rs.getObject(name);
                            		if(nullval == null){
                            			val = null;
                            		}
                            	}
                                method.invoke(ob,val);
                            }else if(paramType.endsWith("string")){
                                method.invoke(ob,rs.getString(name));
                            }else if(paramType.endsWith("boolean")){
                                method.invoke(ob,rs.getBoolean(name));
                        	}else if(paramType.endsWith("timestamp")){
                                method.invoke(ob,rs.getTimestamp(name));
                        	}else if(paramType.endsWith("date")){
                                method.invoke(ob,rs.getDate(name));
                        	}else if(paramType.endsWith("double")){
                                method.invoke(ob,rs.getDouble(name));
                        	}else if(paramType.endsWith("float")){
                            	float val = rs.getFloat(name);
	                        	if( val == 0){
	                        		Object isNullval = rs.getObject(name);
	                        		if( isNullval == null){
	                        			method.invoke(ob,(Float)null);
	                        		}else{
	                        			method.invoke(ob,val);
	                        		}
	                        	}else{
		                        	method.invoke(ob,val);

	                        	}
                                //method.invoke(ob,rs.getFloat(name));
                            } else if(paramType.endsWith("time")){
                                method.invoke(ob,rs.getTime(name));
                            //BLOB
	                        } else if(paramType.endsWith("inputstream")){
	                            method.invoke(ob,rs.getBinaryStream(name));
                    		}else{ 
                                method.invoke(ob,rs.getObject(name));
                    		}
                        	
                        }
                        
                    }catch (IllegalAccessException | InvocationTargetException | SQLException e){
        	            //log.warn("Cant set public method on Domain Object: "+cls.getName()+"." +  method.getName() +"()");
                    }
		        }
		    }
		}catch (Exception e){
            System.err.println(e.getMessage());
        }
	}

	/**
	 * First checks to see if the object exist based on the composite key (if @getCompositeKeySelectWhereClause does not
	 * return empty string), otherwise it uses all set attributes.
	 * If it does exist then it is returned, otherwise it is created and
	 * returned.
	 * 
	 * 
	 * <br/><br/>
	 * <b>Compiste key</b> a combination of two or more columns in a table that can be used to uniquely identify each row in the table
	 * <br/><br/>
	 * Object creation includes saving a corresponding record in the database.
	 * 
	 * @CompositeAttribute should be on all getters in the Domain object that are part of the composite key.
	 * @return the object created or fetched from the database, the getId() will
	 * return 0 if nothing was created.
	 */
	public AbstractDomain fetchOrCreate(){
		ResultSet rs = pManager.selectByCompositeKey(this);
		try{
			if( rs != null ){
				if (rs.next()) {
					if( rs.getLong("id") > 0 ){
						this.createFromRecord(rs);
					}else{
						IReturnToken result = pManager.insert(this);
						
						this.setModificationStatus(result);
						
						if( !result.isFailure()){
							this.setId((Long)result.getResult());
						}else{
							this.setId(0l);
						}
					}
				}else{
					IReturnToken result = pManager.insert(this);
					
					this.setModificationStatus(result);
					
					if( !result.isFailure()){
						this.setId((Long)result.getResult());
					}else{
						this.setId(0l);
					}
					
				}
				return this;
			}
		}catch(Exception e){
			log.error(e.getMessage());
		}finally{
			try{pManager.safeClose(rs.getStatement().getConnection());}catch(Exception e){}
			try{pManager.safeClose(rs.getStatement());}catch(Exception e){}
			try{pManager.safeClose(rs);}catch(Exception e){}
		}

		return this;
	}
	
	/**
	 * Will check to see if the emelemnt exists by ID column. If ID is null,
	 * then the record will be created and a hydrated object will be returned.
	 * 
	 * If the ID does not exist,then the record will be created and a hydrated object will be returned.
	 * 
	 * If the ID is found, then all attributes will be updated. Empty string will not overwrite values, but string with space will overwrite a string.
	 * Nulls will not overwrite non-null values. 
	 * @return Domain hydrated instance.
	 */
	public AbstractDomain update(){
		long id = this.getId();
		if(id == 0 ){
			return this.save();
		}else{
			//Update
			id = pManager.update(this);
			this.setId(id);
		}

		return this;
	}
	
	/**
	 * Will save the domain object as a new record, does not check for duplication.
	 * @return Domain hydrated instance.
	 */
	public AbstractDomain save(){
		//long id = pManager.insert(this.getInsertQuery());
		
		//IReturnToken result = pManager.insert(this.getInsertQuery());
		IReturnToken result = pManager.insert(this);
		
		this.setModificationStatus(result);
		
		if(!result.isFailure()){
			this.setId((Long)result.getResult());
		}else{
			this.setId(0l);
		}
		return this;
	}
	/**
	 * @return List of matching domain objects, based on any attribute set on the domain object
	 */
	public List<IDomain> fetch(){
		return fetch(-1);
	}
	
	/**
	 * Querys based on any attribute set on the domain object
	 * 
	 * @param max limit to the number of objects returned
	 * @return List of matching domain objects, empty if no matches found
	 */
	public List<IDomain> fetch(int max){
		List<IDomain> objs = new ArrayList<IDomain>();

		ResultSet rs = pManager.select(this,max);
		try{
		if( rs != null){
			while (rs.next()) {
				AbstractDomain o = (AbstractDomain)this.clone();
				o.createFromRecord(rs);
				objs.add(o);
			}
		}
		}catch(Exception e){
			log.error(e.getMessage());
		}finally{
			try{pManager.safeClose(rs.getStatement().getConnection());}catch(Exception e){}
			try{pManager.safeClose(rs.getStatement());}catch(Exception e){}
			try{pManager.safeClose(rs);}catch(Exception e){}
		}
		return objs;
	}
	
	public IReturnToken delete(){
		IReturnToken result = pManager.delete(this);
		
		this.setModificationStatus(result);
		
		if(!result.isFailure()){
			this.setId((Long)result.getResult());
		}else{
			this.setId(0l);
		}
		return result;
	}
	
	
	public IReturnToken modificationStatus(){ 
		if( result == null){
			result = new ReturnTokenImpl();
		}
		return result;
	}
	
	public void setModificationStatus(IReturnToken result){ 
		this.result = result;;
	}
	

	
	/**
	 * @return comma separated string of all the getters (attributes) of the domain object
	 */
	public String csvResults() {
		String csv = "";
		
		try{
		
			Method[] methods = this.getClass().getMethods();
		    for (Method method:methods)
		    {
		    	if(method.getName().startsWith("get")){
		    		String columName = createColumnNameFromGetter(method.getName());
			    	method.setAccessible(true);
			    	Object valueObject = method.invoke(this, (Object[]) null);
				    String content =  valueObject != null ? valueObject.toString() : null;
				    
				    if(  !columName.equals("class") && !columName.equals("p_manager") ){
				    	String paramType = method.getReturnType().getName().toLowerCase();
				    	content = columName +"=="+content;
				    	if( paramType.endsWith("string")){
				    		csv += "'"+content+"',";
				    	}else{
				    		csv += content+",";
				    	}
				    }
		    	}

		    }
		}catch(Exception e){};
		

		if( csv.endsWith(",")){
			csv = csv.substring(0, csv.lastIndexOf(","));
		}
		return csv;
	}
	
	protected String createColumnNameFromGetter(String original){
		//strip out get
		String columName = original.substring(3);
		//split on on caps
		String regex = "([a-z])([A-Z])";
        String replacement = "$1_$2";
        columName = columName.replaceAll(regex, replacement);
        columName = columName.toLowerCase();
		
		return columName;
	}
	
	private String createGetterNameFromColumn(String column){
		String getterName = column;
		
		final StringBuilder ret = new StringBuilder(getterName.length());

	    for (final String word : getterName.split("_")) {
	        if (!word.isEmpty()) {
	            ret.append(word.substring(0, 1).toUpperCase());
	            ret.append(word.substring(1).toLowerCase());
	        }
	        if (!(ret.length()==column.length()))
	            ret.append("");
	    }
	    return "get"+ret.toString();

	}
	
//	private String createSetterNameFromColumn(String column){
//		String getterName = column;
//		
//		final StringBuilder ret = new StringBuilder(getterName.length());
//
//	    for (final String word : getterName.split("_")) {
//	        if (!word.isEmpty()) {
//	            ret.append(word.substring(0, 1).toUpperCase());
//	            ret.append(word.substring(1).toLowerCase());
//	        }
//	        if (!(ret.length()==column.length()))
//	            ret.append("");
//	    }
//	    return "set"+ret.toString();
//
//	}
	
	protected String makeSqlSafe(String value){
		
		if(value != null ){
			if( value.indexOf("'") > -1){
				value = value.replaceAll("'", "''");
			}
			if( value.indexOf("\\") > -1){
				value = value.replace("\\", "\\\\");
			}
		}
		return value;
	}
	/**
	 * @return comma seperated string of all column names based on the public getters of the Domain object
	 */
	public String columnNames(){
		
		return listOfColumnNames().toString().replace("[", "").replace("]", "");
		
	}
	/**
	 * @return list of all column names based on the public getters of the Domain object
	 */
	public List<String> listOfColumnNames(){

	    Class<?> objClass= this.getClass();
	    
	    List<String> columnName = new ArrayList<String>();
	
	    // Get the public methods associated with this class.
	    Method[] methods = objClass.getMethods();
	    for (Method method:methods)
	    {
	    	if( method.getName().indexOf("get") == 0  && method.getName().indexOf("getClass") == -1){
	    		
	    		String name = method.getName();
	    		
	    		name = name.replace("get", "");
	    		
	    		String regex = "([a-z])([A-Z]+)";
	            String replacement = "$1_$2";
	            
	            String cleanName = name
                        .replaceAll(regex, replacement)
                        .toLowerCase();
	            log.trace("Public method: " +  method.getName() + " was translated to Column: " +  cleanName);
	            columnName.add(cleanName);
	    	}
	    }
	    
	    return columnName;
	}
	
	/**
	 * Create a select * query based on the Domain objecst composite key, if the values are set. If 
	 * the getCompositeKeySelectWhereClause() returns an empty string then all attributes will be used.
	 * @return query
	 */
	public String createCompositeKeySelectQuery(){
		String where = "";
		if( this.hasCompositeAnnotations() ){
			where = this.buildCompositeKeySelectWhereClauseFromAnnotations();
		}else{
			where = this.getCompositeKeySelectWhereClause();
		}
		
		String testWhere = where.replaceAll("\\s+","").toLowerCase();
		if("where".equals(testWhere) || "".equals(testWhere)){
			log.error("Invalid where clause passed in the creation of the CompositeKey based query:"+this.tableName());
			return "";
		}
		String q = "SELECT * FROM " + this.tableName() + where;
		
		if("".equals(where)){
			q = this.createSelectQuery();
		}
		
		if(q.endsWith(" and ")){
			q = q.substring(0,q.lastIndexOf(" and "));
		}
		return q;
	}
	
	private boolean hasCompositeAnnotations(){
		try{
			Class<?> obj = this.getClass();
	
			// Process @TesterInfo
			if (obj.isAnnotationPresent(UsesDataAnnotations.class)) {
				Annotation annotation = obj.getAnnotation(UsesDataAnnotations.class);
				UsesDataAnnotations compositeInfo = (UsesDataAnnotations) annotation;
				
				if(compositeInfo.type() == UsesDataAnnotations.Type.COMPOSITE)
					return true;
			}
		}catch(Exception e){
			log.error("Could not determine if composite data annotations are used.");
		}
		
		return false;
	}
	
	@SuppressWarnings("unused")
	private boolean hasRequiredAnnotations(){
		try{
			Class<?> obj = this.getClass();
	
			// Process @TesterInfo
			if (obj.isAnnotationPresent(UsesDataAnnotations.class)) {
				Annotation annotation = obj.getAnnotation(UsesDataAnnotations.class);
				UsesDataAnnotations required = (UsesDataAnnotations) annotation;
				
				if(required.type() == UsesDataAnnotations.Type.REQUIRED)
					return true;
			}
		}catch(Exception e){
			log.error("Could not determine if composite data annotations are used.");
		}
		
		return false;
	}
	

	/**
	 * @return the resultant select query for this domain object
	 */
	public String createSelectQuery(){
		String query = "SELECT [[SELECT_CLAUSE]] FROM "+this.tableName()+"[[WHER_CLAUSE]]";;
		String selectClause = "";
		String whereClause = "";
		List<String> columnNames = listOfColumnNames();
		for(String column: columnNames){
			
			selectClause = selectClause + column + ",";
			
			String whereValue = determineWhereValue(column);
			
			if( null != whereValue && !"".equals(whereValue) && !"''".equals(whereValue) && !"'null'".equals(whereValue) && !"null".equals(whereValue)){
				if( !"deactivated".equals(column) && !"created".equals(column) && !"modified".equals(column)){
					if( "id".equals(column) && ("0".equals(whereValue) || "-1".equals(whereValue))){
						//Dont include, this is a new qbe object
					}else{
						whereClause = whereClause + column + "=" + whereValue + " and ";
					}
				}
			}
			
		}
		
		if(!"".equals(selectClause)){
			selectClause = selectClause.substring(0,selectClause.lastIndexOf(","));
		}
		if(!"".equals(whereClause)){
			whereClause = " where " + whereClause.substring(0,whereClause.lastIndexOf(" and "));
		}
		
		query = query.replace("[[SELECT_CLAUSE]]", selectClause);
		query = query.replace("[[WHER_CLAUSE]]", whereClause);

		log.trace(query);
		return query;
	}
	
	/**
	 * @return the resultant insert query for this domain object. May be empty if
	 * no attributes are set.
	 */
	public String createInsertQuery(){
		String sql = "INSERT INTO " + this.tableName() + " ([[COLUMN_NAMES]]) VALUES ([[COL_VALUES]])";
		
		String namesString = "";
		String valueClause = "";
		List<String> names = this.listOfColumnNames();
		for(String name : names){
			String whereValue = this.determineWhereValue(name);
			
			//THE 4 are controlled by DBMS - or specific calls (e.g. delete)
			if( !"deactivated".equals(name) && !"created".equals(name) && !"modified".equals(name)&& !"id".equals(name)){
				if( "'null'".equals(whereValue) || "".equals(whereValue)){
					whereValue = "null";
				}
				namesString = namesString + name + ",";
				valueClause = valueClause + whereValue + ",";
			}
		}
		
		if( namesString.endsWith(",")){
			namesString = namesString.substring(0, namesString.lastIndexOf(","));
		}
		
		if( valueClause.endsWith(",")){
			valueClause = valueClause.substring(0, valueClause.lastIndexOf(","));
		}
		
		if( !"".equals(valueClause)){
			sql = sql.replace("[[COLUMN_NAMES]]", namesString);
			sql = sql.replace("[[COL_VALUES]]", valueClause);
		}else{
			sql = "";
		}
		log.trace(sql);
		return sql;
	}
	
	public PreparedStatement createInsertPreparedStatement(Connection conn) throws Exception{
		
		
		String perp = "INSERT INTO "+this.tableName()+" ([[COLUMN_NAMES]]) VALUES ([[VAL_PLACEHOLDE]])";
		
		String placeHolder = "";
		String namesString = "";

		List<String> names = this.listOfColumnNames();
		for(String name : names){
			
			
			//THE 4 are controlled by DBMS - or specific calls (e.g. delete)
			if( !"deactivated".equals(name) && !"created".equals(name) && !"modified".equals(name)&& !"id".equals(name)){

				namesString = namesString + name + ",";
				placeHolder = placeHolder + "?,";
			}
		}
		
		if( placeHolder.endsWith(",")){
			placeHolder = placeHolder.substring(0, placeHolder.lastIndexOf(","));
		}
		if( namesString.endsWith(",")){
			namesString = namesString.substring(0, namesString.lastIndexOf(","));
		}
		
		

		perp = perp.replace("[[COLUMN_NAMES]]", namesString);
		perp = perp.replace("[[VAL_PLACEHOLDE]]", placeHolder);
		
		log.trace(perp);
		
		PreparedStatement stmt = null;
		if( conn != null){
			stmt = conn.prepareStatement(perp);
		}
		stmt = this.setPreparedStatementValues(stmt, names,false);
		
	

	    return stmt;
	}
	
	//If no where cluase onle returns 1000 records
	//**ONLY RESPECTS ANNOTATIONS FOR SELECTS WHERE CLAUSE
	
	public PreparedStatement createSelectPreparedStatement(Connection conn, int limit, boolean byCompositeKey) throws Exception{
		String limitS = "";
		if(limit > 0){
			limitS = " LIMIT " + limit;
		}
		String perp = "SELECT [[COLUMN_NAMES]] FROM "+this.tableName()+" [[VAL_PLACEHOLDE]] "+limitS;
	
		String placeHolder = "";
		String namesString = "";

		List<String> names = this.listOfColumnNames();
		List<String> whereColumnNames = new ArrayList<String>();
		for(String name : names){
			
			
			//THE 4 are controlled by DBMS - or specific calls (e.g. delete)
			if( !"deactivated".equals(name) && !"created".equals(name) && !"modified".equals(name)){
				
				String val = "";
				if(byCompositeKey){
					val = this.determineWhereValueForCompositeKey(name);
				}else{
					val = this.determineWhereValue(name);
				}

				namesString = namesString + name + ",";
				if(!"".equals(val) && !"''".equals(val) && !"'null'".equals(val) && null != val){
					if("id".equals(name) && !"0".equals(val)){
						placeHolder = placeHolder + name + "=? and ";
						whereColumnNames.add(name);
					}
				}
			}
		}
		
		if( placeHolder.endsWith("and ")){
			placeHolder = placeHolder.substring(0, placeHolder.lastIndexOf("and "));
		}
		if( namesString.endsWith(",")){
			namesString = namesString.substring(0, namesString.lastIndexOf(","));
		}
		if( !"".equals(placeHolder)){
			placeHolder = " WHERE " + placeHolder;
		}else{
			//If no where clause don't over fetch
			limitS = " LIMIT 1000";
		}
		

		perp = perp.replace("[[COLUMN_NAMES]]", namesString);
		perp = perp.replace("[[VAL_PLACEHOLDE]]", placeHolder);
		
		log.debug(perp);
		log.trace("Using composite key = " +byCompositeKey);
		
		PreparedStatement stmt = null;
		if( conn != null){
			stmt = conn.prepareStatement(perp);
		}
		stmt = this.setPreparedStatementValues(stmt, whereColumnNames);
		return stmt;
	}
	
	public PreparedStatement createUpdatePreparedStatement(Connection conn) throws Exception{
		PreparedStatement stmt = null;
//		if( this.getId() <=0){
//			return stmt;
//		}
		
		String perp = "UPDATE "+this.tableName()+" SET [[COLUMN_NAMES]] WHERE ID = ?";


		String namesString = "";

		List<String> names = this.listOfColumnNames();
		List<String> placeHolderNames = new ArrayList<String>();
		for(String name : names){
			//THE 4 are controlled by DBMS - or specific calls (e.g. delete)
			if( !"deactivated".equals(name) && !"created".equals(name) && !"modified".equals(name) && !"id".equals(name)){
				namesString = namesString + name + " = ?,";
				placeHolderNames.add(name);
			}
		}
		
		placeHolderNames.add("id");
		
		if( namesString.endsWith(",")){
			namesString = namesString.substring(0, namesString.lastIndexOf(","));
		}
		

		perp = perp.replace("[[COLUMN_NAMES]]", namesString);
		
		log.trace(perp);

		if( conn != null){
			stmt = conn.prepareStatement(perp);
		}
		stmt = this.setPreparedStatementValues(stmt, placeHolderNames);
		
		return stmt;
	}
	
	private PreparedStatement setPreparedStatementValues(PreparedStatement stmt, List<String> names) throws Exception{
		return setPreparedStatementValues( stmt,  names, true );
	}
	
	//INCLUDE ID in the values set based on the domain objects getters
	private PreparedStatement setPreparedStatementValues(PreparedStatement stmt, List<String> names, boolean includeID) throws Exception{
		int colCount = 1;
		for(String name : names){
			String type = this.determineColumnType(this.createGetterNameFromColumn(name));
			String val = this.determineWhereValue(name,false);
			
			boolean shouldSet = (!"deactivated".equals(name) && !"created".equals(name) && !"modified".equals(name));
			
			if( !includeID ){
				shouldSet = shouldSet && !"id".equals(name);
			}
			//boolean hasValueOtherThanNull = false;			
			if( shouldSet ){
				 log.trace(colCount+ ":" +name  + "(" +type + ")" + "=="+val);
				 if( type.endsWith("String")){
					 if( val == null){
						 stmt.setNull(colCount, java.sql.Types.VARCHAR);
					 }else{
						 stmt.setString(colCount, val);
					 }
				 }else if( type.endsWith("Long")){
					 if( val == null || "".equals(val)){
						 stmt.setNull(colCount, java.sql.Types.BIGINT);
					 }else{
						 stmt.setLong(colCount, Long.parseLong(val));
					 }
				 }else if( type.endsWith("Float")){
					 if( val == null || "".equals(val)){
						 stmt.setNull(colCount, java.sql.Types.FLOAT);
					 }else{
						 stmt.setFloat(colCount, Float.parseFloat(val));
					 }
				 }
				 else if( type.endsWith("Integer")){
					 if( val == null || "".equals(val)){
						 stmt.setNull(colCount, java.sql.Types.INTEGER);
					 }else{
						 stmt.setInt(colCount, Integer.parseInt(val));
					 }
				 }else if( type.endsWith("InputStream")){
					 InputStream blob = this.determineWhereValueBlob(name);
					 if( blob == null ){
						 stmt.setNull(colCount, java.sql.Types.BLOB);
					 }else{
						 stmt.setBlob(colCount, blob);
					 }
				 }
			}
			 colCount++;
		}
		return stmt;
	}
	
	/**
	 * @return the resultant update query for this domain object
	 */
	public String createUpdateQuery() {
		String sql = "UPDATE " + this.tableName() + " SET [[VALUES]]  WHERE ID =" +this.getId();
		
		String valueString = "";
		List<String> names = this.listOfColumnNames();
		for(String name : names){
			String whereValue = this.determineWhereValue(name);
			
			//THE 4 are controlled by DBMS - or specific calls (e.g. delete)
			if( !"deactivated".equals(name) && !"created".equals(name) && !"modified".equals(name)&& !"id".equals(name)){
				if( whereValue == null || ("null").equals(whereValue) || ("'null'").equals(whereValue)){
					valueString = valueString + name + "=null,";
				}else{
					valueString = valueString + name + "=" + whereValue + ",";
				}
			}
		}
		
		if( valueString.endsWith(",")){
			valueString = valueString.substring(0, valueString.lastIndexOf(","));
		}
		

		
		if( !"".equals(valueString) && this.getId() != 0){
			sql = sql.replace("[[VALUES]]", valueString);
		}else{
			sql = "";
		}
		log.trace(sql);
		return sql;
	}
	
	public String createDeleteQuery(){
		String sql = "DELETE FROM " + this.tableName() + " WHERE ID =" +this.getId();
		
		if(this.getId() == 0){
			return "";
		}
		log.trace(sql);
		return sql;
	}
	protected String determineWhereValue(String getterName){
		return determineWhereValue( getterName, true);
	}
	protected String determineWhereValue(String getterName, boolean safe){
		try{
			
			 String mName = createGetterNameFromColumn(getterName);
			 Method method = this.getClass().getMethod(mName);

	    	method.setAccessible(true);
	    	Object valueObject = method.invoke(this, (Object[]) null);
		    String content =  valueObject != null ? valueObject.toString() : null;
		    
		    String paramType = method.getReturnType().getName();
		    content = this.convertAttributeValueBasedOnType( content, paramType, safe);

		    return content;
					    


		}catch(Exception e){};
		return "";
	}
	
	protected InputStream determineWhereValueBlob(String getterName){
		try{
			
			 String mName = createGetterNameFromColumn(getterName);
			 Method method = this.getClass().getMethod(mName);

	    	method.setAccessible(true);
	    	InputStream valueObject = (InputStream)method.invoke(this, (Object[]) null);

		    return valueObject;
					    


		}catch(Exception e){};
		return null;
	}
	
	protected String determineWhereValueForCompositeKey(String getterName){
		String content =  "";
		try{
			
			 String mName = createGetterNameFromColumn(getterName);
			 Method method = this.getClass().getMethod(mName);
			 boolean isComposite = method.isAnnotationPresent(CompositeAttribute.class);
			 
			if( isComposite || "getid".equals(mName.toLowerCase())){
		    	method.setAccessible(true);
		    	Object valueObject = method.invoke(this, (Object[]) null);
			    content =  valueObject != null ? valueObject.toString() : null;
			    
			    String paramType = method.getReturnType().getName();
			    content = this.convertAttributeValueBasedOnType( content, paramType, true);
			}

		    return content;
					    


		}catch(Exception e){};
		return content;
	}
	
	private String convertAttributeValueBasedOnType(String content, String paramType,boolean safe){
	    if( paramType.endsWith("String")){
	    	if( safe ){
	    		content = "'"+this.makeSqlSafe(content)+"'";
	    	}
	    }else if(paramType.endsWith("Integer")){
	    	if(null == content || "null".equals(content)){
	    		content = "";
	    	}
//	    }else if(paramType.endsWith("Long")){
//	    	if(null == content || "null".equals(content) || "0".equals(content) || "-1".equals(content)){
//	    		content = "";
//	    	}
	    }else if(paramType.endsWith("Long")){
//	    	if(null == content || "null".equals(content) || "0".equals(content) || "-1".equals(content)){
//	    		content = "";
//	    	}
	    }else if(paramType.endsWith("Float")){
//	    	if(null == content || "null".equals(content) || "-1".equals(content)){
//	    		content = "";
//	    	}
	    }else if(paramType.endsWith("long")){
	    	if( "0".equals(content) || "-1".equals(content)){
	    		content = "";
	    	}
	    }else if(paramType.endsWith("InputStream")){
	    	if( "0".equals(content) || "-1".equals(content)){
	    		content = "";
	    	}
	    }else if(paramType.endsWith("int")){

	    }else if(paramType.endsWith("float")){
	    	if( "0.0".equals(content)){
	    		content = "";
	    	}
	    	
	    }
	    
	    return content;
	}
	
	protected String determineColumnType(String setterName){
		try{
		   Method method = this.getClass().getMethod(setterName);


	    	method.setAccessible(true);
		    
		    String paramType = method.getReturnType().getName();

		    return paramType;
					    


		}catch(Exception e){
			e.printStackTrace();
		};
		return "";
	}
	
	/**
	 * Loop through all the getters on the domain object that have
	 * the @CompositeAttribute and build a where clause for the domain
	 * object based on the fact that it exists. All getters must
	 * return non-null values for the compiste key to be valid.
	 * <br/><br/>
	 * <b>Compiste key</b> a combination of two or more columns in a table that can be used to uniquely identify each row in the table
	 * 
	 * @return where clause value for all the composite key elements.
	 */
	//TODO: warn - IF ANY OF THE VALUES ARE NULL THEN THIS KEY IS INVALID
	protected String buildCompositeKeySelectWhereClauseFromAnnotations() {
		final String WHERE = " WHERE ";
		StringBuilder sb = new StringBuilder(WHERE);
		String id = (null == this.getId() || this.getId() == 0)?"":"id="+this.getId()+" and ";
		sb.append( id );
		try{

		Class<?> obj = this.getClass();

	
		// Process @CompositeAttribute
		for (Method method : obj.getDeclaredMethods()) {

			// if method is annotated with @Test
			if (method.isAnnotationPresent(CompositeAttribute.class)) {

				//Annotation annotation = method.getAnnotation(CompositeAttribute.class);
				//CompositeAttribute attribute = (CompositeAttribute) annotation;
				String getterName = method.getName();
				
				
				method.setAccessible(true);
		    	Object valueObject = method.invoke(this, (Object[]) null);
			    String content =  valueObject != null ? valueObject.toString() : null;
			    
			    String paramType = method.getReturnType().getName();
		    	
			    content = this.convertAttributeValueBasedOnType( content, paramType, true);

			    if( !"'null'".equals(content)){
				    sb.append(this.createColumnNameFromGetter(getterName));
					sb.append(" = ");
					sb.append(content);
					sb.append(" and ");
			    }

			}

		}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String result = sb.toString();
		if( result.endsWith(" and ")){
			result = result.substring(0, result.lastIndexOf(" and "));
		}
		if(WHERE.equals(result)){
			result = "";
		}
		return result;
	}

	
	public void setPersistenceManager(IPersistenceManager mgr){
		this.pManager = mgr;
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