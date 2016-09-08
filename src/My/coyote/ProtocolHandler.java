package My.coyote;

import java.util.Iterator;

public interface ProtocolHandler {

	/**
     * Pass config info.
     */
    public void setAttribute(String name, Object value);


    public Object getAttribute(String name);
    public Iterator getAttributeNames();

    /**
     * The adapter, used to call the connector.
     */
    public void setAdapter(Adapter adapter);


    public Adapter getAdapter();
    
    
    
    
    /**
     * Init the protocol.
     */
    public void init()
        throws Exception;


    /**
     * Start the protocol.
     */
    public void start()
        throws Exception;
}
