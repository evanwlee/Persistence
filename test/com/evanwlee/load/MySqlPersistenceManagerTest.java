package com.evanwlee.load;


import java.sql.Connection;

import org.junit.Test;

import com.evanwlee.data.PersistenceManagerFactory;
import com.evanwlee.data.domain.evanwlee.Customer;

import static org.junit.Assert.assertTrue;


public class MySqlPersistenceManagerTest {
    @Test
    public void ableToConnectToDatabase() {
    		try{
	           Connection con = PersistenceManagerFactory.current().getPersistenceManager().createConnection();
	           assertTrue("The connectin object was returned",con != null);
	           
	           assertTrue("The connectin could create a stament", con.createStatement() != null);
           
           
	           PersistenceManagerFactory.current().getPersistenceManager().safeClose(con);
           }catch(Exception e){
        	   
           }
    }
    
    
    @Test
    public void createDomainObject() {
    		try{
    			Customer customer = new Customer();
    			customer.setName("TEST_Customer_INSERT");
    			Customer fetchedCustomer = customer.fetchOrCreate();
    			
    			 assertTrue("The fetch matches the created", fetchedCustomer.getName().equals(customer.getName()));
    			 
    			 fetchedCustomer.delete();
           }catch(Exception e){
        	   
           }
    }
    
    @Test
    public void updateDomainObject() {
    		try{
    			String ORIG_NAME = "TEST_CUSTOMER_UPDATE";
    			Customer customer = new Customer();
    			customer.setName(ORIG_NAME);
    			Customer fetchedCustomer = customer.fetchOrCreate();
    			
    			assertTrue("The fetch matches the created", ORIG_NAME.equals(fetchedCustomer.getName()));
    			
    			String NEW_NAME = "TEST_CUSTOMER_UPDATE_ALTER";
    			fetchedCustomer.setName(NEW_NAME);
    			fetchedCustomer.update();
    			
    			
    			
    			Customer customerQbe = new Customer();
    			customerQbe.setName(NEW_NAME);
    			fetchedCustomer = (Customer)customerQbe.fetch().get(0);
    			
    			assertTrue("The fetch matches the updated", NEW_NAME.equals(fetchedCustomer.getName()));
    			 
    			 fetchedCustomer.delete();
           }catch(Exception e){
        	   
           }
    }
}
