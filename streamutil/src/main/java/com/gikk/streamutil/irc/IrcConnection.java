package com.gikk.streamutil.irc;

import java.io.*;
import java.net.*;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**Had to write my own IRC connection class since there were no libraries that were up to date and applicable with
 * Appache v2 license.
 * 
 * This class handles all the sending and receiving of messages from an IRC server. The class is quite bare bone, and
 * doesn't handle all different types of messages, since Twitch only deals with PRIVMSG, JOIN and PART. More modes will be
 * added as needed, or if this becomes a project of its own in the future.
 * 
 * Code inspired by http://archive.oreilly.com/pub/h/1966#code
 * 
 * @author Simon
 *
 */
class IrcConnection {	
	private final String server;
	private final String nick;
	private final String pass;
	private final String channel;
	private final int port;
	
	private final OutputThread outThread;
	private final InputThread inThread;
	
	private Socket socket = null;
	private BufferedWriter writer = null;
	private BufferedReader reader = null;

    public IrcConnection(File file) {
    	PropertiesConfiguration prop = new PropertiesConfiguration();
    	prop.setDelimiterParsingDisabled(true);
    	try {
			prop.load(file);
		} catch (ConfigurationException e1) {
			System.err.println("Could not lode the properties file! Make sure you have a valid 'gikk.ini' in your User/ folder");
		}
    	
        // Fecth settings from our properties file
        server 	= prop.getString("Server");
        nick 	= prop.getString("Nick");
        pass 	= prop.getString("Password");
        channel = prop.getString("Channel");
        port 	= prop.getInt("Port"); 
        
        
        try{
        	socket = new Socket(server, port);
        	socket.setSoTimeout(60 * 1000); //Set a timeout for connection to 30 seconds
        } catch (Exception e){
        	e.printStackTrace();
        }
		try {
			writer = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream( )));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			reader = new BufferedReader( new InputStreamReader(socket.getInputStream( )));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.outThread = new OutputThread(this, reader, writer);
		this.inThread  = new InputThread(this, reader, writer);
    }
    
	public void serverMessage(String message) {
		StaticMethods.sendLine(message, writer);		
	}
	
	public String getChannel() {
		return channel;
	}
        
    /**Connects to the Twitch server and joins the appropriate channel.
     * 
     * @return True if both connections succeeded
     */
    public void connect(){
    	doConnect();
    	doJoin();
    	
    	inThread.start();
    	outThread.start();
    	
    	StaticMethods.sendLine("PRIVMSG " + channel + " Hello!", writer);
    }
    
	public void closeConnection() {
		outThread.end();
		if( outThread.isAlive() )
			outThread.interrupt();
		
		inThread.end();
		if( inThread.isAlive())
			inThread.interrupt();

		try { socket.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { reader.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
		try { writer.close(); } 
		catch (IOException e) { e.printStackTrace(); }
		
	}
	
	private boolean doConnect(){
		// Log on to the server.
        try {
			writer.write("PASS " + pass + "\r\n");
	        writer.write("NICK " + nick + "\r\n");
	        writer.write("USER " + nick + " 8 * : GikkBot\r\n");
	        writer.flush( );
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
        // Read lines from the server until it tells us we have connected.
        String line = null;
        try {
			while ((line = reader.readLine()) != null) {
				System.out.println("IN  " + line);
			    if (line.indexOf("004") >= 0) {
			        return true;
			    }
			    else if (line.indexOf("433") >= 0) {
			        System.out.println("Nickname is already in use.");
			        return false;
			    } 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return false;
	}
	
    private boolean doJoin(){
        // Join the channel.
        try {
			writer.write("JOIN " + channel + "\r\n");
			writer.flush( );
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}       
    }



}
