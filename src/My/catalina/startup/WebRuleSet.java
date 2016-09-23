package My.catalina.startup;


import java.util.ArrayList;

import org.xml.sax.Attributes;

import My.catalina.Context;
import My.catalina.Wrapper;
import My.tomcat.util.IntrospectionUtils;
import My.tomcat.util.digester.CallMethodRule;
import My.tomcat.util.digester.CallParamRule;
import My.tomcat.util.digester.Digester;
import My.tomcat.util.digester.Rule;
import My.tomcat.util.digester.RuleSetBase;

/**
 * <p><strong>RuleSet</strong> for processing the contents of a web application
 * deployment descriptor (<code>/WEB-INF/web.xml</code>) resource.</p>
 *
 */

public class WebRuleSet extends RuleSetBase{

	/**
     * The matching pattern prefix to use for recognizing our elements.
     */
    protected String prefix = null;
	
	
	/**
     * <p>Add the set of Rule instances defined in this RuleSet to the
     * specified <code>Digester</code> instance, associating them with
     * our namespace URI (if any).  This method should only be called
     * by a Digester instance.</p>
     */
	
	@Override
	public void addRuleInstances(Digester digester) {
		// TODO Auto-generated method stub
		digester.addRule(prefix + "web-app/servlet",
                new WrapperCreateRule());
		digester.addSetNext(prefix + "web-app/servlet",
                   "addChild",
                   "org.apache.catalina.Container");
		
		
		digester.addCallMethod(prefix + "web-app/servlet/init-param",
                "addInitParameter", 2);
		digester.addCallParam(prefix + "web-app/servlet/init-param/param-name",0);
		digester.addCallParam(prefix + "web-app/servlet/init-param/param-value",1);
		
		digester.addCallMethod(prefix + "web-app/servlet/jsp-file",
                  "setJspFile", 0);
		
		digester.addCallMethod(prefix + "web-app/servlet/load-on-startup",
                  "setLoadOnStartupString", 0);


		digester.addCallMethod(prefix + "web-app/servlet/servlet-class",
        "setServletClass", 0);
		
		digester.addCallMethod(prefix + "web-app/servlet/servlet-name",
        "setName", 0);

		digester.addRule(prefix + "web-app/servlet-mapping",
				new CallMethodMultiRule("addServletMapping", 2, 0));
		
		digester.addCallParam(prefix + "web-app/servlet-mapping/servlet-name", 1);
		
		digester.addRule(prefix + "web-app/servlet-mapping/url-pattern", new CallParamMultiRule(0));
	}

}



/**
 * A Rule that calls the factory method on the specified Context to
 * create the object that is to be added to the stack.
 */

final class WrapperCreateRule extends Rule {

    public WrapperCreateRule() {
    }

    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        Context context =
            (Context) digester.peek(digester.getCount() - 1);
        Wrapper wrapper = context.createWrapper();
        digester.push(wrapper);
       
    }

    public void end(String namespace, String name)
        throws Exception {
        Wrapper wrapper = (Wrapper) digester.pop();
        
    }

}



/**
 * A Rule that can be used to call multiple times a method as many times as needed
 * (used for addServletMapping).
 */
final class CallMethodMultiRule extends CallMethodRule {

    protected int multiParamIndex = 0;
    
    public CallMethodMultiRule(String methodName, int paramCount, int multiParamIndex) {
        super(methodName, paramCount);
        this.multiParamIndex = multiParamIndex;
    }

    public void end() throws Exception {

        // Retrieve or construct the parameter values array
        Object parameters[] = null;
        if (paramCount > 0) {
            parameters = (Object[]) digester.popParams();
        } else {
            super.end();
        }
        
        ArrayList multiParams = (ArrayList) parameters[multiParamIndex];
        
        // Construct the parameter values array we will need
        // We only do the conversion if the param value is a String and
        // the specified paramType is not String. 
        Object paramValues[] = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (i != multiParamIndex) {
                // convert nulls and convert stringy parameters 
                // for non-stringy param types
                if(parameters[i] == null || (parameters[i] instanceof String 
                        && !String.class.isAssignableFrom(paramTypes[i]))) {
                    paramValues[i] =
                        IntrospectionUtils.convert((String) parameters[i], paramTypes[i]);
                } else {
                    paramValues[i] = parameters[i];
                }
            }
        }

        // Determine the target object for the method call
        Object target;
        if (targetOffset >= 0) {
            target = digester.peek(targetOffset);
        } else {
            target = digester.peek(digester.getCount() + targetOffset);
        }

        if (target == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("[CallMethodRule]{");
            sb.append("");
            sb.append("} Call target is null (");
            sb.append("targetOffset=");
            sb.append(targetOffset);
            sb.append(",stackdepth=");
            sb.append(digester.getCount());
            sb.append(")");
            throw new org.xml.sax.SAXException(sb.toString());
        }
        
        if (multiParams == null) {
            paramValues[multiParamIndex] = null;
            Object result = IntrospectionUtils.callMethodN(target, methodName,
                    paramValues, paramTypes);   
            return;
        }
        
        for (int j = 0; j < multiParams.size(); j++) {
            Object param = multiParams.get(j);
            if(param == null || (param instanceof String 
                    && !String.class.isAssignableFrom(paramTypes[multiParamIndex]))) {
                paramValues[multiParamIndex] =
                    IntrospectionUtils.convert((String) param, paramTypes[multiParamIndex]);
            } else {
                paramValues[multiParamIndex] = param;
            }
            Object result = IntrospectionUtils.callMethodN(target, methodName,
                    paramValues, paramTypes);   
        }
        
    }
}


/**
 * A Rule that can be used to call multiple times a method as many times as needed
 * (used for addServletMapping).
 */
final class CallParamMultiRule extends CallParamRule {

    public CallParamMultiRule(int paramIndex) {
        super(paramIndex);
    }

    public void end(String namespace, String name) {
        if (bodyTextStack != null && !bodyTextStack.empty()) {
            // what we do now is push one parameter onto the top set of parameters
            Object parameters[] = (Object[]) digester.peekParams();
            ArrayList params = (ArrayList) parameters[paramIndex];
            if (params == null) {
                params = new ArrayList();
                parameters[paramIndex] = params;
            }
            params.add(bodyTextStack.pop());
        }
    }

}