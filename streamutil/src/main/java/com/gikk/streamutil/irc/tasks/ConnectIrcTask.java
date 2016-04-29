package com.gikk.streamutil.irc.tasks;

import java.io.IOException;

import com.gikk.streamutil.irc.TwitchIRC;
import com.gikk.streamutil.task.OneTimeTask;

public class ConnectIrcTask extends OneTimeTask{
	enum Capacity {MEMBERS, COMMANDS, TAGS};
	private final TwitchIRC irc;
	
	public static void CREATE_AND_SCHEDULE(TwitchIRC irc){
		ConnectIrcTask task = new ConnectIrcTask(irc);
		task.schedule(0); //Execute immediately
	}
	
	private ConnectIrcTask(TwitchIRC irc) {
		this.irc = irc;
	}
	
	@Override
	public void onExecute() {
		try {
			irc.connect(); 
		} catch (IOException e) {
			System.err.println("IRC failed to initialize. Check your internet connection, and that nothing uses port 6667");
			e.printStackTrace();
			
			irc.dispose();
			return;
		}
		
		addCapacity(Capacity.MEMBERS);
		addCapacity(Capacity.COMMANDS);
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
	
}
