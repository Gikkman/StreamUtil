package com.gikk.streamutil.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

/**A Key-Priority queue is a priority queue which orders its elements according to each elements associated key. 
 * <br>Whenever an element is added to the queue, a key needs to be assigned to it. 
 * 
 * @author Simon
 *
 * @param <E> Type the queue contains
 */
public abstract class KeyPriorityQueue<E> implements Iterable<E> {
	//*********************************************************************************************
	//								VARIABLES 		
	//*********************************************************************************************

	private final PriorityQueue<Node> queue;
	
	//*********************************************************************************************
	//								CONSTRUCTORS 	
	//*********************************************************************************************

	public KeyPriorityQueue() {
		queue = new PriorityQueue<Node>();
	}	
	public KeyPriorityQueue(int intialCapacity) {
		queue = new PriorityQueue<Node>(intialCapacity);
	}
	
	//*********************************************************************************************
	// 								PUBLIC 			
	//*********************************************************************************************
	
	public boolean add(double key, E element){
		Node n = new Node(element, key);
		return queue.add(n);
	}
	
	public boolean put(double key, E element){
		return add(key, element);
	}
	
	/** Clears the entire queue
	 */
	public void clear(){
		queue.clear();
	}
	
	/**Goes through the queue and removes the first instance of element that is encountered
	 * 
	 * <br><br>Complexity: O(n) where n is the number of elements in the queue
	 * 
	 * @param element The element we search for
	 * @return True if element is found (and removed), False otherwise
	 */
	public boolean remove(E element){
		Node itr = null;
		for( Node n : queue)
			if( n.e == element ){
				itr = n;
				break;
			}
		if( itr == null)
			return false;
		else
			return queue.remove(itr);
		
	}
	/**Goes through the queue and removes the first instance of an element with the given key.
	 * 
	 * <br><br>Complexity: O(n) where n is the number of elements in the queue
	 * 
	 * @param key The key we are removing element for
	 * @return True if element is found (and removed), False otherwise
	 */
	public boolean remove(double key){
		Node itr = null;
		for( Node n : queue)
			if( n.key == key ){
				itr = n;
				break;
			}
		if( itr == null)
			return false;
		else
			return queue.remove(itr);
		
	}
	
	/**Returns an ArrayList of all elements in the queue. There is no guarantee that it is sorted. 
	 * This list does not affect the underlying structure, but changes to the elements persist
	 * <br><br>Complexity: O(n) where n is the number of elements in the queue
	 * 
	 * @return An ArrayList of all values in the queue. Order is not quaranteed.
	 */
	public ArrayList<E> values(){
		ArrayList<E> out = new ArrayList<E>(queue.size());
		for( Node n : queue )
			out.add( n.e );
		return out;
	}
	
	/**Returns a double[] of all the keys in the queue. There is no guarantee that it is sorted
	 * <br><br>Complexity: O(n) where n is the number of elements in the queue
	 * @return Array of all keys in the queue
	 */
	public double[] getKeys(){
		double[] keys = new double[ queue.size() ];
		int i = 0;
		for( Node n : queue ){
			keys[i] = n.key;
			i++;
		}
		return keys;
	}
	
	public E peek(){
		return (E) queue.peek().e;
	}
	public double peekKey(){
		return queue.peek().key;
	}
	
	public E poll(){
		return (E) queue.poll().e;
	}
	
	public int size(){
		return queue.size();
	}
	
	public boolean contains(E e){
		return queue.
				stream()
				.filter(obj -> obj.e.equals(e) )
				.count() > 0;
				
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public boolean updateKey(E e, double newKey){
		if( queue.contains(e) ){
				remove(e);
				add(newKey, e);
				return true;
		}
		return false;
	}
	
	public class QueueIterator<T> implements Iterator<T>{
		int cursor = 0;
		int cursorMax;
		ArrayList<T> list;
		
		public QueueIterator(ArrayList<T> arrayList) {
			list = arrayList;
			cursorMax = list.size();
		}
		
		@Override
		public boolean hasNext() {
			return cursor < cursorMax;
		}

		@Override
		public T next() {
			if( !hasNext() )
				throw new NoSuchElementException();
			return list.get(cursor++);
		}
		
		@Override
		public void remove(){
			throw new UnsupportedOperationException();
		}				
	}
	
	//*********************************************************************************************
	//								INTERNALS 		
	//*********************************************************************************************

	protected class Node implements Comparator<Node>, Comparable<Node>{
		E e;
		double key;
		
		Node(E e, double key){
			this.e = e;
			this.key = key;
		}

		@Override
		public int compareTo(KeyPriorityQueue<E>.Node o) {
			return abstractComparatorFunc(this, o);
		}

		@Override
		public int compare(KeyPriorityQueue<E>.Node o1, KeyPriorityQueue<E>.Node o2) {
			return abstractComparatorFunc(o1, o2);
		}
	}
	protected abstract int abstractComparatorFunc(KeyPriorityQueue<E>.Node o1, KeyPriorityQueue<E>.Node o2);
	
	public Iterator<E> iterator(){
		return new QueueIterator<E>( values() );
	}
	
	
}