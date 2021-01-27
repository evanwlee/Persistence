package com.evanwlee.setup;

import java.util.List;

import com.evanwlee.data.IPersistenceManager;
import com.evanwlee.data.PersistenceManagerFactory;
import com.evanwlee.data.domain.Domain;
import com.evanwlee.data.domain.IDomain;
import com.evanwlee.generate.file.FileDataType;
import com.evanwlee.utils.date.DateUtils;
import com.evanwlee.utils.string.RandomString;


public class MockDataSetupManager {
	protected IPersistenceManager pManager = PersistenceManagerFactory.current().getPersistenceManager();

	public static final String TEST_Customer_CONSTANT = "TESTRUN";
	
	public static String TEST_ORDER_JSON = "";
	
	public static final String LIKE_KEY = "TEST-|-";
	
	public void createTestRecord(){

	}
	
	public void destroyAllTestRecords(){

	}
	
	public static void main(String... args){
		MockDataSetupManager me = new MockDataSetupManager();
		
		me.createTestRecord();
		me.destroyAllTestRecords();
		
	}

}
