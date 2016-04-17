package com.gikk.streamutil.irc;

import java.io.File;
import java.nio.file.Paths;


/**Class that allows for easy use of the underlying IRC connection. The IRC bot loads all its setting from
 * a file named 'gikk.ini' located in the user's home directory. <br><br>
 * 
 * The file should contain the following fields:<br>
 * --------------------------------------------------<br>
	Nick = BOT_NAME<br>
	Password = oauth:********************** (Get your bot's oauth at https://twitchapps.com/tmi/) <br>
	<br>
	Server = irc.twitch.tv<br>
	Port = 6667<br>
	<br>
	Channel = #YOUR_STREAM_CHANNEL<br>
	ClientID = YOUR_CLIENT_ID
 * --------------------------------------------------
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
	enum Capacity {MEMBERS, COMMANDS, TAGS};
	
	private final IrcConnection irc;
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	public GikkBot() {
		
		//Load the bots settings from a file named "pirc.ini" located in the users Home folder.
		//In case we fail to connect, we retry 

		File file = Paths.get( System.getProperty("user.home"), "gikk.ini" ).toFile();
		irc = new IrcConnection(file);
		irc.connect();

		addCapacity(Capacity.MEMBERS);
		addCapacity(Capacity.COMMANDS);
		
				
	}
	
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	public void channelMessage(String message){
		irc.channelMessage(message);
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
}
