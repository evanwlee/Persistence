package com.evanwlee.utils.threading;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.evanwlee.utils.threading.work.IAsynchCommand;

public class VMQueue {
	private static final Logger LOGGER = Logger.getLogger(VMQueue.class);
	private ConcurrentLinkedQueue<IAsynchCommand> queue = new ConcurrentLinkedQueue<IAsynchCommand>();
	private volatile static VMQueue manager;
	
	public static VMQueue current() {
		if ( manager == null ) {
			synchronized (VMQueue.class) {
				manager = new VMQueue();
			}
		}
		return manager;
	}
	
	void addToQueue(IAsynchCommand payload) {
		queue.add(payload);
		LOGGER.trace("Attempting to add work to the queue. Queue meter at " + queue.size());
	}
	
	int size() {
		return queue.size();
	}
	
	IAsynchCommand getNextFromQueue() {
		if ( queue.isEmpty() ) {
			LOGGER.trace("Work queue is empty, nothing to do.");	
			return null;
		} else {
			LOGGER.trace("Processing work from the queue. Queue meter at " + queue.size());	
			return queue.remove();
		}
	}
}

