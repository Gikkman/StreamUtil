package com.gikk.streamutil;

import java.io.File;

import com.gikk.streamutil.irc.IrcListener;
import com.gikk.streamutil.irc.TwitchIRC;
import com.gikk.streamutil.irc.commands.Command_Lines;
import com.gikk.streamutil.irc.commands.Command_Stats;
import com.gikk.streamutil.irc.commands.Command_Tick;
import com.gikk.streamutil.irc.commands.Command_Time;
import com.gikk.streamutil.irc.commands.Command_Trust;
import com.gikk.streamutil.irc.listeners.CheckFollowListener;
import com.gikk.streamutil.irc.listeners.MaintainanceListener;
import com.gikk.streamutil.irc.tasks.ConnectIrcTask;
import com.gikk.streamutil.irc.tasks.FetchAdminAndModTask;
import com.gikk.streamutil.misc.GikkPreferences;
import com.gikk.streamutil.twitchApi.TwitchApi;
import com.gikk.streamutil.users.ObservableUser;
import com.gikk.streamutil.users.UserDatabaseCommunicator;
import com.gikk.streamutil.users.UsersOnlineTracker;
import com.gikk.streamutil.users.tasks.IncrementOnlinetimeTask;

import javafx.collections.ObservableList;


/**<b>SINGLETON</b><br><br>
 * 
 * Class that allows for easy use of the underlying IRC connection.  
 * 
 * @author Simon
 */
public class GikkBot{
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private static class HOLDER { static final GikkBot INSTANCE = new GikkBot(); };
	
	
	private final TwitchIRC irc;
	private final UserDatabaseCommunicator userDatabaseCommunicator;
	private final UsersOnlineTracker userOnlineTracker;
	
	
	//***********************************************************************************************
	//											STATIC
	//***********************************************************************************************
	public static GikkBot GET(){
		return HOLDER.INSTANCE;
	}
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	private GikkBot() {
		this.userDatabaseCommunicator  = new UserDatabaseCommunicator();
		this.userOnlineTracker = new UsersOnlineTracker(userDatabaseCommunicator);
		
		//This task makes sure that users that are online on every 1 minute mark gets their online time
		//inremented by one minute. 
		//This means that we do not track each users online time individually! Time online will not be 100% accurate
		IncrementOnlinetimeTask task = new IncrementOnlinetimeTask(userOnlineTracker);
		task.schedule(60 * 1000, 60 * 1000);
		
		File file = GikkPreferences.GET().getPropertiesFile();
		irc = new TwitchIRC(file);
		irc.addIrcListener( new MaintainanceListener(userDatabaseCommunicator, userOnlineTracker, irc) );		
		irc.addIrcListener( new CheckFollowListener( userDatabaseCommunicator ));
		
		//Asynchronous tasks. These are executed asynchronously to increase responsiveness in the UI.
		ConnectIrcTask.CREATE_AND_SCHEDULE(irc);
		FetchAdminAndModTask.CREATE_AND_SCHEDULE(this, userDatabaseCommunicator);
		
		addCommands();
		TwitchApi.GET().addOnNewFollowerListener((follower, total, isNew) ->  { if(isNew) channelMessage("New follower: " + follower); } );
	}

	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	/**Sends a message to the IRC channel. <br>
	 * Formating is done behind the scenes. You only need to pass the text you want to
	 * appear on IRC
	 * 
	 * @param message The message you want to post in the IRC channel.
	 */
	public void channelMessage(String message){
		irc.channelMessage(message);
		//Remember to update the number of line our bot's written :-)
		userDatabaseCommunicator.getOrCreate( irc.getNick() ).addLinesWritten(1);
	}
	
	/**Method for retrieving the name of the TwitchIRC channel we are
	 * connected to
	 * 
	 * @return The channel name (with the leading #)
	 */
	public String getChannel() {
		return irc.getChannel();
	}
	
	public void addIrcListener(IrcListener listener){
		irc.addIrcListener(listener);	
	}
	
	public void removeIrcListener(IrcListener listener){
		irc.removeIrcListener(listener);	
	}
	
	/**Method for retrieving the list of users that are online. The list
	 * is observable, so that JavaFX applications can monitor it
	 * 
	 * @return An ObservableList of all users that are online.
	 */
	public ObservableList<ObservableUser> getUsersOnlineList(){
		return userOnlineTracker.getUserOnlineList();
	}
	
	/**Disposes of the Singleton's resources, such as IRC threads. Also flushes the 
	 * database to storage 
	 */
	public void onProgramExit(){
		irc.dispose();
		userDatabaseCommunicator.onProgramExit();
	}
	
	/**This method is only here for testing purposes, so we can modify the database via the GUI.
	 * TODO: Remove
	 * 
	 * @return The UserDatabaseCommunicator
	 */
	public UserDatabaseCommunicator getDB(){
		return userDatabaseCommunicator;
	}
	
	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************

	private void addCommands() {
		irc.addIrcListener( new Command_Stats(userDatabaseCommunicator) );
		irc.addIrcListener( new Command_Time( userDatabaseCommunicator) );
		irc.addIrcListener( new Command_Trust(userDatabaseCommunicator) );
		irc.addIrcListener( new Command_Lines(userDatabaseCommunicator) );
		irc.addIrcListener( new Command_Tick() );
	}
}
