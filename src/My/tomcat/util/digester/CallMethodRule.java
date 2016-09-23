package My.tomcat.util.digester;

/**
 * <p>Rule implementation that calls a method on an object on the stack
 * (normally the top/parent object), passing arguments collected from 
 * subsequent <code>CallParamRule</code> rules or from the body of this
 * element. </p>
 *
 * <p>By using {@link #CallMethodRule(String methodName)} 
 * a method call can be made to a method which accepts no
 * arguments.</p>
 *
 * <p>Incompatible method parameter types are converted 
 * using <code>org.apache.commons.beanutils.ConvertUtils</code>.
 * </p>
 *
 * <p>This rule now uses
 * <a href="http://commons.apache.org/beanutils/apidocs/org/apache/commons/beanutils/MethodUtils.html">
 * org.apache.commons.beanutils.MethodUtils#invokeMethod
 * </a> by default.
 * This increases the kinds of methods successfully and allows primitives
 * to be matched by passing in wrapper classes.
 * There are rare cases when org.apache.commons.beanutils.MethodUtils#invokeExactMethod 
 * (the old default) is required.
 * This method is much stricter in its reflection.
 * Setting the <code>UseExactMatch</code> to true reverts to the use of this 
 * method.</p>
 *
 * <p>Note that the target method is invoked when the  <i>end</i> of
 * the tag the CallMethodRule fired on is encountered, <i>not</i> when the
 * last parameter becomes available. This implies that rules which fire on
 * tags nested within the one associated with the CallMethodRule will 
 * fire before the CallMethodRule invokes the target method. This behaviour is
 * not configurable. </p>
 *
 * <p>Note also that if a CallMethodRule is expecting exactly one parameter
 * and that parameter is not available (eg CallParamRule is used with an
 * attribute name but the attribute does not exist) then the method will
 * not be invoked. If a CallMethodRule is expecting more than one parameter,
 * then it is always invoked, regardless of whether the parameters were
 * available or not (missing parameters are passed as null values).</p>
 */

public class CallMethodRule extends Rule{

	
	// ------------------- Constructors -------------------
	
	/**
     * Construct a "call method" rule with the specified method name.  The
     * parameter types (if any) default to java.lang.String.
     *
     * @param methodName Method name of the parent method to call
     * @param paramCount The number of parameters to collect, or
     *  zero for a single argument from the body of this element.
     */
    public CallMethodRule(String methodName,
                          int paramCount) {
        this(0, methodName, paramCount);
    }
    
    /**
     * Construct a "call method" rule with the specified method name.  The
     * parameter types (if any) default to java.lang.String.
     *
     * @param targetOffset location of the target object. Positive numbers are
     * relative to the top of the digester object stack. Negative numbers 
     * are relative to the bottom of the stack. Zero implies the top
     * object on the stack.
     * @param methodName Method name of the parent method to call
     * @param paramCount The number of parameters to collect, or
     *  zero for a single argument from the body of this element.
     */
    public CallMethodRule(int targetOffset,
                          String methodName,
                          int paramCount) {

        this.targetOffset = targetOffset;
        this.methodName = methodName;
        this.paramCount = paramCount;        
        if (paramCount == 0) {
            this.paramTypes = new Class[] { String.class };
        } else {
            this.paramTypes = new Class[paramCount];
            for (int i = 0; i < this.paramTypes.length; i++) {
                this.paramTypes[i] = String.class;
            }
        }

    }
    
    
	// -------------------- Instance Variables --------------------
    /**
     * The body text collected from this element.
     */
    protected String bodyText = null;


    /** 
     * location of the target object for the call, relative to the
     * top of the digester object stack. The default value of zero
     * means the target object is the one on top of the stack.
     */
    protected int targetOffset = 0;

    /**
     * The method name to call on the parent object.
     */
    protected String methodName = null;


    /**
     * The number of parameters to collect from <code>MethodParam</code> rules.
     * If this value is zero, a single parameter will be collected from the
     * body of this element.
     */
    protected int paramCount = 0;


    /**
     * The parameter types of the parameters to be collected.
     */
    protected Class paramTypes[] = null;

    /**
     * The names of the classes of the parameters to be collected.
     * This attribute allows creation of the classes to be postponed until the digester is set.
     */
    protected String paramClassNames[] = null;
    
    /**
     * Should <code>MethodUtils.invokeExactMethod</code> be used for reflection.
     */
    protected boolean useExactMatch = false;
}

