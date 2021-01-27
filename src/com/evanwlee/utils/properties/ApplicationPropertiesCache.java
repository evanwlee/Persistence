package com.evanwlee.utils.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;




public class ApplicationPropertiesCache implements ApplicationPropertiesCacheMBean{
	
	private static final Logger LOGGER = Logger.getLogger(ApplicationPropertiesCache.class);
	
	private volatile static ApplicationPropertiesCache current = null;
	public static final String APP_PROPERTIES = "/resources/app.properties";
	


	private ApplicationPropertiesCache(){
	}
	
	private ConcurrentHashMap<ApplicationProperties, String>  registry = null;
	
	public static ApplicationPropertiesCache current() {
		 if (current == null) { 
			 synchronized (ApplicationPropertiesCache.class) {
				 current = new ApplicationPropertiesCache();
				 
				 try{
						MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
						
						// Uniquely identify the MBeans and register them with the platform MBeanServer 
						ObjectName name = new ObjectName("OmbPropertyCache:name=OmbPropertyCache");
						mbs.registerMBean(current, name);
					 }catch(Exception e){
						 e.printStackTrace();
					 }
				
				 current.getRegistry();
				 
				
			 }
		 }
		 
		 return current;
	 }
	

	/**
	 * Get the registry of System Properties, (re)load as necssary.
	 * @return Map of Properties
	 */
	private ConcurrentHashMap<ApplicationProperties, String> getRegistry() {
		// synchronized for your protection, don't want to loads to occur simultaneously
		if(null == registry) {
			registry = loadRegistry();
		}
		
		return registry;
	}
	
	/**
	 * @return lazily loaded registry of Properties
	 */
	private ConcurrentHashMap<ApplicationProperties, String> loadRegistry() {
		LOGGER.debug("Loading properties from file");
		registry = new ConcurrentHashMap<ApplicationProperties, String>();
		BufferedReader lineReader = null;

			try{
				
				URL url = Loader.getResource(APP_PROPERTIES);
				File file = new File(url.getPath());

				InputStream in = new FileInputStream(file);
				lineReader = new BufferedReader(new InputStreamReader(in));
				
				String line = "";
				while(null != (line = lineReader.readLine())) {
					line = line.trim();
					if( line.length() == 0 ||
						line.matches("\\s*#.*") || 
						line.matches("\\s*")){
						continue;
					}
					if( line.indexOf("=") > -1){
						String[] values = line.split("=");
						if( values.length==2){
						registry.put(ApplicationProperties.determinValue(values[0]), values[1]);
						}else{
							registry.put(ApplicationProperties.determinValue(values[0]), "");
						}
					}else{
						registry.put(ApplicationProperties.determinValue(line), "");
					}
				}
			} catch(IOException ioe) {

				//// we don't want to return a half-filled Map, so instantiate a new, empty one on a failure
				registry = new ConcurrentHashMap<ApplicationProperties, String>();
			} catch(Exception ioe) {

				//// we don't want to return a half-filled Map, so instantiate a new, empty one on a failure
				registry = new ConcurrentHashMap<ApplicationProperties, String>();
			}finally{
				if(lineReader != null){try{lineReader.close();}catch(Exception e){}}
			}
		
		return registry;
	}
	
	/**
	 * They will be reloaded as needed.
	 */
	public String purgeCache() {
		registry = null;
		return "Properties Cache was purged.";
	}

	private String getValueFromCache(ApplicationProperties key){
		if(null == registry) {
			
			registry = loadRegistry();
		}
		return registry.get(key);
	}
	
//	private String getValueFromCache(ApplicationProperties key, String defaultValue){
//		String result = getValueFromCache(key);
//		if("".equals(result)){
//			result = defaultValue;
//		}
//		
//		return result;
//	}
	/**
	 * Tries to retrieve the value from the cache, and then from
	 * the properties loaded at start
	 * 
	 * @return value for the property key
	 */
	public String getExcludedDocumentCharacters(){
		String cached = getValueFromCache(ApplicationProperties.EXLUDED_DOC_MAN_CHARACTERS);
		return cached;
	}
	
	public String squelchableTaskTimerList(){
		String cached = getValueFromCache(ApplicationProperties.EXLUDED_DOC_MAN_CHARACTERS);
		return cached;
	}
}
