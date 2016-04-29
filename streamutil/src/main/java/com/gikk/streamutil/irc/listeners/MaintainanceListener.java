package com.gikk.streamutil.irc.listeners;

import java.io.IOException;

import com.gikk.streamutil.irc.IrcListener;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;
import com.gikk.streamutil.irc.TwitchIRC;
import com.gikk.streamutil.users.ObservableUser;
import com.gikk.streamutil.users.UserDatabaseCommunicator;
import com.gikk.streamutil.users.UserStatus;
import com.gikk.streamutil.users.UsersOnlineTracker;

/**Convenience class for handling basic work, such as JOIN/PART, incrementing sent messages and reconnecting.
 * 
 * @author Simon
 *
 */
public class MaintainanceListener implements IrcListener{
	private final UserDatabaseCommunicator userDatabaseCommunicator;
	private final UsersOnlineTracker userOnlineTracker;
	private final TwitchIRC irc;
	
	public MaintainanceListener(UserDatabaseCommunicator uDBc, UsersOnlineTracker uot, TwitchIRC irc) {
		this.userDatabaseCommunicator = uDBc;
		this.userOnlineTracker = uot;
		this.irc = irc;
	}
	
	@Override
	public void onAnything(IrcMessage message) {
		System.out.println("IN  " + message.getLine());
	}
	
	@Override
	public void onPrivMsg(IrcUser sender, IrcMessage message) {
		userDatabaseCommunicator.getOrCreate( sender.getNick() ).addLinesWritten(1);
	}
	
	@Override
	public void onJoin(IrcUser user) {
		userOnlineTracker.joinUser( user.getNick() );		
	}
	
	@Override
	public void onPart(IrcUser user) {
		userOnlineTracker.partUser( user.getNick() );	
	}	
	
	@Override
	public void onMode(IrcMessage message) {
		String content = message.getContent();
		if( content.startsWith("+o") || content.startsWith("-o") ){
			
			String user = content.substring( 3 ); //Remove the +o_ or -o_	
			ObservableUser oUser = userDatabaseCommunicator.getOrCreate(user);
			
			if( content.startsWith("+o") && oUser.getStatus() == UserStatus.REGULAR ){
				oUser.setStatus(UserStatus.MODERATOR);
			}
			if( content.startsWith("-o") && oUser.getStatus()== UserStatus.MODERATOR ){
				oUser.setStatus(UserStatus.REGULAR);
			}
		}
	}
	
	@Override
	public void onDisconnect() {
		userOnlineTracker.clear();
		try {
			irc.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
