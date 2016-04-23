package com.gikk.streamutil.task;

/**One time tasks are fired once, but after an initial delay of X millis
 * 
 * @author Simon
 *
 */
public abstract class OneTimeTask implements GikkTask{
	
	public final void schedule(int delayMilis) {
		Scheduler.GET().scheduleOneTimeTask(this, delayMilis );
	}
}
