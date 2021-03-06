package priorityqueues;

/**
 * <p>A {@link Exception} thrown by {@link PriorityQueue}-based data structures when the queue is empty.</p>
 *
 * <p>Simply returning <tt>null</tt>  from search methods is ambiguous on the part of the caller, because the caller
 * can then not tell whether  * the tree is empty or not by simply checking for <tt>null</tt>-ity of the object reference. While, strictly
 * speaking, in both cases of an empty tree and a non-empty tree where the element in question is simply not there
 * we have the same semantics, it is good practice to notify the caller that a search has been made for a tree
 * that is empty, so that the application can make sure that at the time of the call the tree is guaranteed
 * to have some data stored.</p>
 *
 * <p>You should <b>not</b> edit this class! It is given to you as a resource for your project.</p>
 *
 * @author  <a href="mailto:jasonfil@cs.umd.edu">Jason Filippou</a>
 *
 * @see lists.EmptyListException
 * @see fifoqueues.EmptyFIFOQueueException
 * @see heaps.EmptyHeapException
 */
public class EmptyPriorityQueueException extends Exception {
    public EmptyPriorityQueueException(String msg){
        super(msg);
    }
}
