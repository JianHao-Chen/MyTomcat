package My.catalina.startup;

import My.tomcat.util.digester.Digester;
import My.tomcat.util.digester.RuleSetBase;

/**
 * <p><strong>RuleSet</strong> for processing the contents of a
 * Context or DefaultContext definition element.  To enable parsing of a
 * DefaultContext, be sure to specify a prefix that ends with "/Default".</p>
 */

public class ContextRuleSet extends RuleSetBase{
	// ----------------------------------------------------- Instance Variables


    /**
     * The matching pattern prefix to use for recognizing our elements.
     */
    protected String prefix = null;


    /**
     * Should the context be created.
     */
    protected boolean create = true;


    // ------------------------------------------------------------ Constructor


    /**
     * Construct an instance of this <code>RuleSet</code> with the default
     * matching pattern prefix.
     */
    public ContextRuleSet() {

        this("");

    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified
     * matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the
     *  trailing slash character)
     */
    public ContextRuleSet(String prefix) {

        super();
        this.prefix = prefix;

    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified
     * matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the
     *  trailing slash character)
     */
    public ContextRuleSet(String prefix, boolean create) {

        super();
        this.prefix = prefix;
        this.create = create;

    }


    // --------------------------------------------------------- Public Methods

    /**
     * <p>Add the set of Rule instances defined in this RuleSet to the
     * specified <code>Digester</code> instance, associating them with
     * our namespace URI (if any).  This method should only be called
     * by a Digester instance.</p>
     */
    
    public void addRuleInstances(Digester digester) {
    	
    	digester.addObjectCreate(prefix + "Context",
                "My.catalina.core.StandardContext", "className");
        digester.addSetProperties(prefix + "Context");
        
        
        digester.addRule(prefix + "Context",
                new LifecycleListenerRule
                    ("My.catalina.startup.ContextConfig",
                     "configClass"));
        digester.addSetNext(prefix + "Context",
                   "addChild",
                   "My.catalina.Container");
        
        
        digester.addCallMethod(prefix + "Context/InstanceListener",
                "addInstanceListener", 0);

		digester.addObjectCreate(prefix + "Context/Listener",
		                  null, // MUST be specified in the element
		                  "className");
		digester.addSetProperties(prefix + "Context/Listener");
		digester.addSetNext(prefix + "Context/Listener",
		             "addLifecycleListener",
		             "My.catalina.LifecycleListener");
		
		digester.addObjectCreate(prefix + "Context/Loader",
		             "My.catalina.loader.WebappLoader",
		             "className");
		digester.addSetProperties(prefix + "Context/Loader");
		digester.addSetNext(prefix + "Context/Loader",
		             "setLoader",
		             "My.catalina.Loader");
    }
}
