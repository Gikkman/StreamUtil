package com.gikk.streamutil.irc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;


/**Class that allows for easy use of the underlying IRC connection. The IRC bot loads all its setting from
 * a file named 'pirc.ini' located in the user's home directory. 
 * 
 * The file should contain the following fields:

	Nick = BOT_NAME
	
	Password = oauth:********************** (Get your bot's oauth at https://twitchapps.com/tmi/)
	
	Server = irc.twitch.tv
	Port = 6667
	
	Channel = #YOUR_STREAM_CHANNEL
 * 
 * @author Simon
 */
public class GikkBot{
	enum Capacity {MEMBERS, COMMANDS, TAGS};
	
	private final IrcConnection irc;
	
//	protected final String channel;
	
	public GikkBot() {
		
		//Load the bots settings from a file named "pirc.ini" located in the users Home folder.
		//In case we fail to connect, we retry 

		File file = Paths.get( System.getProperty("user.home"), "gikk.ini" ).toFile();
		irc = new IrcConnection(file);
		irc.connect();

		addCapacity(Capacity.MEMBERS);
		addCapacity(Capacity.COMMANDS);
		//TODO: setMessageDelay(1500);
		
				
	}
	
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

	public void closeConnection() {
		irc.closeConnection();
	}
}
