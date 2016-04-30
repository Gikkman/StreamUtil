package com.gikk.streamutil.irc.commands;

import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;

/**Class that contains a lot of helper methods, that are used again and again in different commands
 * 
 * @author Simon
 *
 */
public final class CommandHelperMethods {
	//***********************************************************
	// 				STATIC
	//***********************************************************
	/**Fetches the first word after a certain command. Useful for PREFIX
	 * commands that has one argument.<br><br>
	 * 
	 * <b>Example:</b> '!stats Simon 2000' <br>
	 * Would return: 'Simon'
	 * 
	 * @param message The IrcMessage from which you want to retrieve the argument
	 * @return The second word, or {@code null} if there is no second word
	 */
	public static String getCommandArgument(IrcMessage message){
		String content = message.getContent();
		if( content.trim().indexOf(" ") == -1 )
			return null;
		else
			return content.split(" ", 2)[1];	
	}
	
	/**Fetches the first word after a certain command. Useful for PREFIX
	 * commands that has one argument. If there is no second word, the user
	 * who sent the message's nick will be returned<br><br>
	 * 
	 * <b>Example:</b> '!stats Simon 2000' <br>
	 * Would return: 'Simon'<br>
	 * 
	 *  <b>Example:</b> '!stats' <br>
	 * Would return: {@code sender.getNick()}'<br><br>
	 * 
	 * @param sender The user who sent this IRC message
	 * @param message The IrcMessage from which you want to retrieve the argument
	 * @return The second word, or {@code null} if there is no second word
	 */
	public static String getCommandArgumentOrSender(IrcUser sender, IrcMessage message){
		String argument = getCommandArgument(message);
		if( argument == null )
			return sender.getNick();
		return argument;
	}
}
