package com.evanwlee.utils.threading.mutable;

/**
 * @author evanl
 * 
 * The abstract class, MutableThread implements the MutableRunnable interface.
 * It is abstract, since it does not implement the run method, which must
 * be implemented by all instances inheriting from this class.
 */
public abstract class MutableThread implements MutableRunnable {

    private int mPriority = Thread.NORM_PRIORITY;
    Thread mThisThread = null;
    String mThreadName = "MutableThread";


    /** The constructor for MutableThread has no passed parameter. It invokes
     * a call to the createThread method, which in turn creates the owned thread.
     */
    public MutableThread() {
        createThread();
    }


    /** The implemented start method starts the thread.
     */
    public void start(){
        mThisThread.start();

    }

    /** This is the implemented start method which first sets the passed parameter
     * for priority and then invokes the start method for the thread.
     */
    public void start(int aPriority) // this method starts the thread with a given priority.
    {
        setPriority(aPriority);
        start();

    }

	/** This method is provided for test purposes only
     * the method is deprecated.
     */
    @SuppressWarnings("deprecation")
	public void stop(){
		mThisThread.stop(); // deprectated method accessed for test purposes only
	}

	/** This method provides the test for isAlive required for monitoring thread status
     *
     */
    public boolean isAlive(){
		return mThisThread.isAlive();
	}
    /** The setName method passes the string for assignment to the thread
     * as name. This string is retained as an attribute for assignment
     * to any thread which is owned by this mutable object.
     */
    public void setName(String aThreadName) {
        mThreadName = aThreadName;
        if (mThisThread != null) mThisThread.setName(aThreadName);

    }

	/** The getName method returns the assigned name for the thread.
     */
    public String getName()
    {
		  return mThreadName;
	}

    /** The setPriority(int) method passes the parameter for priority to the
     * mutable object This priority is assigned to any thread owned by this
     * object.
     */
    public void setPriority(int aPriority)
    {
        mPriority = aPriority;
        if (mThisThread != null)
        mThisThread.setPriority(mPriority);
    }


    /** The createThread method makes a new thread, the old thread is no longer
     * used by this mutable object.
     */
    public void createThread() {
        mThisThread = new Thread(this, mThreadName);
        mThisThread.setPriority(mPriority);
    }

    // this is abstract to enforce implementation
    /** This abstract method for run must be implemented by all extending classes.
     */
    public abstract void run();


}
