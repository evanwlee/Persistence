package com.evanwlee.utils.storage;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.evanwlee.data.domain.evanwlee.utility.TransientDataStore;
import com.evanwlee.utils.date.DateUtils;
import com.evanwlee.utils.logging.LoggerFactory;
import com.evanwlee.utils.properties.PropertyLoader;
import com.evanwlee.utils.threading.VMThreadManager;
import com.evanwlee.utils.token.IReturnToken;

public class TransientDataManager {
	
	private volatile static TransientDataManager currentManager = null;
	private boolean iShouldDoWork = false;
	private static Logger log = LoggerFactory.getLogger(TransientDataManager.class.getName());

	
	TransientDataManager(){
		Properties app = PropertyLoader.loadProperties("resources.app.properties");
		String env = app.getProperty("APP.ENV", "LOCAL");
		if( "".equals(env) || env.equals(null)){
			env = "LOCAL";
		}
		iShouldDoWork = Boolean.valueOf(app.getProperty("APP."+env+".USE_TRANSIENT_STORAGE", "false"));
	}

	public static TransientDataManager current() {
		if (currentManager == null ) {
			synchronized (TransientDataManager.class) {
				if (currentManager == null) {
					currentManager = new TransientDataManager();
				}
			}
		}
		return currentManager;
	}
	
	public IReturnToken storeData(String key, String file){
		return storeData( key,  file,  "com.evanwlee.b2c.TransientDataManager");
	}
	/**
	 * Creates that transient record with one month expiry
	 * @param key
	 * @param file
	 * @param classType
	 * @return
	 */
	public IReturnToken storeData(String key, String file, String classType){
		return storeData( key,  file,  DateUtils.dateOneMonthFromNowString(), classType);
	}
	
	public IReturnToken storeData(String key, String file, String expiry, String classType){
		if(iShouldDoWork){
			VMThreadManager.current().addToQueue(new CleanTransientDataAsynchCommand());
			
			try{
				TransientDataStore storage = new TransientDataStore();
				
				storage.setDataId(key);
				storage.setDataClass(classType);
				storage.setExpires(expiry);
				
				File theFile = new File(file);
				
				if(theFile.exists()){
					storage.setData(new FileInputStream(theFile));
					storage.save();
					return storage.modificationStatus();
				}else{
					log.warn("Was not able to create the TransientDataStore record. Input file does not seem to exist: "+file);
				}
				
				
				
				
			}catch(Exception e){
				log.error("Was not able to create the TransientDataStore record. Was input file valid: "+file+ e.getMessage());
			}
		}
		return null;
		
	}

}
