package My.catalina.startup;

import My.catalina.Container;
import My.catalina.Context;
import My.catalina.core.StandardWrapper;

public class WebAnnotationSet {

	// ------------------- Public Methods --------------------
	
	/**
     * Process the annotations on a context.
     */
    public static void loadApplicationAnnotations(Context context) {
    	
    	loadApplicationServletAnnotations(context);
    }
    
    
    
	// ------------------- protected Methods -------------------
    
    
    /**
     * Process the annotations for the servlets.
     */
    protected static void loadApplicationServletAnnotations(Context context) {
    	
    	ClassLoader classLoader = context.getLoader().getClassLoader();
        StandardWrapper wrapper = null;
        Class classClass = null;
        
        Container[] children = context.findChildren();
        
        for (int i = 0; i < children.length; i++) {
        	if (children[i] instanceof StandardWrapper) {
        		wrapper = (StandardWrapper) children[i];
        		if (wrapper.getServletClass() == null) {
                    continue;
                }
        		
        		try {
        			classClass = classLoader.loadClass(wrapper.getServletClass());
        		}catch (ClassNotFoundException e) {
        			// We do nothing
        		}catch (NoClassDefFoundError e) {
        			// We do nothing
        		}
        		
        		if (classClass == null) {
                    continue;
                }
        		
        		
        	}
        	
        }
    }
	
}
