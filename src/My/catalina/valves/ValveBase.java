package My.catalina.valves;

import java.io.IOException;

import javax.servlet.ServletException;

import My.catalina.Contained;
import My.catalina.Container;
import My.catalina.Valve;
import My.catalina.connector.Request;
import My.catalina.connector.Response;

public abstract class ValveBase implements Contained , Valve{

	// ------------------ Instance Variables ------------------
	 /**
     * The Container whose pipeline this Valve is a component of.
     */
    protected Container container = null;
    
    /**
     * The next Valve in the pipeline this Valve is a component of.
     */
    protected Valve next = null;

	// --------------------- Properties ---------------------
    /**
     * Return the Container with which this Valve is associated, if any.
     */
    public Container getContainer() {

        return (container);

    }


    /**
     * Set the Container with which this Valve is associated, if any.
     *
     * @param container The new associated container
     */
    public void setContainer(Container container) {

        this.container = container;

    }
    
    
    /**
     * Return the next Valve in this pipeline, or <code>null</code> if this
     * is the last Valve in the pipeline.
     */
    public Valve getNext() {

        return (next);

    }


    /**
     * Set the Valve that follows this one in the pipeline it is part of.
     *
     * @param valve The new next valve
     */
    public void setNext(Valve valve) {

        this.next = valve;

    }
    
    
    
    
	//------------------------ Public Methods ------------------------
    
    
    
    /**
     * Return a String rendering of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(this.getClass().getName());
        sb.append("[");
        if (container != null)
            sb.append(container.getName());
        sb.append("]");
        return (sb.toString());
    }


	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void backgroundProcess() {
		
	}


	@Override
	public abstract void invoke(Request request, Response response) throws IOException,
			ServletException ;
		
	
}
