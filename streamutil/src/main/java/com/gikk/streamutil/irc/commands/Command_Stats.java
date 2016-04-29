package com.gikk.streamutil.irc.commands;

import com.gikk.streamutil.GikkBot;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;
import com.gikk.streamutil.users.ObservableUser;
import com.gikk.streamutil.users.UserDatabaseCommunicator;

/**Chat command: !stats<br><br>
 * 
 * This command has two different syntaxes: <ul>
 * <li>!stats
 * <li>!stats [user]
 * </ul> 
 * The first version meens that the sending user is the target of the command. In the second
 * version, the user denoted by [user] is the target.<br><br>
 * 
 * The response from this command is similar to:<br>
 * UserName [STATUS] [FOLLOWER] [TRUSTED] TimeOnline LinesWritten
 * 
 * <br>
 * @author Simon
 *
 */
public class Command_Stats extends Command_Base{
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private final UserDatabaseCommunicator uDBc;
	private final String pattern = "!stats|!stat";
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	public Command_Stats(UserDatabaseCommunicator uDBc){
		super( CommandType.PREFIX_COMMAND );
		this.uDBc = uDBc;
	}

	//***********************************************************************************************
	//											PROTECTED
	//***********************************************************************************************
	@Override
	protected String getCommandWords() {
		return pattern;
	}

	@Override
	protected void performCommand(String command, IrcUser sender, IrcMessage message) {
		String target = CommandHelperMethods.getCommandArgumentOrSender(sender, message);		
		ObservableUser user = uDBc.getUser(target);
		if( user == null )
			GikkBot.GET().channelMessage("User " + target + " unknown. Check your spelling");
		else
			GikkBot.GET().channelMessage( user.toString() );
	}
}
