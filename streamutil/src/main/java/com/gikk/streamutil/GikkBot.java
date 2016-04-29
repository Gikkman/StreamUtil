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
 * Class that allows for easy use of the underlying IRC connection. The IRC bot loads all its setting from
 * a file named 'gikk.ini' located in the user's home directory. <br><br>
 * 
 * The file should contain the following fields:<br>
 * --------------------------------------------------<br><code>
	Nick = BOT_NAME<br>
	Password = oauth:********************** <br>
	<br>
	Server = irc.twitch.tv<br>
	Port = 6667<br>
	<br>
	Channel = #YOUR_STREAM_CHANNEL<br>
	ClientID = YOUR_CLIENT_ID<br></code>
 * --------------------------------------------------<br><br>
 * 
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
	public void channelMessage(String message){
		irc.channelMessage(message);
		//Remember to update the number of line our bot's written :-)
		userDatabaseCommunicator.getOrCreate( irc.getNick() ).addLinesWritten(1);
	}
	
	public void clearChat(){
		irc.priorityChannelMessage(".clear");
	}
	
	public void purgeUser(String user){
		timeoutUser(user, 1);
	}
	
	public void timeoutUser(String user, int time){
		irc.priorityChannelMessage(".timeout " + user + " " + time);
	}
	
	public void serverMessage(String text) {
		irc.serverMessage(text);	
	}
	
	public void addIrcListener(IrcListener listener){
		irc.addIrcListener(listener);	
	}
	
	public String getChannel() {
		return irc.getChannel();
	}
	
	public void removeIrcListener(IrcListener listener){
		irc.removeIrcListener(listener);	
	}
	
	public ObservableList<ObservableUser> getUsersOnlineList(){
		return userOnlineTracker.getUserOnlineList();
	}
	
	public void onProgramExit(){
		irc.dispose();
		userDatabaseCommunicator.onProgramExit();
	}
	
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
		irc.addIrcListener( new Command_Tick() );
		irc.addIrcListener( new Command_Lines(userDatabaseCommunicator) );
	}
}
