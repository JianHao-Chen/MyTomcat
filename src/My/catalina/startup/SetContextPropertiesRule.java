package My.catalina.startup;

import org.xml.sax.Attributes;

import My.tomcat.util.IntrospectionUtils;
import My.tomcat.util.digester.Rule;

public class SetContextPropertiesRule extends Rule{

	
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
            if ("path".equals(name) || "docBase".equals(name)) {
                continue;
            }
            String value = attributes.getValue(i);
            if (!digester.isFakeAttribute(digester.peek(), name) 
                    && !IntrospectionUtils.setProperty(digester.peek(), name, value) 
                    && digester.getRulesValidation()) {
                
            }
    	}
    	
    }
	
}
