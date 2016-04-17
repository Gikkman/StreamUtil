package com.gikk.streamutil.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

class OutputThread extends Thread{
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private final IrcConnection connection;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	private final OutputQueue queue;
	
	private boolean isConnected = true;
	
	private int MESSAGE_GAP_MILLIS = 1500;	//We may not send more than 20 messages to the Twitch server / 30 seconds
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	public OutputThread(IrcConnection connection, OutputQueue queue, BufferedReader reader, BufferedWriter writer){
		this.connection = connection;
		this.queue = queue;
		this.reader = reader;
		this.writer = writer;
		
		this.setName("GikkBot-OutputThread");
	}
	
	@Override
	public void run(){
		String line;
        while( isConnected ){
        	line = queue.next();
        	if( line != null ) {
        		sendLine(line);
        	}
        	else {
        		//If we get a null line from the queue, it might mean that the application interrupted the thread
        		// and wants us to shut down.
        		isConnected = connection.isConnected();	
        	}        	
        	try { Thread.sleep(MESSAGE_GAP_MILLIS); } 
        	catch (InterruptedException e) { /* Being interrupted probably means that we are about to shut down */ }
        }
    }
	
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************

	public void enqueueMessage(String message){
		queue.add(message);
	}
	
	public void enqueueMessageFront(String message){
		//TODO: Find a more elegant way than this for sending messages quickly
		queue.addFirst(message);
	}
	

	public void quickSend(String message) {
		sendLine(message);		
	}
	
	public void end() {
		isConnected = false;
	}
	
	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************

	/**Sends a message AS IS, without modyfying it in any way. Users of this method are responsible for
	 * formating the string correctly: 
	 * <br>
	 * That means, who ever uses this method has to manually assign channel data and the similar to the
	 * message.
	 * 
	 * @param message
	 */
	private void sendLine(String message){
		/**An IRC message may not be longer than 512 characters. Also, they must end with \r\n,
		 * so if the supplied message is longer than 510 characters, we have to cut it short.
		 * 
		 * While it might be an alternative to split the message and send it in different batches,
		 * that would mean that we lose potential commands and such. Hence, instead we just drop 
		 * everything beyond the 510th character.
		 */
		if( message.length() > 510 ){
			message = message.substring(0, 511);
		}		
		
		try{
			writer.write(message + "\r\n");
			writer.flush();
			System.out.println("OUT " + message);
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
