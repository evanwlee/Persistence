package com.evanwlee.service.twitter;

import static org.junit.Assert.assertFalse;


import org.junit.Test;

//import com.evanwlee.setup.MockDataSetupManager;
import com.evanwlee.utils.string.RandomString;
import com.evanwlee.utils.token.IReturnToken;


public class TwitterServiceTest {

	static {

	}


    @Test
    public void postUniqueToTwitterTest() {
    	String uniqueText = "Test message to twitter" + new RandomString(6).nextString();
    	IReturnToken result = TwitterServiceManger.current().postToTwitter(uniqueText);
 		assertFalse("Was able to submit unique post to twitter",!result.isFailure());
    }
    

}
