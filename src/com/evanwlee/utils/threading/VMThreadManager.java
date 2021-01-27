package com.evanwlee.utils.threading;


import com.evanwlee.utils.threading.mutable.MutableThread;
import com.evanwlee.utils.threading.mutable.ThreadWatchDog;
import com.evanwlee.utils.threading.work.IAsynchCommand;

public class VMThreadManager {
	//private static final Logger LOGGER = Logger.getLogger(VMThread.class);
	private volatile static VMThreadManager manager;
	private VMQueue queue;
	
	private boolean inited = false;
	
	private VMThread thread = null;
	
	public static VMThreadManager current() {
		if ( manager == null ) {
			synchronized (VMThreadManager.class) {
				manager = new VMThreadManager();
				manager.initialize();
			}
		}
		return manager;
	}
	
	
	 /**
	  * Fire up the asynchronous process for executing 
	  * asynch actions in web app
	  *
	  */
	private void initialize() {
		if ( !inited ) {
	     	thread = new VMThread();
	     	thread.setName("VMThread");
	        ThreadWatchDog.getInstance().put(thread);
	        thread.start();
	        MutableThread lWatchDog = ThreadWatchDog.getInstance();
	        lWatchDog.start();
	        inited = true;
	        
	        queue = VMQueue.current();
		}
	}
	

	public void addToQueue(IAsynchCommand asynchCmd) {
		queue.addToQueue(asynchCmd);
	}
	
	public void stop() {
		thread.stop();
	}

}
