package priorityqueues;

import exceptions.InvalidCapacityException;
import exceptions.UnsupportedOperationException;
import fifoqueues.CircularArrayFIFOQueue;
import fifoqueues.EmptyQueueException;
import fifoqueues.FIFOQueue;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 *  <tt>LinearPriorityQueue</tt> is a priority queue implemented as an array
 * of common FIFO fifoqueues, where the fifoqueues themselves hold objects
 * with the same priority in the order they arrived. It is important
 * to understand that, because in a priority queue new elements can be 
 * added in pretty much any position of the data structure, a List is not
 * a suitable container for the FIFO fifoqueues mentioned above, since it would
 * only allow for adding an element at the first or last queue in the sequence.
 * Thus, the statement in p.661 of Java Foundations, that a list of fifoqueues is
 * a suitable implementation for a priority queue, is not completely correct.
 * In this implementation, we will use a Java ArrayList, which allows us to add
 * elements at any position in the structure.
 * 
 *  A more straightforward implementation of a priority queue as a special-
 * case heap can be found in priorityqueues.HeapPriorityQueue.java.
 *
 * @param <T> The type held by the container.
 * 
 * @author Jason Filippou (jasonfil@cs.umd.edu)
 *
 * @see HeapPriorityQueue
 *
 */
public class LinearPriorityQueue<T> implements PriorityQueue<T> {

	private ArrayList<PriorityFIFOQueueNode<T>> data; // See below for the implementation of the type PriorityFIFOQueueNode
	private final int DEFAULT_CAPACITY = 10;
	protected boolean modificationFlag;

	/**
	 * Default constructor initializes the data structure with
	 * the default capacity.
	 */
	public LinearPriorityQueue(){
		data = new ArrayList<PriorityFIFOQueueNode<T>>(DEFAULT_CAPACITY);
		modificationFlag = false;
	}

	/**
	 * Non-default constructor initializes the data structure with 
	 * the provided capacity.
	 * @param capacity The initial capacity to endow our inner implementation with.
	 * @throws InvalidCapacityException if the capacity provided is negative.
	 */
	public LinearPriorityQueue(int capacity) throws InvalidCapacityException{
		if(capacity < 0)
			throw new InvalidCapacityException("Invalid capacity provided!");
		data = new ArrayList<PriorityFIFOQueueNode<T>>(capacity);
		modificationFlag = false;
	}
	
