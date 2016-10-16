package My.catalina.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.servlet.ServletContext;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.Globals;
import My.catalina.Lifecycle;
import My.catalina.LifecycleException;
import My.catalina.LifecycleListener;
import My.catalina.Loader;
import My.catalina.Session;
import My.catalina.util.LifecycleSupport;

/**
 * Standard implementation of the <b>Manager</b> interface that provides
 * simple session persistence across restarts of this component (such as
 * when the entire server is shut down and restarted, or when a particular
 * web application is reloaded.
 * <p>
 * <b>IMPLEMENTATION NOTE</b>:  Correct behavior of session storing and
 * reloading depends upon external calls to the <code>start()</code> and
 * <code>stop()</code> methods of this class at the correct times.
 */

public class StandardManager 
	extends ManagerBase
	implements Lifecycle{

	
	// ------------------ Instance Variables ------------------
	
	/**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    protected int maxActiveSessions = -1;
	
	
	/**
     * Path name of the disk file in which active sessions are saved
     * when we stop, and from which these sessions are loaded when we start.
     * A <code>null</code> value indicates that no persistence is desired.
     * If this pathname is relative, it will be resolved against the
     * temporary working directory provided by our context, available via
     * the <code>javax.servlet.context.tempdir</code> context attribute.
     */
    protected String pathname = "SESSIONS.ser";
    
	
	/**
     * Has this component been started yet?
     */
    protected boolean started = false;
	
    
    /**
     * Number of session creations that failed due to maxActiveSessions.
     */
    protected int rejectedSessions = 0;
    
    
    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

	public void addLifecycleListener(LifecycleListener listener) {
		// TODO Auto-generated method stub
	}

	public LifecycleListener[] findLifecycleListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeLifecycleListener(LifecycleListener listener) {
		// TODO Auto-generated method stub
	}
	
	
	
	
	
	/**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public Session createSession(String sessionId) {
    	if ((maxActiveSessions >= 0) &&
                (sessions.size() >= maxActiveSessions)) {
    		rejectedSessions++;
            throw new IllegalStateException("standardManager.createSession.ise");
    	}
    	
    	
    	return (super.createSession(sessionId));
    }
    
    
    /**
     * Set the Container with which this Manager has been associated.  If
     * it is a Context (the usual case), listen for changes to the session
     * timeout property.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {
    	
    	// Default processing provided by our superclass
        super.setContainer(container);
        
        // Register with the new Container (if any)
        if ((this.container != null) && (this.container instanceof Context)) {
        	setMaxInactiveInterval
            ( ((Context) this.container).getSessionTimeout()*60 );  
        }
    	
    }
	
	

	 /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
	public void start() throws LifecycleException {
		if( ! initialized )
            init();
		
		// Validate and update our current component state
        if (started) {
            return;
        }
        
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;
        
        String dummy = generateSessionId();
     
        // Load unloaded sessions, if any
        try {
        	load();
        }
        catch (Throwable t) {
            log.error("standardManager.managerLoad");
        }
        
        
	}

	@Override
	public void stop() throws LifecycleException {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	/**
     * Load any currently active sessions that were previously unloaded
     * to the appropriate persistence mechanism, if any.  If persistence is not
     * supported, this method returns without doing anything.
     *
     * @exception ClassNotFoundException if a serialized class cannot be
     *  found during the reload
     * @exception IOException if an input/output error occurs
     */
    public void load() throws ClassNotFoundException, IOException {
    	doLoad();
    }
    
    /**
     * Load any currently active sessions that were previously unloaded
     * to the appropriate persistence mechanism, if any.  If persistence is not
     * supported, this method returns without doing anything.
     *
     * @exception ClassNotFoundException if a serialized class cannot be
     *  found during the reload
     * @exception IOException if an input/output error occurs
     */
    protected void doLoad() throws ClassNotFoundException, IOException {
    	
    	// Initialize our internal data structures
        sessions.clear();
        
        // Open an input stream to the specified pathname, if any
        File file = file();
        if (file == null)
            return;
        
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        
        try {
        	fis = new FileInputStream(file.getAbsolutePath());
        	
        }
        catch (FileNotFoundException e) {
        	return;
        }
        catch (IOException e) {
        	
        	log.error("standardManager.loading.ioe");
        	
        }
    }
    
    
    /**
     * Return a File object representing the pathname to our
     * persistence file, if any.
     */
    protected File file() {
    	
    	if ((pathname == null) || (pathname.length() == 0))
            return (null);
    	
    	File file = new File(pathname);
        if (!file.isAbsolute()) {
        	if (container instanceof Context) {
        		
        		ServletContext servletContext =
                    ((Context) container).getServletContext();
        		
        		File tempdir = (File)
                servletContext.getAttribute(Globals.WORK_DIR_ATTR);
        		
        		if (tempdir != null)
                    file = new File(tempdir, pathname);
        	}
        }
        
        return (file);
    }
    
	

}
