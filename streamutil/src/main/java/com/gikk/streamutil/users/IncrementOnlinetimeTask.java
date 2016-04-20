package com.gikk.streamutil.users;

import com.gikk.streamutil.task.RepeatedTask;

/**This task created by the UserManager and scheduled to fire once every minute. When it fires, it requests that
 * all users curretly listed as online increments their time online by 1 minute.
 * 
 * @author Simon
 *
 */
public class IncrementOnlinetimeTask extends RepeatedTask{
	@Override
	public void onExecute() {
		UserManager.get().incrementUsersOnlinetime(1);	//Add one minute to all users that are online
	}
}
