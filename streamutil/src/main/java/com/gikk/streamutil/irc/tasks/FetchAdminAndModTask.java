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
 *
 * @author Simon
 *
 */
public class FetchAdminAndModTask extends OneTimeTask {
	private final UserDatabaseCommunicator userDatabaseCommunicator;
	private final GikkBot gikkbot;
	
	private final IrcListener thisListener;
	
	/**Creates an instance of this task and schedules it to execute in 10 seconds
	 * 
	 * @param gikkbot The instance of GikkBot. This is needed to avoid recursive calls to GikkBot's constructor
	 * @param uDBc GikkBot's UserDatabaseCommunicator 
	 */
	public static void CREATE_AND_SCHEDULE(GikkBot gikkbot, UserDatabaseCommunicator uDBc){
		FetchAdminAndModTask task = new FetchAdminAndModTask(gikkbot, uDBc);
		task.schedule(10 * 1000); //The bot needs to establish a connection before executing this command, so we give it plenty of time
	}
	
	private FetchAdminAndModTask(GikkBot gikkbot,UserDatabaseCommunicator uDBc) {
		this.userDatabaseCommunicator = uDBc;
		this.gikkbot = gikkbot;
		
    	thisListener = new IrcListener() {
    		@Override
    		public void onNotice(IrcMessage message) {
    			if( message.getContent().startsWith("The moderators of this room are: ")) {
    				//The channel name is equal to the broadcasters Twitch name, so we add that name as admin
	    			String target = message.getTarget();
	    			String admin = target.substring( target.indexOf("#") + 1);	
	    			userDatabaseCommunicator.getOrCreate(admin).setStatus(UserStatus.ADMIN);
	    			
	    			//Then we fetch the name of all the moderators of the channel and update those as well
    				String modsText = message.getContent().substring( "The moderators of this room are: ".length() );
	    			String[] mods = modsText.split(", ");	    			
	    			for( String mod : mods )
	    				if( !mod.matches(admin) ) //Don't wanna demote our admin
	    					userDatabaseCommunicator.getOrCreate(mod).setStatus(UserStatus.MODERATOR);
	    			
	    			//TODO: Would be cool if it actually removed currently enlisted mods that aren't mods anymore
    			}
    		}
		};	
		
		this.gikkbot.addIrcListener(thisListener);
		
	}
	
	@Override
	public void onExecute() {
		this.gikkbot.channelMessage(".mods");
	}
	
}
