package com.gikk.streamutil;

import java.io.File;
import java.nio.file.Paths;

import com.gikk.streamutil.irc.IrcListener;
import com.gikk.streamutil.irc.TwitchIRC;
import com.gikk.streamutil.irc.listeners.MaintainanceListener;
import com.gikk.streamutil.irc.tasks.FetchAdminAndModsTask;
import com.gikk.streamutil.users.IncrementOnlinetimeTask;
import com.gikk.streamutil.users.ObservableUser;
import com.gikk.streamutil.users.UserDatabaseCommunicator;
import com.gikk.streamutil.users.UsersOnlineTracker;

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
 * TODO: Move this loading of the ini file to another class, and make the ini file gloabaly accessible, since it is needed in more than one place
 * 
 * @author Simon
 */
public class GikkBot{
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private static class HOLDER { static final GikkBot INSTANCE = new GikkBot(); };
	enum Capacity {MEMBERS, COMMANDS, TAGS};
	
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
		
		//Load the bots settings from a file named "gikk.ini" located in the users Home folder.
		//In case we fail to connect, we retry 
		File file = Paths.get( System.getProperty("user.home"), "gikk.ini" ).toFile();
		irc = new TwitchIRC(file);
		irc.addIrcListener( new MaintainanceListener(userDatabaseCommunicator, userOnlineTracker) );		
		irc.connect();
		addCapacity(Capacity.MEMBERS);
		addCapacity(Capacity.COMMANDS);
		
		FetchAdminAndModsTask.CREATE_AND_SCHEDULE(this, userDatabaseCommunicator);
	}
	
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	public void channelMessage(String message){
		irc.channelMessage(message);
		//Remember to update the number of line our bot's written :-)
		userDatabaseCommunicator.incrementWrittenRows( irc.getNick() , 1);
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
	
	public void closeConnection() {
		irc.closeConnection();
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
		closeConnection();
		userDatabaseCommunicator.onProgramExit();
	}
	
	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************
	private void addCapacity(Capacity capacity){
		switch (capacity) {
		case MEMBERS:
			irc.serverMessage("CAP REQ :twitch.tv/membership");
			break;
		case COMMANDS:
			irc.serverMessage("CAP REQ :twitch.tv/commands");
			break;			
		case TAGS:
			irc.serverMessage("CAP REQ :twitch.tv/tags");
			break;
		}
	}

	
}
