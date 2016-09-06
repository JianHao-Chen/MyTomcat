package My.tomcat.util.digester;

import java.util.ArrayList;
import java.util.EmptyStackException;

public class ArrayStack extends ArrayList{

	/**
     * Constructs a new empty <code>ArrayStack</code>. The initial size
     * is controlled by <code>ArrayList</code> and is currently 10.
     */
    public ArrayStack() {
        super();
    }
    
    /**
     * Constructs a new empty <code>ArrayStack</code> with an initial size.
     */
    public ArrayStack(int initialSize) {
        super(initialSize);
    }
    
    
    /**
     * Return <code>true</code> if this stack is currently empty.
     */
    public boolean empty() {
        return isEmpty();
    }
    
    
    /**
     * Returns the top item off of this stack without removing it.
     */
    public Object peek() throws EmptyStackException {
        int n = size();
        if (n <= 0) {
            throw new EmptyStackException();
        } else {
            return get(n - 1);
        }
    }
    
    
    /**
     * Returns the n'th item down (zero-relative) from the top of this
     * stack without removing it.
     */
    public Object peek(int n) throws EmptyStackException {
        int m = (size() - n) - 1;
        if (m < 0) {
            throw new EmptyStackException();
        } else {
            return get(m);
        }
    }
    
    
    /**
     * Pops the top item off of this stack and return it.
     */
    public Object pop() throws EmptyStackException {
        int n = size();
        if (n <= 0) {
            throw new EmptyStackException();
        } else {
            return remove(n - 1);
        }
    }
    
    
    /**
     * Pushes a new item onto the top of this stack. The pushed item is also
     * returned. This is equivalent to calling <code>add</code>.
     */
    public Object push(Object item) {
        add(item);
        return item;
    }
    
    
    /**
     * Returns the one-based position of the distance from the top that the
     * specified object exists on this stack, where the top-most element is
     * considered to be at distance <code>1</code>.  If the object is not
     * present on the stack, return <code>-1</code> instead.  The
     * <code>equals()</code> method is used to compare to the items
     * in this stack.
     */
    public int search(Object object) {
        int i = size() - 1;        // Current index
        int n = 1;                 // Current distance
        while (i >= 0) {
            Object current = get(i);
            if ((object == null && current == null) ||
                (object != null && object.equals(current))) {
                return n;
            }
            i--;
            n++;
        }
        return -1;
    }
    
}
