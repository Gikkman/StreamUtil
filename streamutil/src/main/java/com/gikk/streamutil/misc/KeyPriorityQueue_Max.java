package com.gikk.streamutil.misc;

/**A small wrapper around a priority queue which allows the user to add an element and assign the element a key-value.
 * The priority queue is then sorted in descending order (highest key to lowest key, where the highest key is the queue's head).
 * 
 * In case two keys are tied, the first added will be found first in the list
 * 
 * @param <E> The type the queue should contain. 
 */
public class KeyPriorityQueue_Max<E> extends KeyPriorityQueue<E> {

	@Override
	public int abstractComparatorFunc(KeyPriorityQueue<E>.Node o1, KeyPriorityQueue<E>.Node o2) {
		if( o1.key < o2.key )
			return 1;
		else if ( o1.key > o2.key )
			return -1;
		else
			return 0;
	}	
}
