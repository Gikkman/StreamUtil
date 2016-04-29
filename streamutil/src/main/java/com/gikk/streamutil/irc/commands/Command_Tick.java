package com.gikk.streamutil.irc.commands;

import com.gikk.streamutil.GikkBot;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;

public class Command_Tick extends Command_Base{
	private final String pattern = "tick";
	
	public Command_Tick() {
		super(CommandType.CONTENT_COMMAND);
	}
		
	@Override
	protected String getCommandWords() {
		return pattern;
	}

	@Override
	protected void performCommand(String command, IrcUser sender, IrcMessage message) {
		GikkBot.GET().serverMessage("tock");
	}

}
