package com.gikk.streamutil.irc;

public class IrcMessage {
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
	private int prefixLenght = -1, commandLength = -1, targetLenght = -1, contenctLenght = -1;
	private final String line, prefix, command, target, content;
	
	//***********************************************************
	// 				STATIC
	//***********************************************************

	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************	
	public IrcMessage(String line){
		this.line = line;
		String[] parts = line.split(" ", 4);
		
		if( parts.length == 4) {
			content = parts[3];
			contenctLenght = parts[3].length();
		} else {
			content = "";
		}
		if( parts.length >= 3) {
			target = parts[2];
			targetLenght = parts[2].length();
		}else {
			target = "";
		}
		if( parts.length >= 2) {
			command = parts[1];
			commandLength = parts[1].length();
		}else {
			command = "";
		}
		if( parts.length >= 1) {
			prefix = parts[0];
			prefixLenght = parts[0].length();
		}else {
			prefix = "";
		}
	}
	
	//***********************************************************
	// 				PUBLIC
	//***********************************************************	
	public String getLine(){
		return line;
	}
	
	public int getPrefixLenght() {
		return prefixLenght;
	}
	
	public int getCommandLength() {
		return commandLength;
	}

	public int getTargetLenght() {
		return targetLenght;
	}

	public int getContenctLenght() {
		return contenctLenght;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getCommand() {
		return command;
	}

	public String getTarget() {
		return target;
	}

	public String getContent() {
		return content;
	}
	
	//***********************************************************
	// 				PRIVATE
	//***********************************************************	
}
