package com.gikk.streamutil.irc;

public class IrcMessage {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	int senderLenght = -1, commandLength = -1, targetLenght = -1, contenctLenght = -1;
	String sender = "", command = "", target = "", content = "";
	
	//***********************************************************
	// 				STATIC
	//***********************************************************

	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************	
	public IrcMessage(String line){
		String[] parts = line.split(" ", 4);
		
		if( parts.length == 4) {
			content = parts[3];
			contenctLenght = parts[3].length();
		}
		if( parts.length >= 3) {
			target = parts[2];
			targetLenght = parts[2].length();
		}
		if( parts.length >= 2) {
			command = parts[1];
			commandLength = parts[1].length();
		}
		if( parts.length >= 1) {
			sender = parts[0];
			senderLenght = parts[0].length();
		}
	}
	
	//***********************************************************
	// 				PUBLIC
	//***********************************************************	
	public String getNick(){
		return sender.substring( (sender.charAt(0) == ':') ? 1 : 0, sender.indexOf("!") );
	}
	public String getName(){
		return sender.substring( sender.indexOf("!"+1), sender.indexOf("@") ) ;
	}
	public String getHost(){
		return sender.substring( sender.indexOf("@"+1), sender.length() );
	}
	
	//***********************************************************
	// 				PRIVATE
	//***********************************************************	
}
