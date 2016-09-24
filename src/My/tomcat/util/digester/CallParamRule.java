package My.tomcat.util.digester;

import org.xml.sax.Attributes;

/**
 * <p>Rule implementation that saves a parameter for use by a surrounding 
 * <code>CallMethodRule<code>.</p>
 *
 * <p>This parameter may be:
 * <ul>
 * <li>from an attribute of the current element
 * See {@link #CallParamRule(int paramIndex, String attributeName)}
 * <li>from current the element body
 * See {@link #CallParamRule(int paramIndex)}
 * <li>from the top object on the stack. 
 * See {@link #CallParamRule(int paramIndex, boolean fromStack)}
 * <li>the current path being processed (separate <code>Rule</code>). 
 * See {@link PathCallParamRule}
 * </ul>
 * </p>
 */

public class CallParamRule extends Rule{

	// ------------------------- Constructors -------------------------
	
	/**
     * Construct a "call parameter" rule that will save the body text of this
     * element as the parameter value.
     *
     * @param paramIndex The zero-relative parameter number
     */
    public CallParamRule(int paramIndex) {

        this(paramIndex, null);

    }
    
    /**
     * Construct a "call parameter" rule that will save the value of the
     * specified attribute as the parameter value.
     *
     * @param paramIndex The zero-relative parameter number
     * @param attributeName The name of the attribute to save
     */
    public CallParamRule(int paramIndex,
                         String attributeName) {

        this.paramIndex = paramIndex;
        this.attributeName = attributeName;

    }
    
    
	// ------------------------- Instance Variables -------------------------
    
    /**
     * The attribute from which to save the parameter value
     */
    protected String attributeName = null;


    /**
     * The zero-relative index of the parameter we are saving.
     */
    protected int paramIndex = 0;


    /**
     * Is the parameter to be set from the stack?
     */
    protected boolean fromStack = false;
    
    /**
     * The position of the object from the top of the stack
     */
    protected int stackIndex = 0;

    /** 
     * Stack is used to allow nested body text to be processed.
     * Lazy creation.
     */
    protected ArrayStack bodyTextStack;
    
	// ------------------- Public Methods -------------------
    
    /**
     * Process the start of this element.
     *
     * @param attributes The attribute list for this element
     */
    public void begin(Attributes attributes) throws Exception {
    	
    	Object param = null;
        
        if (attributeName != null) {
        	param = attributes.getValue(attributeName);
        }
        else if(fromStack) {
        	
        	param = digester.peek(stackIndex);
        }
        
        
        // Have to save the param object to the param stack frame here.
        // Can't wait until end(). Otherwise, the object will be lost.
        // We can't save the object as instance variables, as 
        // the instance variables will be overwritten
        // if this CallParamRule is reused in subsequent nesting.
        
        if(param != null) {
        	Object parameters[] = (Object[]) digester.peekParams();
            parameters[paramIndex] = param;
        }

    }
    
    
    
    /**
     * Process the body text of this element.
     *
     * @param bodyText The body text of this element
     */
    public void body(String bodyText) throws Exception {

        if (attributeName == null && !fromStack) {
            // We must wait to set the parameter until end
            // so that we can make sure that the right set of parameters
            // is at the top of the stack
            if (bodyTextStack == null) {
                bodyTextStack = new ArrayStack();
            }
            bodyTextStack.push(bodyText.trim());
        }

    }
    
    
    
    /**
     * Process any body texts now.
     */
    public void end(String namespace, String name) {
        if (bodyTextStack != null && !bodyTextStack.empty()) {
            // what we do now is push one parameter onto the top set of parameters
            Object parameters[] = (Object[]) digester.peekParams();
            parameters[paramIndex] = bodyTextStack.pop();
        }
    }
    
    
    
}
