package com.gikk.streamutil.irc.commands;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import com.gikk.streamutil.GikkBot;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;
import com.gikk.streamutil.users.ObservableUser;
import com.gikk.streamutil.users.UserDatabaseCommunicator;

/**Lets user check online time for themselves, another user or the to pX online times.<br><br>
 * 
 * This command has three different syntaxes: <ul>
 * <li>!time
 * <li>!time [user]
 * <li>!time [number] (Range: 1 - 10 )
 * </ul> 
 * The first syntax will check the senders online time. The second syntax checks [user]'s online time.
 * The third syntax checks the top [numbers] users online time. 
 * 
 * @author Simon
 *
 */
public class Command_Time extends Command_Base{
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private final String pattern = "!time";
	private final UserDatabaseCommunicator uDBc;
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	public Command_Time(UserDatabaseCommunicator uDBc) {
		super(CommandType.PREFIX_COMMAND);
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
		
		if( NumberUtils.isNumber(target) ){
			sendTopTimeUsers(target);
		} 
		else {
			sendUserTime(target);
		}
		
	}
	
	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************
	private void sendTopTimeUsers(String amount){
		int target = Integer.parseInt(amount);
		if( target < 1 || target > 10 ){
			GikkBot.GET().channelMessage("Illigal argument " + target + " for command !time. Must be at least 1 and at most 10");
			return;
		}	
		
		List<ObservableUser> oUsers = uDBc.getTopTimeUsers( target );
		String out = "Time online Top" + amount +" users: ";		
		for( int i = 1; i <= oUsers.size(); i++ ){
			out += "[" + i + "] " + oUsers.get(i-1).getUserName() +" : " + oUsers.get(i-1).getTimeOnlineFormated() +" | ";
		}	
		GikkBot.GET().channelMessage(out);
	}
	
	private void sendUserTime(String user){
		ObservableUser oUser = uDBc.getUser(user);
		
		if( oUser == null ){
			GikkBot.GET().channelMessage("Illigal argument " + user + " for command !time. User not found");
			return;
		}
		
		GikkBot.GET().channelMessage( "Time online for " + oUser.getUserName() + ":" + oUser.getTimeOnlineFormated() );
	}
}
