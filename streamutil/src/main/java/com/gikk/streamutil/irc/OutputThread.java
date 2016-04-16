package com.gikk.streamutil.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;

class OutputThread extends Thread{
	private final IrcConnection connection;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	
	private boolean isConnected = true;
	private boolean isActive = true;
	
	public OutputThread(IrcConnection connection, BufferedReader reader, BufferedWriter writer){
		this.connection = connection;
		this.reader = reader;
		this.writer = writer;
		
		this.setName("GikkBot-OutputThread");
	}
	
	@Override
	public void run(){
            
    }

	public void end() {
		//TODO: In case we need it
	}
}
