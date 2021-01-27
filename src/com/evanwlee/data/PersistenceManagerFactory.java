package com.evanwlee.data;

import java.util.Properties;

import com.evanwlee.utils.properties.PropertyLoader;

public class PersistenceManagerFactory {
	private volatile static PersistenceManagerFactory me = null;
	
	private String className = "com.evanwlee.data.MysqlPooledPersistenceManager";

	PersistenceManagerFactory(){
		Properties app = PropertyLoader.loadProperties("resources.app.properties");
		String env = app.getProperty("APP.ENV","LOCAL");
		className = app.getProperty("APP."+env+".PERSISTENCE.MANAGER","com.evanwlee.data.MysqlPooledPersistenceManager");
	}
	public static PersistenceManagerFactory current() {
		if (me == null) {
			synchronized (PersistenceManagerFactory.class) {
				if (me == null) {
					me = new PersistenceManagerFactory();
				}
			}
		}
		return me;
	}
	
	/**
	 * @return The default manager which is the simple
	 * MysqlPersistenceManager which creates a connection for every request
	 */
	public IPersistenceManager getSimplePersistenceManager(){
		return MysqlPersistenceManager.current();
	}
	/**
	 * @return The default poold manager which is the simple
	 * MysqlPooledPersistenceManager which creates a connection pool, much faster
	 */
	public IPersistenceManager getPooledPersistenceManager(){
		return MysqlPooledPersistenceManager.current();
	}
	/**
	 * @return The default poold manager which is the simple
	 * MysqlPooledPersistenceManager which creates a connection pool, much faster
	 */
	public IPersistenceManager getPreparedStatementPersistenceManager(){
		return MysqlPreparedStatementPersistenceManager.current();
	}
	
	/**
	 * @return returns the configured Persitence Manager as configure
	 * in app.properties
	 */
	public IPersistenceManager getPersistenceManager(){
		switch(className){
			case "com.evanwlee.data.MysqlPooledPersistenceManager":
				return MysqlPooledPersistenceManager.current();
			case "com.evanwlee.data.MysqlPersistenceManager":
				return MysqlPersistenceManager.current();
			case "com.evanwlee.data.MysqlPreparedStatementPersistenceManager":
				return MysqlPreparedStatementPersistenceManager.current();
			default:
				return MysqlPooledPersistenceManager.current();
		}	
	}
	

}
