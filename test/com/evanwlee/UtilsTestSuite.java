package com.evanwlee;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

//JUnit Suite Test
@RunWith(Suite.class)
@Suite.SuiteClasses({ 
   com.evanwlee.load.MySqlPersistenceManagerTest.class ,
   com.evanwlee.data.domain.WCDomainFactoryTest.class,
   com.evanwlee.utils.string.RandomStringTest.class
})
public class UtilsTestSuite {
}