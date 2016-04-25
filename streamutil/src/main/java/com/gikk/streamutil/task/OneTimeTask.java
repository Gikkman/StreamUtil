package com.gikk.streamutil.task;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**One time tasks are fired once, but after an initial delay of X millis.
 * 
 * @author Simon
 *
 */
public abstract class OneTimeTask implements GikkTask{
	private ScheduledFuture<?> future;
	
	public final void schedule(int delayMilis) {
		this.future = Scheduler.GET().scheduleOneTimeTask(this, delayMilis );
	}
	
	/**Interrupts the task and forces it to stop. Useful for task that waits for
	 * responses or for resources. It is safe to call this method even if the task
	 * is done.<br><br>
	 * 
	 * After this method returns, subsequent calls to {@code isDone()} will always return true
	 */
	@SuppressWarnings("unused")
	public final void forceStopIfRunning(){
		if( future != null ){
			long running = this.future.getDelay(TimeUnit.MILLISECONDS);
			boolean done = this.future.isDone();
			boolean cancel = this.future.cancel(true);
			
		}
	}
	
	/**
     * Returns {@code true} if this task completed.
     *
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * {@code true}.
     *
     * @return {@code true} if this task completed
     */
	public boolean isDone(){
		try{
			return future.isDone();
		} catch (NullPointerException e){
			throw new NullPointerException("Error. The task has to be scheduled before this method can be invoked.");
		}
	}
	
	/**Check if this task is still running. A task is considered running if there is 0 or less milliseconds till it will start
	 * running and it is not done yet.
	 * 
	 * @return {@code true}  if the task is running. {@code false} if the task is still waiting to run <b>or</b> 
	 * if the task is still running
	 */
	public boolean isRunning() {
		try{
			//A task is running if it is not done AND if there are 0 or less milliseconds until it will start running.
			return future.getDelay(TimeUnit.MILLISECONDS) <= 0 && !future.isDone();
		} catch (NullPointerException e){
			throw new NullPointerException("Error. The task has to be scheduled before this method can be invoked.");
		}
	}

}
