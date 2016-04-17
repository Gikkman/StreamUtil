package com.gikk.streamutil.task;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


class Scheduler {
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private static class Holder {	static final Scheduler INSTANCE = new Scheduler();  }
	
	private final ScheduledThreadPoolExecutor executor;
	
	//***********************************************************************************************
	//											STATIC
	//***********************************************************************************************
	public static Scheduler GET(){	
		return Holder.INSTANCE;
	}
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	private Scheduler () { 
		executor = new ScheduledThreadPoolExecutor(5);
	}
	
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	/**Schedules a RepeatedTask to be executed once every periodSeconds. The RepeatedTasks onUpdate() will be called
	 * the first time after periodSeconds. To execute a certain action as soon as the service have been scheduled, override
	 * the RepeatedTasks onScheduled() method.
	 * 
	 * @param task
	 * @param periodMillis
	 */
	public ScheduledFuture<?> scheduleRepeatedTask(RepeatedTask task, int periodMillis) {	
		ScheduledFuture<?> out = executor.scheduleAtFixedRate( () -> task.onExecute() , periodMillis, periodMillis, TimeUnit.MILLISECONDS);
		task.onScheduled();
		
		return out;
	}
	
	/**Postpones a OneTimeTask for delayMillis. After the assigned delay, the task will be executed once.
	 * 
	 * @param oneTimeTask
	 * @param delayMillis
	 */
	public void scheduleOneTimeTask(OneTimeTask oneTimeTask, int delayMillis) {		
		executor.schedule( () -> oneTimeTask.onExecute() , delayMillis, TimeUnit.MILLISECONDS);
	}
	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************
	private void onProgramExit() {
		//TODO: Find a convenient way to call this method...
		
		Thread thread = new Thread( () -> {
			try {
				executor.shutdown();
				executor.awaitTermination(120, TimeUnit.SECONDS);
				executor.shutdownNow();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} );		
		thread.setDaemon( false );
		thread.start();
	}
}
