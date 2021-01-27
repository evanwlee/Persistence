package com.evanwlee.service.twitter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.Logger;

import com.evanwlee.utils.logging.LoggerFactory;
import com.evanwlee.utils.string.RandomString;
import com.evanwlee.utils.token.IReturnToken;
import com.evanwlee.utils.token.ReturnTokenImpl;
import com.evanwlee.utils.token.StatusMessageImpl;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterServiceManger {
	//30 seconds
	public final long TIME_BETWEEN_CALLS_TO_TWITTER = 1 *    30 *   1000;
	
	private Logger log = LoggerFactory.getLogger(TwitterServiceManger.class.getName());
	private volatile static TwitterServiceManger currentManager = null;
	
	private volatile static Twitter twitter = null;
	
	private static final String LOCK = File.separator+"tmp"+File.separator+"twitterservice.lck";

	TwitterServiceManger() {
		twitter = new TwitterFactory().getInstance();
		
		twitter.setOAuthConsumer( TwitterConfig.CONSUMER_KEY, TwitterConfig.CONSUMER_SECRET );
		twitter.setOAuthAccessToken(new AccessToken( TwitterConfig.ACCESS_TOKEN,
				TwitterConfig.ACCESS_TOKEN_SECRET ));
	}
	
	/**
	 * Returns the current domain factory manager for the application.
	 */
	public static TwitterServiceManger current() {
		if (currentManager == null ) {
			synchronized (TwitterServiceManger.class) {
				if (currentManager == null) {
					currentManager = new TwitterServiceManger();
				}
			}
		}
		return currentManager;
	}

	/**
	 * Caller should throttle how often this call is made to avoid Twitter refusing 
	 * request. 
	 * 
	 * Will create a lock token that will prevent further calls for 10 minutes if
	 * an Exception is received from Twitter to avoid slamming Twitter. Client implementation 
	 * can include retry after that interval, if desired.
	 * 
	 * 
	 * @param message
	 * @return token with any failure messages.
	 */
	public IReturnToken postToTwitter(String message) {
		IReturnToken result = new ReturnTokenImpl();
		if(message != null && "".equals(message.trim())) {
			log.error("Call to post empty message to Twitter");
			result.setFailure(true);
			result.addMessage(
					new StatusMessageImpl(IReturnToken.Status.ERROR, 
							"Call to post empty message to Twitter"));
			
			return result;
		}else { 
			if( areServiceCallsAllowed() ) {
				try {
					twitter.updateStatus(message);
					log.info("Successfully posted tweet to Twitter: "  +message);
				} catch (TwitterException te) {
					result.setFailure(true);
					
					suspendSerivceCalls();
					
					if( te.getMessage() != null && te.getMessage().lastIndexOf("duplicate") > 1) {
						log.error("Looks like we are attempting to post a duplicate to twitter: " +message);
						result.addMessage(
								new StatusMessageImpl(IReturnToken.Status.ERROR, 
										"Looks like we are attempting to post a duplicate to twitter: " +message));
					}else {
						te.printStackTrace();
						result.addMessage(
								new StatusMessageImpl(IReturnToken.Status.ERROR, 
										"Was not able to post to Twitter!" + te.getMessage()));
					}
				}
			}else {
				log.debug("Twitter service calls currently paused ("+LOCK+" exists). Please try agian later.");
				result.setFailure(true);
				result.addMessage(
						new StatusMessageImpl(IReturnToken.Status.WARNING, 
								"Twitter service calls currently paused ("+LOCK+" exists)."));
			}
		}
		return result;
	}
	
	public void pauseToSpareTwitter() {
		this.pauseToSpareTwitter(TIME_BETWEEN_CALLS_TO_TWITTER);
	}
	
	/**
	 * Use this if making multiple calls in a loop.
	 * 
	 * @param desiredSleepMilis milliseconds to sleep between calls
	 * 
	 */
	public void pauseToSpareTwitter(long desiredSleepMilis) {
		try{
			Thread.sleep(desiredSleepMilis);
		}catch(Exception e) {//Don't slam twitter
			log.error("Publish Mention thread issue, was not able to sleep: "+e.getMessage());
		}
	}
	
	private boolean areServiceCallsAllowed() {
		//get pause token
		if( !doesPauseTokenExist() ) {
			return true;
		}

		//token older than 10 minutes
		if( isPauseTokenExpired() ) {
			this.resumeSerivceCalls();
			return true;
		}
		
		return false;
	}
	
	private boolean doesPauseTokenExist() {
		File f = new File(LOCK);
		return (f.exists() && !f.isDirectory());
	}
	
	//token older than 10 minutes
	private boolean isPauseTokenExpired() {
		 File file = new File(LOCK);
		 
		 long tenMinutesAgo = System.currentTimeMillis() - (10 * 60 * 1000);
		 if (file.lastModified() < tenMinutesAgo) {
			 log.info("Twitter lock file is expired"); 
		     return true;
		 }else {
			 return false;
		 }
	}
	
	private void suspendSerivceCalls() {
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(LOCK), "utf-8"));
		    writer.write("Prevent spamming of twitter service by b2c workers, can delete if you now it should not be throttled.");
		} catch (IOException ex) {
		    // Report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}
	
	private void resumeSerivceCalls() {
		//Delete Pause Token
		File file = new File(LOCK); 
        
        if(file.exists() && file.delete()) { 
            log.info("Twitter lock file deleted successfully"); 
        } else{ 
            log.warn("Twitter lock file could ot be deleted successfully"); 
        } 
	}

	public static void main(String[] args) {
		TwitterServiceManger.current().postToTwitter("Alte mention test."+new RandomString(10).nextString());
	}
}
