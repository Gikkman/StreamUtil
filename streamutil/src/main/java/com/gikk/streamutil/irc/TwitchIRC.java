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
	
	private OutputThread outThread;
	private InputThread inThread;
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
			System.err.println("If no valid properties file is found, the program will not work.\n"
							 + "Concider performing the initialization"
							 + "process again.");
		}
    	
        // Fecth settings from our properties file
        server 	= prop.getString("server");
        nick 	= prop.getString("nick");
        pass 	= prop.getString("password");
        channel = prop.getString("channel");
        port 	= prop.getInt("port"); 
        
		this.queue 	   = new OutputQueue();
    }
    
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************
	/**Sends a message directly to the server. This method should be used very sparsely.
	 * 
	 * @param message The message that should be sent
	 */
    public void serverMessage(String message) {
		outThread.quickSend(message);
	}
	
	/**Enqueues a message at the end of the message queue.
	 * 
	 * @param message The message that should be sent
	 */
	public void channelMessage(String message){
		queue.add("PRIVMSG " + getChannel() + " :" + message);
	}
	
	/**Enqueues a message at the front of the message queue. The message will be sent as soon as possible.
	 * 
	 * @param message The message that should be sent
	 */
	public void priorityChannelMessage(String message){
		queue.addFirst("PRIVMSG " + getChannel() + " :" + message);
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
	
	/**Adds a specific listener to the list of active listeners
	 * 
	 * @param listener Listener to be added
	 */
	public void addIrcListener(IrcListener listener){
		synchronized (listeners) {			
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
			return this.listeners.remove(listener);
		}
	}
        
    /**Connects to the Twitch server and joins the appropriate channel.
     * 
     * @return {@code true} if connection was successful
     * @throws IOException In case the BufferedReader or BufferedWriter throws an error during connection. Might be due to timeout, socket closing or something else
     */
    public boolean connect() throws IOException{
    	if( isConnected ){
	    	System.err.println("\tAlready connected to a server!");
	    	return false;
    	}    	
    	
		createResources();
    	
    	isConnected = doConnect();
    	if( isConnected ){
    		isJoined    = doJoin();
    	} 
    	if( isJoined ){
    		inThread.start();
    		outThread.start();   	
    		
    		return true;
    	}  	
    	return false;
    }

	/**Closes the connection to the IrcServer, leaves all channels, terminates the input- and output thread and 
     * frees all resources. <br><br>
     * 
     * It is safe to call this method even if connections are already closed.<br><br>
     * 
     * This method is different from {@code dispose()} in that it calls the {@code onDisconnect()} method
     * of all the listeners. A listener may thus attempt to reconnect 
     */
	public void disconnect() {
		//Since several sources can call this method on program shutdown, we avoid entering it again if 
		//we've already disconnected
		if( !isConnected )
			return;
		
		isConnected = false;
		isJoined    = false;
		
		System.out.println("\n\tDisconnecting from IRC...");
		releaseResources();		
		System.out.println("\tDisconnected from IRC\n");
		
		for( IrcListener l : listeners )
			l.onDisconnect();	
	}
	
	/**Closes the connection to the IrcServer, leaves all channels, terminates the input- and output thread and 
     * frees all resources. <br><br>
     * 
     * It is safe to call this method even if connections are already closed.<br><br>
     * 
     * This method is different from {@code dispose()} in that it <b>does not</b> call the {@code onDisconnect()} method
     * of any of the listeners.
     */
	public void dispose(){
		isConnected = false;
		isJoined    = false;
		
		System.out.println("\n\tDisposing of IRC...");
		releaseResources();		
		System.out.println("\tDisposing of IRC completed\n");
	}


	//***********************************************************************************************
	//										PRIVATE and PACKAGE
	//***********************************************************************************************	
	private void createResources(){
        try{
        	socket = new Socket(server, port);
        	socket.setSoTimeout(120 * 1000); //Set a timeout for connection to 120 seconds
        } catch (Exception e){
        	e.printStackTrace();
        }
		try {
			writer = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			reader = new BufferedReader( new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.outThread = new OutputThread(this, queue, reader, writer);
		this.inThread  = new InputThread(this, reader, writer);
	}
	
	private void releaseResources(){
		
		outThread.end();
		if( outThread.isAlive() )
			outThread.interrupt();
		
		inThread.end();
		if( inThread.isAlive())
			inThread.interrupt();
		
		try { socket.close(); } 
		catch (IOException e) {  }
		
		try { reader.close(); } 
		catch (IOException e) {  }
		
		try { writer.close(); } 
		catch (IOException e) {  }
	}
	
	
	private boolean doConnect() throws IOException{
		// Log on to the server.
		writer.write("PASS " + pass + "\r\n");
        writer.write("NICK " + nick + "\r\n");
        writer.write("USER " + nick + " 8 * : GikkBot\r\n");
        writer.flush( );
        
        
        // Read lines from the server until it tells us we have connected.
        String line = null;
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
		    else if (line.indexOf("Error logging in") >= 0) {
		    	return false;
		    }
		}
		//We hit this return if the reader timed out, or we reached the end of stream
        return false;
	}
	
    private boolean doJoin() throws IOException{
        // Join the channel.
		writer.write("JOIN " + channel + "\r\n");
		writer.flush( );
		return true;
    
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
    		serverMessage("PONG " + message.getCommand() );
    		
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
			
			if( message.getCommand().matches("MODE") ){
				for(IrcListener l : listeners )
					l.onMode(message);
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
