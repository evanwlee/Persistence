package com.evanwlee.utils.threading.mutable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.evanwlee.utils.logging.LoggerFactory;




/**
 * @author evanl
 * 
 * The ThreadWatchDog class is the watchdog used to monitor threads
 * in an application. There are two dogs used to monitor, the ALPHA_DOG
 * and the BETA_DOG. The BETA_DOG's sole responsibility is to monitor
 * the ALPHA_DOG. The ALPHA_DOG monitors the BETA_DOG and all assigned
 * threads for monitoring. If a thread dies, the dogs will report an
 * exception. If the dead thread is a MutableThread, the dog will try to
 * restart it.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ThreadWatchDog extends MutableThread {
	
	private Logger logger = LoggerFactory.getLogger(ThreadWatchDog.class.getName());


    private static ThreadWatchDog mAlphaThread = null; // alphadog instance
    private static ThreadWatchDog mBetaThread = null; // betadog instance
    
	private static HashMap mThreadHashMap = null; // vector of all threads
    private static int mAlphaPriority = Thread.NORM_PRIORITY;
    private static int mBetaPriority =  Thread.NORM_PRIORITY;
    private static long POLL_TIME = 30000; //  3 second check for TEST Change this to 60000 for one min. etc
    /** The attribute mWatchDogPollCount is added for test interest. It shows
     * how many times the polling has occurred within the dog.
     */
    public  int mWatchDogPollCount = 0; // test count
    //private int mDogIdentity = 0;

    private ThreadWatchDog() {
        super();
        mThreadHashMap = new HashMap();

    }

    /** The getInstance method insures that the ALPHA_DOG (and therefore also
     * the BETA_DOG) is a singleton. It is static so that it can be referenced
     * throughout the application within the JVM.
     */
    public synchronized static ThreadWatchDog getInstance(){
        if (mAlphaThread == null) {
            mAlphaThread = new ThreadWatchDog();
            mAlphaThread.setPriority(mAlphaPriority);
            mAlphaThread.setName("ALPHA");
            mBetaThread = new ThreadWatchDog();
            mBetaThread.setPriority(mBetaPriority);
            mBetaThread.setName("BETA");
            // these dogs watch each other
            mAlphaThread.put(mBetaThread);
            mBetaThread.put(mAlphaThread);
        }
        return mAlphaThread;
    }


    /** This is the run method for the thread.
     */
    public void run() {
    	boolean run = true;
        while(run) {
            try {
                checkAllThreads();
                poll();
            } catch (Exception eReportingException) {
            	logger.error("General failure in WatchDog", eReportingException);
            	run = false;
            }
        }
    }


    /** The checkAllThreads method for a dog checks each thread which has been
     * put into the HashMap.
     */

     public synchronized void checkAllThreads()  {
    	logger.trace( this.getName()+" is checking to see if all threads are alive.");
        Set lThreadSet = mThreadHashMap.keySet();
        Iterator lThreadIterator = lThreadSet.iterator();

        while(lThreadIterator.hasNext()){
            String lThreadKey = (String) lThreadIterator.next();
            Object lTestThread =  mThreadHashMap.get(lThreadKey);
            try{Thread.sleep(5000);}catch(Exception e){}

            if (lTestThread instanceof MutableThread){

                if (!((MutableThread)lTestThread).isAlive()){
                	if ( mWatchDogPollCount > 2 ) {
                		//logic issue in this open source.  
                		//When starting the ALPHA watchDog the BETA has yet to fire
                		//so it does not make sense to worry about it for a few cycles
                		logger.error("Mutable Thread " + lThreadKey + " is dead");
                	}
                    try {
                        logger.warn(this.getName()+" is attempting to restart thread : " + lThreadKey);
                        // attempt to restart the thread by clearing and restarting
                        //logger.debug("Creating Thread for " + lThreadKey);
                        ((MutableThread)lTestThread).createThread();
                        //logger.debug("Starting " + lThreadKey);
                        ((MutableThread)lTestThread).start();
                        logger.info("Thread Restart was Successful");
                    } catch (Exception eException) {
                    	logger.debug("Thread " + lThreadKey + " restart failure",eException);
                    }
                }else {
                	logger.trace("Mutable Thread " + ((MutableThread)lTestThread).getName() + " is Alive");
                }

                //logger.debug("Thread " + ((MutableThread)lTestThread).getName() + " is alive ");
             }else  { // not a Mutable thread
                if (!((Thread)lTestThread).isAlive()){
                   logger.error("Thread " + lThreadKey + " is dead & cannot be restarted ( not Mutable )");
                }else {
                    // thread is alive
                	logger.trace("Thread " + ((Thread)lTestThread).getName() + "  is Alive");
                }
            }
        }
    }

    /** The put method puts a mutable thread into the HashMap for monitoring by a dog.
     * Note that this is an overloaded method which also allows for non-mutable Threads to
     * be monitored as well.
     */
	public synchronized void put(MutableThread aMutableThread) {
        //logger.debug("ThreadWatchDog.adding mutable thread : " + aMutableThread.getName());
        mThreadHashMap.put(aMutableThread.getName(), aMutableThread);
    }

    /** The put method causes the dog to monitor the passed Thread.
     */
    public synchronized void put(Thread aThread) {
        //logger.debug("ThreadWatchDog.adding thread : " + aThread.getName());
        mThreadHashMap.put(aThread.getName(), aThread);
    }

    private synchronized void poll() {
        try {
            mWatchDogPollCount++; // for test and monitoring, shows the number of times polling has occurred
            logger.trace("WatchDogPollCount is " + mWatchDogPollCount + " for " + this.getName());
            this.wait(POLL_TIME);
        } catch (InterruptedException eInterruptedException) {
            throw new RuntimeException( eInterruptedException );
        }
    }
    /** This is the main body for the test program running the ThreadWatchDogs
     * and also a few test threads.
     */
    @SuppressWarnings({ "static-access", "deprecation" })
	public  static void main(String[] args){
        System.out.println("TEST: Running Thread Watch Dog");

        TestThread threadOne = new TestThread();
        threadOne.setName("threadOne");
        TestThread threadTwo = new TestThread();
        threadTwo.setName("threadTwo");
        TestMutableThread threadMutable = new TestMutableThread();
        threadMutable.setName("threadMutable");
        ThreadWatchDog.getInstance().put(threadOne);
        ThreadWatchDog.getInstance().put(threadTwo);
        ThreadWatchDog.getInstance().put(threadMutable);
        threadOne.start();
        System.out.println("TEST: Thread One started");
        threadTwo.start();
        System.out.println("TEST: Thread Two started");
        threadMutable.start();
        MutableThread lWatchDog = ThreadWatchDog.getInstance();
        System.out.println("TEST: Starting the watchdog");
        lWatchDog.start();
        System.out.println("TEST: Stopping threadOne");
        threadOne.stop();
        System.out.println("TEST: Stopping threadMutable");
        threadMutable.stop();
        while(((ThreadWatchDog)lWatchDog).mWatchDogPollCount < 2) {
            Thread lThread = Thread.currentThread();

            synchronized(lWatchDog) {
                try {
                    lThread.sleep(1000); // 1 second timeout
                } catch (InterruptedException eInterruptedException) {
                    eInterruptedException.printStackTrace();
                }
            }
        }
        System.out.println("TEST: Stopping the ALPHA Watchdog itself after running for a while");

        lWatchDog.stop();
    }
}

class TestThread extends Thread {
    private static final int WAIT_TIME = 100; // .1 second wait time for TEST
    public void run() {
        while(true) {
            synchronized(this) {
                try {
                    this.wait(WAIT_TIME);
                } catch (InterruptedException eInterruptedException) {
                    eInterruptedException.printStackTrace();
                }
            }
        }
    }
}

class TestMutableThread extends MutableThread {

    private static final int WAIT_TIME = 100; // .1 second wait time for TEST

    public void run() {
        while(true) {
            Thread lThread = Thread.currentThread();
            synchronized(lThread) {
                try {
                    lThread.wait(WAIT_TIME);
                } catch (InterruptedException eInterruptedException) {
                    eInterruptedException.printStackTrace();
                }
            }
        }
    }
}

