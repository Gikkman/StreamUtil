package com.gikk.streamutil.irc.tasks;

import com.gikk.streamutil.GikkBot;
import com.gikk.streamutil.irc.IrcListener;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.task.OneTimeTask;
import com.gikk.streamutil.users.UserDatabaseCommunicator;
import com.gikk.streamutil.users.UserStatus;

/**This task is intended to fetch all mods for a certain channel and add lists them as moderators in the database.<br><br>
 * 
 * Note that this task does not remove users currently enlisted in the database as moderators that aren't moderators 
 * in the channel anymore. This task is only intended to be used in the program initialization process.
 * 
 * @author Simon
 *
 */
public class FetchAdminAndModsTask extends OneTimeTask {
	private final UserDatabaseCommunicator userDatabaseCommunicator;
	private final GikkBot gikkBot;

	private final IrcListener tempListener;
	
	public static void CREATE_AND_SCHEDULE(GikkBot gB, UserDatabaseCommunicator uDBc){
		FetchAdminAndModsTask task = new FetchAdminAndModsTask(gB, uDBc);
		task.schedule(5 * 1000);
	}
	
	private FetchAdminAndModsTask(GikkBot gB, UserDatabaseCommunicator uDBc) {
		this.gikkBot = gB;
		this.userDatabaseCommunicator = uDBc;
		
    	tempListener = new IrcListener() {
    		@Override
    		public void onNotice(IrcMessage message) {
    			if( message.getContent().startsWith(":The moderators of this room are: ")) {
    				//The channel name is equal to the broadcasters Twitch name, so we add that name as admin
	    			String target = message.getTarget();
	    			String admin = target.substring( target.indexOf("#") + 1);	
	    			userDatabaseCommunicator.updateStatus(UserStatus.ADMIN, admin);
    				
	    			//Then we fetch the name of all the moderators of the channel and update those as well
    				String modsText = message.getContent().substring( ":The moderators of this room are: ".length() );
	    			String[] mods = modsText.split(", ");	    			
	    			userDatabaseCommunicator.updateStatus(UserStatus.MODERATOR, mods);
    			}
    		}
		};	
		
		gikkBot.addIrcListener(tempListener);
		
	}
	
	@Override
	public void onExecute() {
		gikkBot.serverMessage("PRIVMSG " + gikkBot.getChannel() + " :.mods");
	}
	
}
