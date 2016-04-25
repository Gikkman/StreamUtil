package com.gikk.streamutil.task;

import java.util.concurrent.ScheduledFuture;

/**Repeted tasks are certain tasks that are performed every X milliseconds. Usually stuff like polling and the similar.
 * They use the Scheduler class, which holds a thread-pool, to perform their work. This way, we don't need to allocate
 * one thread per task.
 * 
 * @author Simon
 *
 */
public abstract class RepeatedTask implements GikkTask {	
	
	protected ScheduledFuture<?> future;
	
	/**Schedules this task so that it is called periodically. A task will remain in the scheduling queue until
	 * explicitly removed or until the program terminates. <br><br>
	 * 
	 * Directly after the task has been scheduled, the onSchedule method will be called. Execution of the onSchedule method
	 * is performed on the same thread that called this method.
	 * 
	 * @param initialDelayMilis	How long we wait until we execute this task the first time
	 * @param periodMilis	How long we wait until we execute this task from the previous time it was executed
	 */
	public final void schedule(int initialDelayMilis, int periodMilis) {
		future = Scheduler.GET().scheduleRepeatedTask(this, initialDelayMilis, periodMilis);		
	}
	
	/** Calling this method will attempt to end this task.<br>
	 *  If cancellation is successful, the <code>onEnd()</code> method will be called. Cancellation might fail if
	 *  the task has already been finished.<br>
	 * 
	 * @param mayInterrupt {@code true} if the thread executing this
     * task should be interrupted; otherwise, in-progress tasks are allowed
     * to complete
	 */
	public final void stopTask(boolean mayInterrupt){
		if( future == null )
			return;
		
		if( !future.cancel(mayInterrupt) ){
			System.err.println(" Unable to cancel task");
			return;
		}
		onEnd();	
	}
	
	//***********************************************************************************************
	//											TO BE OVERRIDDEN
	//***********************************************************************************************	
	/**The onScheduled method is called directly after the task has been added to the task scheduler
	 */
	protected void onScheduled(){ }
	
	/**The onEnd method is called directly after the task has been removed from the task scheduler.
	 * A task is only removed from the scheduler by something calling the 'endTask()' method on the task
	 */
	protected void onEnd() { }
}
