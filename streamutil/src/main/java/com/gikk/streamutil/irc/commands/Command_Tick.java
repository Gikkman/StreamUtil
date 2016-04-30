package com.gikk.streamutil.irc.commands;

import com.gikk.streamutil.GikkBot;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;

/**Showcase how CONTENT_COMMAND work. If an IRC line contains this commands 
 * pattern, the command will be fired.<br><br>
 * 
 * This particular command responds with "tock" whenever a chat line contains
 * the pattern "tick"
 * 
 * @author Simon
 *
 */
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
		GikkBot.GET().channelMessage("tock");
	}

}
