package com.evanwlee.service.twitter;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.evanwlee.utils.logging.LoggerFactory;
import com.evanwlee.utils.properties.PropertyLoader;

public class TwitterConfig {
	
	private static Logger log = LoggerFactory.getLogger(TwitterConfig.class.getName());

	public static String CONSUMER_KEY = "";
	public static String CONSUMER_SECRET = "";
    public static String ACCESS_TOKEN = "";
    public static String ACCESS_TOKEN_SECRET = "";

    static{
    	Properties endpointProperties = PropertyLoader.loadProperties("resources.app.properties");
		String env = endpointProperties.getProperty("APP.ENV", "LOCAL");
		if( "".equals(env) || env.equals(null)){
			env = "LOCAL";
		}
		CONSUMER_KEY = endpointProperties.getProperty("APP."+env+".TWITTER.CONSUMER.KEY", CONSUMER_KEY);
		CONSUMER_SECRET = endpointProperties.getProperty("APP."+env+".TWITTER.CONSUMER.SECRET", CONSUMER_SECRET);
		ACCESS_TOKEN = endpointProperties.getProperty("APP."+env+".TWITTER.ACCESS.TOKEN", ACCESS_TOKEN);
		ACCESS_TOKEN_SECRET = endpointProperties.getProperty("APP."+env+".TWITTER.ACCESS.SECRET", ACCESS_TOKEN_SECRET);
    	log.info("Setting Twitter Tokens");
    }


}
