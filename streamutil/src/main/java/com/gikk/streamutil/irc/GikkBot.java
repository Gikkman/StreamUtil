package com.gikk.streamutil.irc;

import java.io.File;
import java.nio.file.Paths;

import com.gikk.streamutil.users.UserManager;


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
 * TODO: Move this loading of the ini file to another class, and make the ini file gloabaly accessible, since it is needed
 * in more than one place
 * 
 * @author Simon
 */
public class GikkBot{
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private static class HOLDER { static final GikkBot INSTANCE = new GikkBot(); };
	enum Capacity {MEMBERS, COMMANDS, TAGS};
	
	private final IrcConnection irc;
	
	
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
		
		//Load the bots settings from a file named "pirc.ini" located in the users Home folder.
		//In case we fail to connect, we retry 
		File file = Paths.get( System.getProperty("user.home"), "gikk.ini" ).toFile();
		irc = new IrcConnection(file);
		
		irc.addIrcListener( new MyIrcListener() );
		
		irc.connect();
		addCapacity(Capacity.MEMBERS);
		addCapacity(Capacity.COMMANDS);
	}
	
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	public void channelMessage(String message){
		irc.channelMessage(message);
		//Remember to update the number of line our bot's written :-)
		UserManager.GET().incrementWrittenRows( irc.getNick() , 1);
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
	
	private class MyIrcListener implements IrcListener {
		@Override
		public void onAnything(IrcMessage message) {
			System.out.println("IN  " + message.getLine());
		}			
		@Override
		public void onWhisper(IrcUser user, IrcMessage message) {
			System.out.println("\t" + user.getNick() +" thinks I'm special <3");	
		}			
		@Override
		public void onPrivMsg(IrcUser user, IrcMessage message) {
			UserManager.GET().incrementWrittenRows( user.getNick() , 1);
		}			
		@Override
		public void onPart(IrcUser user) {
			UserManager.GET().partUser( user.getNick() );
			
		}			
		@Override
		public void onJoin(IrcUser user) {
			UserManager.GET().joinUser( user.getNick() );
			
		}
	}
}
