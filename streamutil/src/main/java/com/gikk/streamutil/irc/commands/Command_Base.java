package com.gikk.streamutil.irc.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.gikk.streamutil.irc.IrcListener;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;

public abstract class Command_Base implements IrcListener{
	public enum CommandType{ PREFIX_COMMAND, CONTENT_COMMAND };
	
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private Set<String> commandPattern;
	private CommandType type;
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	/**Base class for simpler chat commands. Simple chat commands perform a certain
	 * action whenever a certain pattern of characters are seen. 
	 * 
	 * @param leadingCommand {@code True} means that the pattern must be in the beginning of the chat line. 
	 * {@code False} means that the command can be found anywhere in the message
	 */
	protected Command_Base(CommandType type){
		commandPattern = compile();
		this.type = type;
	}
	
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	@Override
	public final void onPrivMsg(IrcUser sender, IrcMessage message) {
		/* This could've been done with REGEX matching, instead of using startsWith()/contains().
		 * 
		 * This is much simpler though and easier to understand and maintain. Also, since the 
		 * amount of work requirered is so small, the gain from using a Matcher is probably 
		 * close to zero
		 * 
		 * We get the command by stripping everything but the first word away.
		 */
		String content = message.getContent();
		String trim = content.trim();
		String[] split = trim.split("\\s");
		String command = split[0].toLowerCase(Locale.ENGLISH);
		
		if( type == CommandType.PREFIX_COMMAND ){
			for( String pattern : commandPattern ){
				if( command.startsWith(pattern) ){
					performCommand(command, sender, message);
					break;	//We don't want to fire twice for the same message
				}
			}
		}
		else {
			for( String pattern : commandPattern ) {
				if( command.contains(pattern) ){
					performCommand(command, sender, message);
					break; //We don't want to fire twice for the same message
				}
			}
		}
	}
	
	//***********************************************************************************************
	//											ABSTRACT
	//***********************************************************************************************
	/**This method must return the words this command should react to. If the command
	 * listen to several words, they should be separated by {@code |} signs. <br><br>
	 * <b>Examlpe:</b> {@code !stats|!stat|?stats|?stat} will react to those chat lines
	 * that starts with any of those 4 strings
	 * <br><br>
	 * 
	 * @return A string, comprising of all words this command reacts to
	 */
	protected abstract String getCommandWords();
	
	/**
	 * This method is the commands execution. This will be called whenever a chat line
	 * is seen that matches the commandPattern
	 * @param command TODO
	 */
	protected abstract void performCommand(String command, IrcUser sender, IrcMessage message);
	
	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************
	private Set<String> compile(){
		return new HashSet<>( 
				Arrays.stream( getCommandWords().toLowerCase(Locale.ENGLISH).split("\\|") )
					  		   .collect( Collectors.toSet()) );
	}
}
