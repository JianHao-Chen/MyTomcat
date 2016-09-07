package My.catalina.startup;

import org.xml.sax.Attributes;

import My.catalina.Container;
import My.catalina.Lifecycle;
import My.catalina.LifecycleListener;
import My.tomcat.util.digester.Rule;

/**
 * Rule that creates a new {@link LifecycleListener} and associates it with the
 * top object on the stack which must implement {@link Container} and
 * {@link Lifecycle}. The implementation class to be used is determined by:
 * <ol>
 * <li>Does the top element on the stack specify an implementation class using
 *     the attribute specified when this rule was created?</li>
 * <li>Does the parent {@link Container} of the {@link Container} on the top of
 *     the stack specify an implementation class using the attribute specified
 *     when this rule was created?</li>
 * <li>Use the default implementation class specified when this rule was
 *     created.</li>
 * </ol>
 */

public class LifecycleListenerRule extends Rule{
	// ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this Rule.
     *
     * @param listenerClass Default name of the LifecycleListener
     *  implementation class to be created
     * @param attributeName Name of the attribute that optionally
     *  includes an override name of the LifecycleListener class
     */
    public LifecycleListenerRule(String listenerClass, String attributeName) {

        this.listenerClass = listenerClass;
        this.attributeName = attributeName;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The attribute name of an attribute that can override the
     * implementation class name.
     */
    private String attributeName;


    /**
     * The name of the <code>LifecycleListener</code> implementation class.
     */
    private String listenerClass;


    // --------------------------------------------------------- Public Methods

    /**
     * Handle the beginning of an XML element.
     */
    public void begin(String namespace, String name, Attributes attributes)
    throws Exception {
    	
    	 Container c = (Container) digester.peek();
         Container p = null;
         Object obj = digester.peek(1);
         if (obj instanceof Container) {
             p = (Container) obj;
         }

         String className = null;
         
         // Check the container for the specified attribute
         if (attributeName != null) {
             String value = attributes.getValue(attributeName);
             if (value != null)
                 className = value;
         }
         
         
      // Use the default
         if (className == null) {
             className = listenerClass;
         }
         
         // Instantiate a new LifecyleListener implementation object
         Class<?> clazz = Class.forName(className);
         LifecycleListener listener =
             (LifecycleListener) clazz.newInstance();

         // Add this LifecycleListener to our associated component
         ( (Lifecycle)c).addLifecycleListener(listener);
         
         
    }
}
