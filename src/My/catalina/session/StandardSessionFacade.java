package My.catalina.session;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

public class StandardSessionFacade implements HttpSession {

	// -------------------- Constructors ------------------------
	
	/**
     * Construct a new session facade.
     */
    public StandardSessionFacade(StandardSession session) {
        super();
        this.session = (HttpSession) session;
    }
    
    
    /**
     * Construct a new session facade.
     */
    public StandardSessionFacade(HttpSession session) {
        super();
        this.session = session;
    }
    
    
	// -------------------- Instance Variables ------------------------
    
    
    /**
     * Wrapped session object.
     */
    private HttpSession session = null;


    
    
	// -------------------- HttpSession Methods ---------------------
    
    
	@Override
	public long getCreationTime() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setMaxInactiveInterval(int interval) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Object getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setAttribute(String name, Object value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void removeAttribute(String name) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}
    
    
	
    
    
	
	
}
