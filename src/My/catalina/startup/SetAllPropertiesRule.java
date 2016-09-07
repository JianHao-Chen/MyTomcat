package My.catalina.startup;

import java.util.HashMap;

import org.xml.sax.Attributes;

import My.tomcat.util.IntrospectionUtils;
import My.tomcat.util.digester.Rule;

public class SetAllPropertiesRule extends Rule{
	// ----------------------------------------------------------- Constructors
    public SetAllPropertiesRule() {}
    
    public SetAllPropertiesRule(String[] exclude) {
        for (int i=0; i<exclude.length; i++ ) if (exclude[i]!=null) this.excludes.put(exclude[i],exclude[i]);
    }

    // ----------------------------------------------------- Instance Variables
    protected HashMap<String,String> excludes = new HashMap<String,String>();

    // --------------------------------------------------------- Public Methods


    /**
     * Handle the beginning of an XML element.
     *
     * @param attributes The attributes of this element
     *
     * @exception Exception if a processing error occurs
     */
    public void begin(String namespace, String nameX, Attributes attributes)
        throws Exception {

        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getLocalName(i);
            if ("".equals(name)) {
                name = attributes.getQName(i);
            }
            String value = attributes.getValue(i);
            if ( !excludes.containsKey(name)) {
                if (!digester.isFakeAttribute(digester.peek(), name) 
                        && !IntrospectionUtils.setProperty(digester.peek(), name, value) 
                        && digester.getRulesValidation()) {
                    System.out.println(" did not find a matching property");
                }
            }
        }

    }
}
