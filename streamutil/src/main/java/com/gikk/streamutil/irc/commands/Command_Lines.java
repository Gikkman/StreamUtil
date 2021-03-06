package com.gikk.streamutil.irc.commands;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import com.gikk.gikk_stream_util.db0.gikk_stream_util.users.Users;
import com.gikk.streamutil.GikkBot;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;
import com.gikk.streamutil.users.ObservableUser;
import com.gikk.streamutil.users.UserDatabaseCommunicator;

/**Lets user check lines written for themselves, another user or the top X lines written.<br><br>
 * 
 * This command has four different syntaxes: <ul>
 * <li>!lines
 * <li>!lines [user]
 * <li>!lines [number] (Range: 1 - 10 )
 * <li>!lurker
 * </ul> 
 * The first syntax will check the senders lines written. The second syntax checks [user]'s lines written.
 * The third syntax checks the top [numbers] users lines written.<br> 
 * The forth syntax will find a random user whom have a total online time of 10 or more minutes, 
 * and have written less than 10 lines, and say their name in chat. The user does not have to be online right now
 * 
 * @author Simon
 *
 */
public class Command_Lines extends Command_Base{
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private final String patternA = "!lines";
	private final String pattnerB = "!lurker";
	private final UserDatabaseCommunicator uDBc;
	
	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************	
	public Command_Lines(UserDatabaseCommunicator uDBc) {
		super(CommandType.PREFIX_COMMAND);
		this.uDBc = uDBc;
	}
	
	//***********************************************************
	// 				PROTECTED
	//***********************************************************	
	@Override
	protected String getCommandWords() {
		return patternA+"|"+pattnerB;
	}

	protected void performCommand(String command, IrcUser sender, IrcMessage message) {
		String target = CommandHelperMethods.getCommandArgumentOrSender(sender, message);
		
		if( command.matches(patternA) )
			if( NumberUtils.isNumber(target) ){
				sendTopLinesUsers(target);
			} 
			else {
				sendUserLines(target);
			}
		else
			exposeRandomLurker();
		
	}

	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************
	private void sendTopLinesUsers(String amount){
		int target = Integer.parseInt(amount);
		if( target < 1 || target > 10 ){
			GikkBot.GET().channelMessage("Illigal argument " + target + " for command !lines. Must be at least 1 and at most 10");
			return;
		}	
		
		List<ObservableUser> oUsers = uDBc.getUsersWhere( Users.LINES_WRITTEN.comparator().reversed(), target);
		String out = "Lines written Top" + amount +" users: ";		
		for( int i = 1; i <= oUsers.size(); i++ ){
			out += "[" + i + "] " + oUsers.get(i-1).getUserName() +" : " + oUsers.get(i-1).getLinesWritten() +" | ";
		}	
		GikkBot.GET().channelMessage(out);
	}
	
	private void sendUserLines(String user){
		ObservableUser oUser = uDBc.getUser(user);
		
		if( oUser == null ){
			GikkBot.GET().channelMessage("Illigal argument " + user + " for command !lines. User not found");
			return;
		}
		
		GikkBot.GET().channelMessage( "Lines written for " + oUser.getUserName() + ":" + oUser.getLinesWritten() );
	}
	
	private void exposeRandomLurker() {
		//TODO: Consider this only finding user that are actually online right now...
		ObservableUser oUser = uDBc.getRandomUserWhere( Users.LINES_WRITTEN.lessOrEqual(10).and( Users.TIME_ONLINE.greaterOrEqual(10)));	
		if( oUser != null )
			GikkBot.GET().channelMessage("All eyes on " + oUser.getUserName() + ". Say \"Hi!\" to the lurker!");
		else
			GikkBot.GET().channelMessage("/me sobs in a corner. No lurker to expose...");
	}
}
