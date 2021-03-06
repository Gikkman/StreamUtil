package com.gikk.streamutil.task;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**<b>Singleton</b> <br><br>
 * 
 * This class handles scheduling of tasks that should be executed one or more times sometime in the future.<br><br>
 * 
 * This class uses a ScheduledThreadPoolExecutor to execute the different tasks. That has the consequence that a thread
 * might be running for a while after the program tells the Scheduler to terminate. <b>This is normal and may take up to 60 seconds.</b>
 * After 120 seconds the Scheduler force-terminates all remaining tasks.
 * 
 * @author Simon
 *
 */
public class Scheduler {
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
	 * the first time after initDelayMillis. To execute a certain action as soon as the service have been scheduled, override
	 * the RepeatedTasks onScheduled() method.
	 * 
	 * @param task The task to the executed
	 * @param initDelayMillis How long we wait until we execute this task the first time
	 * @param periodMillis How long we wait from the previous execution to the next
	 */
	ScheduledFuture<?> scheduleRepeatedTask(RepeatedTask task, int initDelayMillis, int periodMillis) {	
		ScheduledFuture<?> out = executor.scheduleAtFixedRate( () -> task.onExecute() , initDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
		task.onScheduled();
		
		return out;
	}
	
	/**Postpones a OneTimeTask for delayMillis. After the assigned delay, the task will be executed once.
	 * 
	 * @param oneTimeTask The task to be executed
	 * @param delayMillis How long we wait until we execute this task
	 */
	ScheduledFuture<?> scheduleOneTimeTask(OneTimeTask oneTimeTask, int delayMillis) {		
		return executor.schedule( () -> oneTimeTask.onExecute() , delayMillis, TimeUnit.MILLISECONDS);
	}
	//***********************************************************************************************
	//											TERMINATION
	//***********************************************************************************************
	public void onProgramExit() {		
		Thread thread = new Thread( () -> {
			try {
				System.out.println();
				System.out.println("\tThere are currently " + executor.getQueue().size()+ " task scheduled.\n"
								 + "\tThere are currently " + executor.getActiveCount() + " tasks executing.\n"
						 		 + "\tAttempting shutdown. Please allow up to a minute...");
				
				executor.shutdown(); 
				if( executor.awaitTermination(60, TimeUnit.SECONDS) ) {
					return;
				}
			
				System.out.println();
				System.out.println("\tThere are still " + executor.getActiveCount() + " tasks executing.\n"
								 + "\tForcing shutdown...");
				executor.shutdownNow();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} );		
		thread.setDaemon( false );
		thread.start();
	}
}
