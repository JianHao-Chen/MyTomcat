package My.catalina.startup;

import My.tomcat.util.digester.Digester;
import My.tomcat.util.digester.RuleSetBase;

public class RealmRuleSet extends RuleSetBase{
	 // ----------------------------------------------------- Instance Variables


    /**
     * The matching pattern prefix to use for recognizing our elements.
     */
    protected String prefix = null;


    // ------------------------------------------------------------ Constructor


    /**
     * Construct an instance of this <code>RuleSet</code> with the default
     * matching pattern prefix.
     */
    public RealmRuleSet() {

        this("");

    }


    /**
     * Construct an instance of this <code>RuleSet</code> with the specified
     * matching pattern prefix.
     *
     * @param prefix Prefix for matching pattern rules (including the
     *  trailing slash character)
     */
    public RealmRuleSet(String prefix) {

        super();
        this.prefix = prefix;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Add the set of Rule instances defined in this RuleSet to the
     * specified <code>Digester</code> instance, associating them with
     * our namespace URI (if any).  This method should only be called
     * by a Digester instance.</p>
     *
     * @param digester Digester instance to which the new Rule instances
     *  should be added.
     */
    public void addRuleInstances(Digester digester) {

        digester.addObjectCreate(prefix + "Realm",
                                 null, // MUST be specified in the element,
                                 "className");
        digester.addSetProperties(prefix + "Realm");
        digester.addSetNext(prefix + "Realm",
                            "setRealm",
                            "org.apache.catalina.Realm");

        digester.addObjectCreate(prefix + "Realm/Realm",
                                 null, // MUST be specified in the element
                                 "className");
        digester.addSetProperties(prefix + "Realm/Realm");
        digester.addSetNext(prefix + "Realm/Realm",
                            "addRealm",
                            "org.apache.catalina.Realm");

    }
}
