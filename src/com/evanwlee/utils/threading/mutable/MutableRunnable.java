package com.evanwlee.utils.threading.mutable;

/**
 * @author evanl
 * 
 * 
 * The MutableRunnable Interface describes an interface which creates an
 * owned thread by an object. It also provides access to that thread. The
 * MutableRunnable object retains it's own priority and name such that when the
 * crateThread method is invoked, the priority and name of the new thread
 * is the same as the old thread.
 */

public interface MutableRunnable extends Runnable {

    /** The start method is the standard Thread start method which will result
     * in starting the thread at the currently set priority.
     */
    public void start();

    /** The start(int) method starts the thread with a passed parameter for the
     * priority. This is a convenience method.
     */
    public void start(int aPriority);

    /** The setPriority(int) method is a method to set the thread priority. The
     * passed parameter is an integer describing the thread priority.
     */
    public void setPriority(int aPriority);


    /** The create method creates a new thread which is owned by the
     * MutableRunnable object.
     */
    public void createThread();

    /** setName(String) sets the thread name to the passed parameter string value.
     */
    public void setName(java.lang.String aName);
    /** getName() returns the thread name string
	     */
    public String getName();
    /** The isAlive method provides the test for isAlive required for monitoring thread status
	  *
	  */
	public boolean isAlive();

}