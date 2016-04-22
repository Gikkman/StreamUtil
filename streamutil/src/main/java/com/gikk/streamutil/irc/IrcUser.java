package com.gikk.streamutil.irc;

/**Convenience class for handling IrcUsers. Easier than sending a bunch of strings everywhere.
 * 
 * @author Simon
 *
 */
public class IrcUser {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private String nick, name, host;
	
	//***********************************************************
	// 				STATIC
	//***********************************************************
	/**Creates a new IrcUser from the IrcMessage, if possible. If the message is of
	 * type PRIVMSG, JOIN, PART, or WHISPER, it should be possible to create a user
	 * 
	 * @param message
	 * @return An IrcUser if possible, or <code>null</code> if not
	 */
	static IrcUser create(IrcMessage message) {
		/* A IrcPrefix indicating that this was sent from a user is constructed like this:
		 * (:)nickName!realName@host		(The : is optional)
		 * 
		 * So we check if this can be made into a IrcUser or not and if so, we construct
		 * a IrcUser and return it.
		 */
		String prefix = message.getPrefix();
		int nickIdx = prefix.indexOf('!');
		int nameIdx = prefix.indexOf('@');
		
		if( nickIdx == -1 || nameIdx == -1)
			return null;

		String nick = prefix.substring( prefix.charAt(0) == ':' ? 1 : 0, nickIdx);
		String name = prefix.substring( nickIdx + 1, nameIdx );
		String host = prefix.substring( nameIdx + 1 );
		
		return new IrcUser(nick, name, host);
	}

	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************	
	private IrcUser(String nick, String name, String host){
		this.nick = nick;
		this.name = name;
		this.host = host;
	}
	
	//***********************************************************
	// 				PUBLIC
	//***********************************************************	
	public String getName(){
		return name.toLowerCase();
	}
	
	public String getNick(){
		return nick.toLowerCase();
	}
	
	public String getHost(){
		return host.toLowerCase();
	}
	
	void changeNick(String newNick){
		this.nick = newNick;
	}	
}
