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
	
	private ScheduledFuture<?> future;
	
	public final void schedule(int periodSeconds) {
		future = Scheduler.GET().scheduleRepeatedTask(this, periodSeconds);		
	}
	
	public final void endTask(){
		if( future == null )
			return;
		
		if( !future.cancel(false) ){
			System.err.println("\tUnable to cancel task");
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
