package com.gikk.streamutil.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

class InputThread extends Thread{
	private final IrcConnection connection;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	
	private boolean isConnected = true;
	private boolean isActive	= true;
	
	public InputThread(IrcConnection connection, BufferedReader reader, BufferedWriter writer){
		this.connection = connection;
		this.reader  = reader;
		this.writer  = writer;
		
		this.setName("GikkBot-InputThread");
	}
	
	@Override
	public void run() {
		
        while (isConnected) {
            try {
                String line = null;
                while ( (line = reader.readLine()) != null ) {
                	//print what we got
                	System.out.println("IN  " + line);
                	
                	if (line.toLowerCase( ).startsWith("ping ")) {
    			        // We must respond to PINGs to avoid being disconnected.
                		StaticMethods.sendLine("PONG " + line.substring(5) + "\r\n", writer);
    			        StaticMethods.sendLine("PRIVMSG " + connection.getChannel() + " :I got pinged!\r\n", writer);
    			    }
                }
                //If we reach this line, it means the line was null. That only happens if the end of the stream's been reached
                isConnected = false;
            }
            catch (SocketTimeoutException e){
            	//If we time out, that means we haven't seen anything from server in a while, so we ping it
            	StaticMethods.sendLine("PING", writer);
            }
            catch (SocketException e) {
				//This means we force closed the socket. Hence, we just let it slide
			}
            catch (IOException e) {
            	//Apparently, something went wrong with our line reading...	
            	e.printStackTrace();
            }
        }
		//If we have been disconnected, we close the connection and clean up the resources held by the IrcConnection
		isActive = false;
		connection.closeConnection();
	}

	public void end() {
		isConnected = false;
		isActive = false;
	}
}
