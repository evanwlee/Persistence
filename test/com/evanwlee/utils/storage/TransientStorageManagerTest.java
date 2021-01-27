package com.evanwlee.utils.storage;

import org.junit.Test;

import com.evanwlee.utils.file.FileUtils;

public class TransientStorageManagerTest {
	
	@Test
    public void storeRecordTest() throws Exception{
		try{
			String pathToFile = "/tmp/testFarse.txt";
			FileUtils.writeTextFile("Farse For Testing", pathToFile);
			TransientDataManager.current().storeData("TEST", pathToFile, "com.evanwlee.b2c.TransientStorageManagerTest");
			FileUtils.delete(pathToFile);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
