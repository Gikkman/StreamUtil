package com.gikk.streamutil.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**This class handles all outgoing IRC traffic.<br><br>
 * 
 * The implementation is intended to be thread safe and handle all potential errors (<u>keyword: INTENDED</u>). 
 * That means that we can have multiple threads feeding the message queue safely and still operate without any trouble.
 * 
 * @author Simon
 *
 */
class OutputThread extends Thread{
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private final TwitchIRC connection;
	private final BufferedReader reader;
	private final BufferedWriter writer;
	private final OutputQueue queue;
	
	private boolean isConnected = true;
	
	private int MESSAGE_GAP_MILLIS = 1500;	//We may not send more than 20 messages to the Twitch server / 30 seconds
	
	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	public OutputThread(TwitchIRC connection, OutputQueue queue, BufferedReader reader, BufferedWriter writer){
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
        	catch (InterruptedException e) { 
        		/* Being interrupted probably means that we are about to shut down.
        		 * If we are about to close down, isConnected will be set to false so we can just go back
        		 * to the loop and automatically terminate from there.  */ 
    		}
        }
    }
	
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************

	/**Enqueues a message at the end of the message queue.
	 * 
	 * @param message
	 */
	public void enqueueMessage(String message){
		queue.add(message);
	}
	
	/**Enqueues a message at the front of the message queue. The message will be sent as soon as possible.
	 * 
	 * @param message
	 */
	public void enqueueMessageFront(String message){
		queue.addFirst(message);
	}
	
	/**Circumvents the message queue completely and attempts to send the message at once. Should only be used for sending
	 * PING responses.
	 * 
	 * @param message
	 */
	public void quickSend(String message) {
		sendLine(message);		
	}
	
	/**Tells the thread to not start listening for new messages again. However, if the thread is already listening for
	 * messages, this method will not interrupt the thread. That has to be done manually.
	 * 
	 */
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
			System.out.println("OUT " + message);
			synchronized (writer) {		
				writer.write(message + "\r\n");
				writer.flush();
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
