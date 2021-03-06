package com.gikk.streamutil.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**Class for handling all incoming IRC traffic (after the initial connection is established). <br><br>
 * 
 * The implementation is intended to be thread safe and handle all potential errors (<u>keyword: INTENDED</u>).
 * 
 * @author Simon
 *
 */
class InputThread extends Thread{
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private final TwitchIRC connection;
	private final BufferedReader reader;
	
	private boolean isConnected = true;
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	public InputThread(TwitchIRC connection, BufferedReader reader, BufferedWriter writer){
		this.connection = connection;
		this.reader  = reader;
		
		this.setName("GikkBot-InputThread");
	}
	
	@Override
	public void run() {		
        while (isConnected) {
            try {
                String line = null;
                while ( (line = reader.readLine()) != null ) {           	
                	try{
	                	connection.incommingMessage(line);
                	} catch (Exception e) {
                		System.err.println("Error in handling the incomming Irc Message");
                		e.printStackTrace();
                	}
                }
                //If we reach this line, it means the line was null. That only happens if the end of the stream's been reached
                isConnected = false;
            }
            catch (SocketTimeoutException e){
            	//If we time out, that means we haven't seen anything from server in a while, so we ping it
            	connection.serverMessage("PING " + System.currentTimeMillis());
            }
            catch (SocketException e) {
            	//This probably means we force closed the socket.
            	if( !(e.getMessage().indexOf("Socket Closed") >= 0) ){
            		e.printStackTrace();
            	}
            	isConnected = false;
			}
            catch (IOException e) {
            	//Apparently, something went wrong with our line reading...	
            	e.printStackTrace();
            }
        }
        
		//If we have been disconnected, we close the connection and clean up the resources held by the IrcConnection.
        //However, if we are disconnected intentionally, we don't need to try to disconnect again
		if( connection.isConnected() )
			connection.disconnect();
	}

	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	public void end() {
		isConnected = false;
	}
	public boolean isConnected(){
		return isConnected;
	}
}
