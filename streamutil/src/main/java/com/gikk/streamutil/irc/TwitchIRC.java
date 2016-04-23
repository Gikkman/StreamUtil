package com.gikk.streamutil.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

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
public class TwitchIRC {	
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private final String server;
	private final String nick;
	private final String pass;
	private final String channel;
	private final int port;
	
	private final OutputThread outThread;
	private final InputThread inThread;
	private final OutputQueue queue;
	
	private final ArrayList<IrcListener> listeners = new ArrayList<>();
	
	private boolean isConnected = false;
	private boolean isJoined    = false;
	private Socket socket = null;
	private BufferedWriter writer = null;
	private BufferedReader reader = null;
	
	private String serverName = "";

	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
    public TwitchIRC(File propertiesFile) {
    	PropertiesConfiguration prop = new PropertiesConfiguration();
    	prop.setDelimiterParsingDisabled(true);
    	try {
			prop.load(propertiesFile);
		} catch (ConfigurationException e1) {
			//TODO: Handle this error better
			System.err.println("Could not lode the properties file! Make sure you have a valid 'gikk.ini' in your Home/User/ folder");
			System.exit(-1);
		}
    	
        // Fecth settings from our properties file
        server 	= prop.getString("Server");
        nick 	= prop.getString("Nick");
        pass 	= prop.getString("Password");
        channel = prop.getString("Channel");
        port 	= prop.getInt("Port"); 
        
        try{
        	socket = new Socket(server, port);
        	socket.setSoTimeout(120 * 1000); //Set a timeout for connection to 120 seconds
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
		
		this.queue 	   = new OutputQueue();
		this.outThread = new OutputThread(this, queue, reader, writer);
		this.inThread  = new InputThread(this, reader, writer);	
    }
    
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	/**Sends a message directly to the server. This method should be used very sparsely.
	 * 
	 * @param message
	 */
    public void serverMessage(String message) {
		outThread.quickSend(message);
	}
	
	public void channelMessage(String message) {
		outThread.enqueueMessage("PRIVMSG " + getChannel() + " :" + message);
	}
	
	public void priorityChannelMessage(String message) {
		outThread.enqueueMessageFront("PRIVMSG " + getChannel() + " :" + message);		
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public boolean isJoined() {
		return isJoined;
	}

	public String getChannel() {
		return channel;
	}
	
	public String getNick() {
		return nick;
	}
	
	public String getServerName(){
		return serverName;
	}
	
	public void addIrcListener(IrcListener listener){
		synchronized (listeners) {			
			System.out.println("\tAdded listener");
			this.listeners.add(listener);
		}
	}
	
	/**Removes a specific listener from the list of active listeners
	 * 
	 * @param listener Listener to be removed
	 * @return <code>true</code> if the listener was removed
	 */
	public boolean removeIrcListener(IrcListener listener){
		synchronized (listeners) {	
			System.out.println("\tRemoved listener");
			return this.listeners.remove(listener);
		}
	}
        
    /**Connects to the Twitch server and joins the appropriate channel.
     * 
     * @return True if both connections succeeded
     */
    public void connect(){
    	if( isConnected ){
	    	System.err.println("\tAlready connected to a server!");
	    	return;
    	}    		
    	
    	//TODO: This should be handled differently. Consider making this actually throw the errors that might occur.
    	isConnected = doConnect();
    	isJoined    = doJoin();
    	
    	inThread.start();
    	outThread.start();
    	
    	channelMessage("Hello! " + getNick() + " at your service!");
    }

	/**Closes the connection to the IrcServer, leaves all channels, terminates the input- and output thread and 
     * frees all resources.
     * 
     */
	public void closeConnection() {
		//Since several sources can call this method on program shutdown, we avoid entering it again if 
		//we've already disconnected
		if( !isConnected )
			return;
		
		System.out.println("\n\tDisconnecting from IRC...");
		
		isConnected = false;
		isJoined    = false;
		
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
		
		System.out.println("\tAll IRC resources has been disposed of\n");
	}
	
	//***********************************************************************************************
	//										PRIVATE and PACKAGE
	//***********************************************************************************************	
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
				
				if( serverName.isEmpty() )
					serverName = line.substring(0, line.indexOf(" "));
			    if (line.indexOf("004") >= 0) {
			        return true;
			    }
			    else if (line.indexOf("433") >= 0) {
			        System.out.println("Nickname is already in use.");
			        return false;
			    }
			    //TODO: Other error codes
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
    
	void incommingMessage(String line){
		IrcMessage message	= new IrcMessage(line);
		boolean wasPing 	= false;
		
		//PING is a bit strange, so we need to handle it separately. And also, we want to respond to a ping
		//before we do anything else.
		if (message.getPrefix().equalsIgnoreCase("PING") ||  message.getCommand().equalsIgnoreCase("PING")) {

    		// A PING contains the message "PING MESSAGE", and we want to reply with MESSAGE as well
    		// Hence, we reply "PONG MESSAGE" . That's where the substring(5) comes from bellow, we strip
    		//out everything but the message
    		serverMessage("PONG :" + message.getCommand() );
    		channelMessage("We got pinged!");
    		
    		wasPing = true;
		}
		
		synchronized (listeners) {
			//Call all the appropriate listeners for the given message.
			//
			//First, we call all onAnything messages
			for(IrcListener l : listeners )
				l.onAnything(message);
			
			//If this was a ping, we don't have to do anything more
			if( wasPing )
				return;
			
			if( message.getCommand().matches("[0-9]+") ){
				//TODO: Implement different codes
				return;
			}
			
			if( message.getCommand().matches("NOTICE") ){
				for(IrcListener l : listeners )
					l.onNotice(message);
				return;
			}
			
			//Check for message types that are sent by a user
			//TODO: Implement other message types. These are only the ones used by Twitch
			IrcUser user = IrcUser.create( message );
			if( user != null)
				switch( message.getCommand().toUpperCase() ) {
				case "PRIVMSG":
					for(IrcListener l : listeners )
						l.onPrivMsg(user, message);
					return;
					
				case "WHISPER":
					for(IrcListener l : listeners )
						l.onWhisper(user, message);
					return;
					
				case "JOIN":
					for(IrcListener l : listeners )
						l.onJoin( user );
					return;
					
				case "PART":
					for(IrcListener l : listeners )
						l.onPart( user );
					return;		
					
				default:
				}
			
			//If we've gotten all the way down here, we don't know this message's type
			for(IrcListener l : listeners )
				l.onUnknown(message);
		}
	}
}