	/**
	 * The current implementation does <em>not</em> allow for copy-construction.
	 * The reason is that the iterator returns the objects contained themselves,
	 * not their priorities. It is therefore hard to produce the exact same
	 * queue as the one provided (simply considering a unique priority per 
	 * object scanned would produce a semantically equivalent representation, but
	 * in terms of the memory model the two fifoqueues would be very different).
	 * @param other The queue to base the creation of the current object on.
	 * @throws UnsupportedOperationException always
	 */
	public LinearPriorityQueue(FIFOQueue<T> other){
		try {
			throw new UnsupportedOperationException("Current implementation does not allow for copy construction");
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Standard equals() method. priorityqueues.PriorityQueue objects are Iterable, therefore
	 * it is made easy to write one such method.
	 * @param other The Object to compare this priorityqueues.LinearPriorityQueue to.
	 * @return true if the Objects are equal according to the criteria implied by the method.
	 */
	
	@Override
	public boolean equals(Object other){
		if(other == null)
			return false;
		if(!(other instanceof FIFOQueue<?>))
			return false;
		@SuppressWarnings("unchecked")
		FIFOQueue<T> otherq = (FIFOQueue<T>)other;
		if(otherq.size() != size())
			return false;
		Iterator<T> itForThis = iterator(), itForOther = otherq.iterator();
		while(itForThis.hasNext())
			if(!itForThis.next().equals(itForOther.next()))
				return false;
		return true;
	}

	@Override
	public void enqueue(T element, int priority) { 
		for(int i = 0; i < data.size(); i++){
			if(data.get(i).getPriority() == priority){ // FIFO case
				data.get(i).enqueue(element); 
				return;
			}
			else if(data.get(i).getPriority() > priority){ // Must make a new queue before the currently scanned one.
				int prev = (i > 0) ? i - 1 : 0;
				data.add(prev, new PriorityFIFOQueueNode<T>(element, priority));
				return;
			}
		}
		// The for loop ending would mean that the element inserted has
		// the lowest priority of all elements already in the priority queue,
		// and thus a new FIFO queue needs to be initialized with "element"
		// as its only element, and then added to the back of the ArrayList.
		data.add(new PriorityFIFOQueueNode<T>(element, priority));
		modificationFlag = true;
	}

	@Override
	public T dequeue() throws EmptyQueueException {
		if(isEmpty())
			throw new EmptyQueueException("dequeue(): queue is empty!");
		T first = data.get(0).dequeue(); 
		if(data.get(0).isEmpty())
			data.remove(0);
		modificationFlag = true;
		return first;
	}

	@Override
	public T first() throws EmptyQueueException {
		if(isEmpty())
			throw new EmptyQueueException("dequeue(): queue is empty!");
		return data.get(0).first(); // call to first() instead of dequeue()
	}


	@Override
	public int size() {
		int total = 0;
		for(PriorityFIFOQueueNode<T> node : data)
			total += node.size();
		return total;
	}

	@Override
	public boolean isEmpty() {
		return data.size() == 0;
	}

	@Override
	public void clear() {
		data.clear();
		modificationFlag = true;
	}

	@Override
	public Iterator<T> iterator() {
		return new LinearPriorityQueueIterator<T>();
	}

	class LinearPriorityQueueIterator<T2> implements Iterator<T2> {

		private Iterator<PriorityFIFOQueueNode<T>> arrayListIt;
		private Iterator<T> queueIt;
		private boolean calledNextOnce;
		
		public LinearPriorityQueueIterator(){
			modificationFlag = false;
			arrayListIt = data.iterator();
			calledNextOnce = false;
			queueIt = (arrayListIt.hasNext()) ? arrayListIt.next().iterator() : null;  
		}
		
		@Override
		public boolean hasNext() {
			if(data.isEmpty() || queueIt == null)
				return false;
			if(queueIt.hasNext())
				return true;
			// If the current queue does not have a subsequent element, 
			// we need to look whether there are other fifoqueues in the ArrayList
			// that might have!
			else { 
				while(arrayListIt.hasNext() ){
					queueIt =arrayListIt.next().iterator();
					if(queueIt.hasNext())
						return true;
				}  
			}
			return false;
		}
		

		@SuppressWarnings("unchecked")
		@Override
		public T2 next() throws ConcurrentModificationException{
			if(modificationFlag)
				throw new ConcurrentModificationException("next(): Attempted to traverse a modified priority queue.");
			if(queueIt.hasNext()){
				calledNextOnce = true;
				return (T2)queueIt.next();
			}
			// If not, similarly to hasNext(), advance the iterator
			// until we find a non-empty queue. If we don't, just return null.
			else{
				while(arrayListIt.hasNext()){
					queueIt = arrayListIt.next().iterator();
					if(queueIt.hasNext()){
						calledNextOnce = true;
						return (T2)queueIt.next();
					}
				}
			}
			return null;
		}
		
		@Override
		public void remove()  throws IllegalStateException{
			if(!calledNextOnce)
				throw new IllegalStateException("Need at least one call to next() prior to removal.");
			queueIt.remove();
		}
		
	}



	/**
	 * A PriorityFIFOQueueNode is a FIFOQueue which is enhanced with an integer
	 * that dictates its priority. In this case we use a CircularArrayFIFOQueue
	 * as our preferred queue implementation, but any one of our fifoqueues can
	 * be used.
	 * 
	 * @author Jason Filippou (jasonfil@cs.umd.edu)
	 *
	 * @param <T> The type of element held by the PriorityFIFOQueueNode.
	 */
	@SuppressWarnings("hiding")
	private class PriorityFIFOQueueNode<T> extends CircularArrayFIFOQueue<T> {

		private int priority;

		/**
		 * Constructor that supplies the data element and priority
		 * for the PriorityFIFOQueueNode.
		 * @param data the data element of the PriorityFIFOQueueNode
		 * @param priority the priority of the PriorityFIFOQueueNode in the priority queue
		 */
		public PriorityFIFOQueueNode(T data, int priority){
			this.priority = priority;
			if(data != null) // Don't fill up with wasteful references
				enqueue(data);
		}

		/**
		 * Constructor that supplies the data element of the PriorityFIFOQueueNode.
		 * Default priority given: -1.
		 * @param dat the data element of the PriorityFIFOQueueNode
		 */
		public PriorityFIFOQueueNode(T dat){
			this(dat, -1);
		}

		/**
		 * Default constructor provides the node with a null data reference 
		 * and -1 priority.
		 */
		public PriorityFIFOQueueNode(){
			this(null);
		}

		/**
		 * Simple getter for priority of node.
		 * @return the originally supplied priority of the node.
		 */
		public int getPriority(){
			return priority;
		}

		/**
		 * Simple setter for node's priority.
		 * @param priority the priority to supply to the node.
		 */
		public void setPriority(int priority){
			this.priority = priority;
		}
	}
	
	@Override
	public String toString(){
		String retVal ="[";
		for(PriorityFIFOQueueNode<T> pqn : data)
			for(T el : pqn)
				retVal += (el + ", ");
		retVal = retVal.substring(0, retVal.length() - 2); // Eat up last comma (", ")
		return retVal + "]";
	}


}