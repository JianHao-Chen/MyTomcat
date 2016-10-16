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
		return session.getCreationTime();
	}


	@Override
	public String getId() {
		return session.getId();
	}


	@Override
	public long getLastAccessedTime() {
		 return session.getLastAccessedTime();
	}


	@Override
	public ServletContext getServletContext() {
		return session.getServletContext();
	}


	@Override
	public void setMaxInactiveInterval(int interval) {
		session.setMaxInactiveInterval(interval);
		
	}


	@Override
	public int getMaxInactiveInterval() {
		return session.getMaxInactiveInterval();
	}


	@Override
	public Object getAttribute(String name) {
		return session.getAttribute(name);
	}


	@Override
	public void setAttribute(String name, Object value) {
		session.setAttribute(name, value);
		
	}


	@Override
	public void removeAttribute(String name) {
		session.removeAttribute(name);
		
	}


	@Override
	public Enumeration getAttributeNames() {
		return session.getAttributeNames();
	}


	@Override
	public void invalidate() {
		session.invalidate();
		
	}


	@Override
	public boolean isNew() {
		return session.isNew();
	}
    
    
	
    
    
	
	
}
