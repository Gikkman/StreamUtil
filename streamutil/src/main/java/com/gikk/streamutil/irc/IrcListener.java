package com.gikk.streamutil.irc;

public interface IrcListener {	
	
	public default void onAnything( IrcMessage message ) {}
	
	public default void onPrivMsg( IrcUser sender, IrcMessage message ) {}
	
	public default void onWhisper( IrcUser sender, IrcMessage message ) {}
	
	public default void onJoin( IrcUser user ) {}
	
	public default void onPart( IrcUser user ) {}
	
	public default void onNotice( IrcMessage message ) {}
	
	public default void onUnknown( IrcMessage message ) {}
}
