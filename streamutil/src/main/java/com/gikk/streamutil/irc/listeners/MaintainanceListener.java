package com.gikk.streamutil.irc.listeners;

import com.gikk.streamutil.irc.IrcListener;
import com.gikk.streamutil.irc.IrcMessage;
import com.gikk.streamutil.irc.IrcUser;
import com.gikk.streamutil.users.UserDatabaseCommunicator;
import com.gikk.streamutil.users.UsersOnlineTracker;

public class MaintainanceListener implements IrcListener{
	private final UserDatabaseCommunicator userDatabaseCommunicator;
	private final UsersOnlineTracker userOnlineTracker;
	
	public MaintainanceListener(UserDatabaseCommunicator uDBc, UsersOnlineTracker uot) {
		this.userDatabaseCommunicator = uDBc;
		this.userOnlineTracker = uot;
	}
	
	@Override
	public void onAnything(IrcMessage message) {
		System.out.println("IN  " + message.getLine());
	}
	
	@Override
	public void onPrivMsg(IrcUser sender, IrcMessage message) {
		userDatabaseCommunicator.incrementWrittenRows( sender.getNick() , 1);
	}
	
	@Override
	public void onJoin(IrcUser user) {
		userOnlineTracker.joinUser( user.getNick() );		
	}
	
	@Override
	public void onPart(IrcUser user) {
		userOnlineTracker.partUser( user.getNick() );	
	}
}
