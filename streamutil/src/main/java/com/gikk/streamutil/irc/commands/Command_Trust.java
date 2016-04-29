package com.gikk.streamutil.irc.commands;

import com.gikk.streamutil.GikkBot;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;
import com.gikk.streamutil.users.ObservableUser;
import com.gikk.streamutil.users.UserDatabaseCommunicator;
import com.gikk.streamutil.users.UserStatus;

/**Command for setting the TRUSTED flag for users.<br><br>
 * 
 * This command has two different syntaxes: <ul>
 * <li>!trust [user]
 * <li>!untrust [user] 
 * </ul> 
 * The first syntax will set [user]'s TRUSTED flag to {@code true}. The second syntax
 * will set [user]'s TRUSTED flag to {@code false}<br><br>
 * 
 * What it means to be trusted is not defined per say. Some commands might make sure
 * that the caller is trusted or not. That is up to the implementers decision.<br><br>
 * 
 * @author Simon
 *
 */
public class Command_Trust extends Command_Base {
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private final String patternA = "!trust";
	private final String patternB = "!untrust";
	
	private final UserDatabaseCommunicator uDBc;
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	public Command_Trust(UserDatabaseCommunicator uDBc) {
		super(CommandType.PREFIX_COMMAND);
		this.uDBc = uDBc;
	}

	//***********************************************************************************************
	//											PROTECTED
	//***********************************************************************************************
	@Override
	protected String getCommandWords() {
		return patternA + "|" + patternB;
	}

	@Override
	protected void performCommand(String command, IrcUser sender, IrcMessage message) {
		//Check that the sender can actually issue this command
		ObservableUser issuingUser = uDBc.getOrCreate( sender.getNick() );
		if( issuingUser.getStatus() == UserStatus.REGULAR ){	//Only mods and admins may trust people
			return;
		}
		
		//Check that the commands argument is correct
		String target = CommandHelperMethods.getCommandArgument(message);
		if(target == null){
			GikkBot.GET().channelMessage("Cannot perform !trust. Syntax is !trust [user]");
			return;
		}
		
		//Check that the argument user exists
		ObservableUser targetUser = uDBc.getUser(target);
		if( targetUser == null ){
			GikkBot.GET().channelMessage("Cannot perform !trust. User " + target + " not found");
			return;
		}
		
		//Issue the chosen command
		if( message.getContent().startsWith(patternA) )
			doTrust(issuingUser, targetUser );
		else
			doUntrust(issuingUser, targetUser );
		
	}

	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************
	private void doTrust(ObservableUser issuingUser, ObservableUser targetUser) {		
		if( !targetUser.getTrusted() ){
			targetUser.setTrusted(true);
			GikkBot.GET().channelMessage("User " + targetUser.getUserName() + " is now trusted!");
		} 
		else {
			GikkBot.GET().channelMessage("User " + targetUser.getUserName() + " is already trusted");
		}
	}

	private void doUntrust(ObservableUser issuingUser, ObservableUser targetUser) {
		if( targetUser.getTrusted() ){
			targetUser.setTrusted(false);
			GikkBot.GET().channelMessage("User " + targetUser.getUserName() + " is no longer trusted");
		} 
		else {
			GikkBot.GET().channelMessage("User " + targetUser.getUserName() + " is not trusted to begin with");
		}
	}
}
