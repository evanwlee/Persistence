package com.evanwlee.utils.threading;

import org.apache.log4j.Logger;

import com.evanwlee.utils.threading.mutable.MutableThread;
import com.evanwlee.utils.threading.work.IAsynchCommand;


public class VMThread extends MutableThread implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(VMThread.class);
	
	private static final int SNAPSHOT_MAX_RETRY_COUNT = 5;
	private static int failureCount = 0;
	private boolean keepAlive = true;
	private int sleepCheckQueue = 5000;
	private int sleepBetweenProcessing = 5000;
	
	
	public void run() {
		while ( keepAlive ) {
			try {
				Thread.sleep(sleepCheckQueue);
				LOGGER.trace("Checking task queue for work...");
				doWork();
			} catch (Exception e) {
				failureCount++;
				LOGGER.error("General Failure in Usage Thread.  Failure count is < " + failureCount + " >", e);
				// 5 strikes and we are out
				if ( failureCount == SNAPSHOT_MAX_RETRY_COUNT ) {
					keepAlive = false;
				}			
			}						
		}
	}
	
	private void doWork() throws Exception {
		IAsynchCommand task = VMQueue.current().getNextFromQueue();
		while ( task != null ) {
			task.execute();
			//Natural pause so the VM is not slammed
			Thread.sleep(sleepBetweenProcessing);
			//rinse and repeat
			task = VMQueue.current().getNextFromQueue();
		}
	}
	
	public int getSleep() {
		return sleepBetweenProcessing;
	}


	public void setSleep(int sleep) {
		this.sleepBetweenProcessing = sleep;
	}
	

}
