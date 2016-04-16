package com.gikk.streamutil.irc;

import java.util.LinkedList;

/**We need a specialized messaging queue to be able to handle A) one consumer/multiple producers and 
 * B) being able to put messages to the front and back of the queue.
 * 
 * We also want the next() method to block until there is anything to send to the IRC server in the 
 * queue.
 * 
 * Due to these reasons, we cannot use a normal queue.
 * 
 * @author Simon
 *
 */
class OutputQueue {
	private final LinkedList<String> queue = new LinkedList<>();
	
	/**Adds a message to the back of the output queue
	 * 
	 * @param s The message to add to the queue
	 */
	public void add(String s){
		synchronized (queue) {
			queue.add(s);
			queue.notify();
		}
	}
	
	/**Adds a message to the front of the output queue.
	 * Can be useful for prioritized messages
	 * 
	 * @param s The message to add to the queue
	 */
	public void addFirst(String s){
		synchronized (queue) {
			queue.addFirst(s);
			queue.notify();
		}
	}
	
	/**A <b>blocking</b> call that retrieves the next message from the queue.
	 * If no message is currently in the queue, this method will block until a message appears.
	 * 
	 * @return The next message
	 */
	public String next(){
		synchronized (queue) {
			if( !hasNext() )
				try { queue.wait(); } 
				catch (InterruptedException e) { e.printStackTrace(); }
			
			String message = queue.getFirst();
			queue.removeFirst();
			return message;
		}
	}
		
	/**Checks if there are any elements currently in the queue
	 * 
	 * @return True if there are any messages in the queue
	 */
	public boolean hasNext(){
		synchronized (queue) {
			return queue.size() > 0;
		}
	}
}
