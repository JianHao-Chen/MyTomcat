package My.tomcat.util.digester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RulesBase implements Rules {
	// ----------------------------------------------------- Instance Variables


    /**
     * The set of registered Rule instances, keyed by the matching pattern.
     * Each value is a List containing the Rules for that pattern, in the
     * order that they were orginally registered.
     */
    protected HashMap cache = new HashMap();


    /**
     * The Digester instance with which this Rules instance is associated.
     */
    protected Digester digester = null;


    /**
     * The namespace URI for which subsequently added <code>Rule</code>
     * objects are relevant, or <code>null</code> for matching independent
     * of namespaces.
     */
    protected String namespaceURI = null;


    /**
     * The set of registered Rule instances, in the order that they were
     * originally registered.
     */
    protected ArrayList rules = new ArrayList();


    // ------------------------------------------------------------- Properties


    /**
     * Return the Digester instance with which this Rules instance is
     * associated.
     */
    public Digester getDigester() {

        return (this.digester);

    }


    /**
     * Set the Digester instance with which this Rules instance is associated.
     *
     * @param digester The newly associated Digester instance
     */
    public void setDigester(Digester digester) {

        this.digester = digester;
        Iterator items = rules.iterator();
        while (items.hasNext()) {
            Rule item = (Rule) items.next();
            item.setDigester(digester);
        }

    }


    /**
     * Return the namespace URI that will be applied to all subsequently
     * added <code>Rule</code> objects.
     */
    public String getNamespaceURI() {

        return (this.namespaceURI);

    }


    /**
     * Set the namespace URI that will be applied to all subsequently
     * added <code>Rule</code> objects.
     *
     * @param namespaceURI Namespace URI that must match on all
     *  subsequently added rules, or <code>null</code> for matching
     *  regardless of the current namespace URI
     */
    public void setNamespaceURI(String namespaceURI) {

        this.namespaceURI = namespaceURI;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Register a new Rule instance matching the specified pattern.
     *
     * @param pattern Nesting pattern to be matched for this Rule
     * @param rule Rule instance to be registered
     */
    public void add(String pattern, Rule rule) {
        // to help users who accidently add '/' to the end of their patterns
        int patternLength = pattern.length();
        if (patternLength>1 && pattern.endsWith("/")) {
            pattern = pattern.substring(0, patternLength-1);
        }
        
        
        List list = (List) cache.get(pattern);
        if (list == null) {
            list = new ArrayList();
            cache.put(pattern, list);
        }
        list.add(rule);
        rules.add(rule);
        if (this.digester != null) {
            rule.setDigester(this.digester);
        }
        if (this.namespaceURI != null) {
            rule.setNamespaceURI(this.namespaceURI);
        }

    }


    /**
     * Clear all existing Rule instance registrations.
     */
    public void clear() {

        cache.clear();
        rules.clear();

    }


    /**
     * Return a List of all registered Rule instances that match the specified
     * nesting pattern, or a zero-length List if there are no matches.  If more
     * than one Rule instance matches, they <strong>must</strong> be returned
     * in the order originally registered through the <code>add()</code>
     * method.
     *
     * @param pattern Nesting pattern to be matched
     *
     * @deprecated Call match(namespaceURI,pattern) instead.
     */
    public List match(String pattern) {

        return (match(null, pattern));

    }


    /**
     * Return a List of all registered Rule instances that match the specified
     * nesting pattern, or a zero-length List if there are no matches.  If more
     * than one Rule instance matches, they <strong>must</strong> be returned
     * in the order originally registered through the <code>add()</code>
     * method.
     *
     * @param namespaceURI Namespace URI for which to select matching rules,
     *  or <code>null</code> to match regardless of namespace URI
     * @param pattern Nesting pattern to be matched
     */
    public List match(String namespaceURI, String pattern) {

        // List rulesList = (List) this.cache.get(pattern);
        List rulesList = lookup(namespaceURI, pattern);
        if ((rulesList == null) || (rulesList.size() < 1)) {
            // Find the longest key, ie more discriminant
            String longKey = "";
            Iterator keys = this.cache.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (key.startsWith("*/")) {
                    if (pattern.equals(key.substring(2)) ||
                        pattern.endsWith(key.substring(1))) {
                        if (key.length() > longKey.length()) {
                            // rulesList = (List) this.cache.get(key);
                            rulesList = lookup(namespaceURI, key);
                            longKey = key;
                        }
                    }
                }
            }
        }
        if (rulesList == null) {
            rulesList = new ArrayList();
        }
        return (rulesList);

    }


    /**
     * Return a List of all registered Rule instances, or a zero-length List
     * if there are no registered Rule instances.  If more than one Rule
     * instance has been registered, they <strong>must</strong> be returned
     * in the order originally registered through the <code>add()</code>
     * method.
     */
    public List rules() {

        return (this.rules);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Return a List of Rule instances for the specified pattern that also
     * match the specified namespace URI (if any).  If there are no such
     * rules, return <code>null</code>.
     *
     * @param namespaceURI Namespace URI to match, or <code>null</code> to
     *  select matching rules regardless of namespace URI
     * @param pattern Pattern to be matched
     */
    protected List lookup(String namespaceURI, String pattern) {

        // Optimize when no namespace URI is specified
        List list = (List) this.cache.get(pattern);
        if (list == null) {
            return (null);
        }
        if ((namespaceURI == null) || (namespaceURI.length() == 0)) {
            return (list);
        }

        // Select only Rules that match on the specified namespace URI
        ArrayList results = new ArrayList();
        Iterator items = list.iterator();
        while (items.hasNext()) {
            Rule item = (Rule) items.next();
            if ((namespaceURI.equals(item.getNamespaceURI())) ||
                    (item.getNamespaceURI() == null)) {
                results.add(item);
            }
        }
        return (results);

    }

}
