package com.gikk.streamutil.irc;

import java.io.BufferedWriter;
import java.io.IOException;

/**A place to store static methods that doesn't really fit anywhere at the moment. This design is probably up
 * for refactoring in the future.
 * 
 * @author Simon
 *
 */
class StaticMethods {

	/**Sends a message AS IS, without modyfying it in any way. Users of this method are responsible for
	 * formating the string correctly: 
	 * <br>
	 * That means, who ever uses this method has to manually assign channel data and the similar to the
	 * message.
	 * 
	 * @param message
	 */
	public static void sendLine(String message, BufferedWriter writer){
		/**An IRC message may not be longer than 512 characters. Also, they must end with \r\n,
		 * so if the supplied message is longer than 510 characters, we split the message into
		 * chunks of 510 characters.
		 * 
		 * This recursive method will work, since it will continue splitting messages as long
		 * as they are too long. 
		 */
		if( message.length() > 510 ){
			sendLine(message.substring(0, 511), writer );
			sendLine(message.substring(511, message.length() ), writer );
			return;
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
