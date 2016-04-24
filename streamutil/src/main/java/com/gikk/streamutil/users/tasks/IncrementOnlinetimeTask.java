package com.gikk.streamutil.users.tasks;

import com.gikk.streamutil.task.RepeatedTask;
import com.gikk.streamutil.users.UsersOnlineTracker;

/**This task created by the UserManager and scheduled to fire once every minute. When it fires, it requests that
 * all users curretly listed as online increments their time online by 1 minute.
 * 
 * @author Simon
 *
 */
public class IncrementOnlinetimeTask extends RepeatedTask{
	
	private final UsersOnlineTracker usersOnline;
	
	public IncrementOnlinetimeTask(UsersOnlineTracker uo) {
		this.usersOnline = uo;
	}
	
	@Override
	public void onExecute() {
		usersOnline.incrementUsersOnlinetime(1);	//Add one minute to all users that are online
	}
}
