package My.catalina.core;

import My.catalina.Globals;

public final class ApplicationFilterFactory {

	// -------------------------------------------------------------- Constants


    public static final int ERROR = 1;
    public static final Integer ERROR_INTEGER = new Integer(ERROR);
    public static final int FORWARD = 2;
    public static final Integer FORWARD_INTEGER = new Integer(FORWARD);
    public static final int INCLUDE = 4;
    public static final Integer INCLUDE_INTEGER = new Integer(INCLUDE);
    public static final int REQUEST = 8;
    public static final Integer REQUEST_INTEGER = new Integer(REQUEST);

    public static final String DISPATCHER_TYPE_ATTR = 
        Globals.DISPATCHER_TYPE_ATTR;
    public static final String DISPATCHER_REQUEST_PATH_ATTR = 
        Globals.DISPATCHER_REQUEST_PATH_ATTR;

    private static ApplicationFilterFactory factory = null;
    
    
	// --------------------- Constructors ---------------------
    /*
     * Prevent instanciation outside of the getInstanceMethod().
     */
    private ApplicationFilterFactory() {
    }
    
    
	// -------------------- Public Methods --------------------
    
    /**
     * Return the fqctory instance.
     */
    public static ApplicationFilterFactory getInstance() {
        if (factory == null) {
            factory = new ApplicationFilterFactory();
        }
        return factory;
    }
    
    
    
}
