package My.tomcat.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

/**
 * Utils for introspection and reflection
 */

public final class IntrospectionUtils {

	 private static My.juli.logging.Log log=
	        My.juli.logging.LogFactory.getLog( IntrospectionUtils.class );
	 
	
	 
	 public static Object getProperty(Object o, String name) {
	        String getter = "get" + capitalize(name);
	        String isGetter = "is" + capitalize(name);

	        try {
	            Method methods[] = findMethods(o.getClass());
	            Method getPropertyMethod = null;

	            // First, the ideal case - a getFoo() method
	            for (int i = 0; i < methods.length; i++) {
	                Class paramT[] = methods[i].getParameterTypes();
	                if (getter.equals(methods[i].getName()) && paramT.length == 0) {
	                    return methods[i].invoke(o, (Object[]) null);
	                }
	                if (isGetter.equals(methods[i].getName()) && paramT.length == 0) {
	                    return methods[i].invoke(o, (Object[]) null);
	                }

	                if ("getProperty".equals(methods[i].getName())) {
	                    getPropertyMethod = methods[i];
	                }
	            }

	            // Ok, no setXXX found, try a getProperty("name")
	            if (getPropertyMethod != null) {
	                Object params[] = new Object[1];
	                params[0] = name;
	                return getPropertyMethod.invoke(o, params);
	            }

	        } catch (IllegalArgumentException ex2) {
	            log.warn("IAE " + o + " " + name, ex2);
	        } catch (SecurityException ex1) {
	            if (dbg > 0)
	                d("SecurityException for " + o.getClass() + " " + name + ")");
	            if (dbg > 1)
	                ex1.printStackTrace();
	        } catch (IllegalAccessException iae) {
	            if (dbg > 0)
	                d("IllegalAccessException for " + o.getClass() + " " + name
	                        + ")");
	            if (dbg > 1)
	                iae.printStackTrace();
	        } catch (InvocationTargetException ie) {
	            if (dbg > 0)
	                d("InvocationTargetException for " + o.getClass() + " " + name
	                        + ")");
	            if (dbg > 1)
	                ie.printStackTrace();
	        }
	        return null;
	    }
	 
	 
	
	/**
     * Find a method with the right name If found, call the method ( if param is
     * int or boolean we'll convert value to the right type before)
     */
	public static boolean setProperty(Object o, String name, String value) {
		return setProperty(o,name,value,true);
	}
	
	public static boolean setProperty(Object o, String name, String value,boolean invokeSetProperty) {
		
		 String setter = "set" + capitalize(name);
		 
		 try {
			 Method methods[] = findMethods(o.getClass());
			 Method setPropertyMethodVoid = null;
			 Method setPropertyMethodBool = null;

			 // First, the ideal case - a setFoo( String ) method
			 for (int i = 0; i < methods.length; i++) {
				 Class paramT[] = methods[i].getParameterTypes();
				 if (setter.equals(methods[i].getName()) && paramT.length == 1
						 && "java.lang.String".equals(paramT[0].getName())) {

					 methods[i].invoke(o, new Object[] { value });
					 return true;
				 }
			 }
			 
			// Try a setFoo ( int ) or ( boolean )
	            for (int i = 0; i < methods.length; i++) {
	                boolean ok = true;
	                if (setter.equals(methods[i].getName())
	                        && methods[i].getParameterTypes().length == 1) {

	                    // match - find the type and invoke it
	                    Class paramType = methods[i].getParameterTypes()[0];
	                    Object params[] = new Object[1];

	                    // Try a setFoo ( int )
	                    if ("java.lang.Integer".equals(paramType.getName())
	                            || "int".equals(paramType.getName())) {
	                        try {
	                            params[0] = new Integer(value);
	                        } catch (NumberFormatException ex) {
	                            ok = false;
	                        }
	                    // Try a setFoo ( long )
	                    }else if ("java.lang.Long".equals(paramType.getName())
	                                || "long".equals(paramType.getName())) {
	                            try {
	                                params[0] = new Long(value);
	                            } catch (NumberFormatException ex) {
	                                ok = false;
	                            }

	                        // Try a setFoo ( boolean )
	                    } else if ("java.lang.Boolean".equals(paramType.getName())
	                            || "boolean".equals(paramType.getName())) {
	                        params[0] = new Boolean(value);

	                        // Try a setFoo ( InetAddress )
	                    } else if ("java.net.InetAddress".equals(paramType
	                            .getName())) {
	                        try {
	                            params[0] = InetAddress.getByName(value);
	                        } catch (UnknownHostException exc) {
	                         //   d("Unable to resolve host name:" + value);
	                         //   ok = false;
	                        }

	                        // Unknown type
	                    } else {
	                      //  d("Unknown type " + paramType.getName());
	                    }

	                    if (ok) {
	                        methods[i].invoke(o, params);
	                        return true;
	                    }
	                }

	                // save "setProperty" for later
	                if ("setProperty".equals(methods[i].getName())) {
	                    if (methods[i].getReturnType()==Boolean.TYPE){
	                        setPropertyMethodBool = methods[i];
	                    }else {
	                        setPropertyMethodVoid = methods[i];    
	                    }
	                    
	                }
	            }

	            // Ok, no setXXX found, try a setProperty("name", "value")
	            if (invokeSetProperty && (setPropertyMethodBool != null || setPropertyMethodVoid != null)) {
	                Object params[] = new Object[2];
	                params[0] = name;
	                params[1] = value;
	                if (setPropertyMethodBool != null) {
	                    try {
	                        return (Boolean) setPropertyMethodBool.invoke(o, params);
	                    }catch (IllegalArgumentException biae) {
	                        //the boolean method had the wrong
	                        //parameter types. lets try the other
	                        if (setPropertyMethodVoid!=null) {
	                            setPropertyMethodVoid.invoke(o, params);
	                            return true;
	                        }else {
	                            throw biae;
	                        }
	                    }
	                } else {
	                    setPropertyMethodVoid.invoke(o, params);
	                    return true;
	                }
	            }

	        } catch (Exception e) {
	           
	        }
	        
	        return false;
	    }
	
	
	
	static Hashtable objectMethods = new Hashtable();
	
	public static Method[] findMethods(Class c) {
        Method methods[] = (Method[]) objectMethods.get(c);
        if (methods != null)
            return methods;

        methods = c.getMethods();
        objectMethods.put(c, methods);
        return methods;
    }
	
	
	
	 public static Method findMethod(Class c, String name, Class params[]) {
		 Method methods[] = findMethods(c);
		 if (methods == null)
	            return null;
		 
		 for (int i = 0; i < methods.length; i++) {
			 if (methods[i].getName().equals(name)) {
				 
				 Class methodParams[] = methods[i].getParameterTypes();
				 
				 if (methodParams == null)
					 if (params == null || params.length == 0)
	                        return methods[i];
				 
				 if (params == null)
					 if (methodParams == null || methodParams.length == 0)
	                        return methods[i];
				 
				 if (params.length != methodParams.length)
	                    continue;
				 
				 boolean found = true;
	             for (int j = 0; j < params.length; j++) {
	             	if (params[j] != methodParams[j]) {
	                	found = false;
	                	break;
	             	}
	              }
					 
	              if (found)
	              	return methods[i];	 
			 }
		 }
		 
		 return null;
	 }
	 
	 
	 
	 public static Object callMethod1(Object target, String methodN,
	            Object param1, String typeParam1, ClassLoader cl) throws Exception {
	        if (target == null || param1 == null) {
	            d("Assert: Illegal params " + target + " " + param1);
	        }
	        if (dbg > 0)
	            d("callMethod1 " + target.getClass().getName() + " "
	                    + param1.getClass().getName() + " " + typeParam1);

	        Class params[] = new Class[1];
	        if (typeParam1 == null)
	            params[0] = param1.getClass();
	        else
	            params[0] = cl.loadClass(typeParam1);
	        
	        Method m = findMethod(target.getClass(), methodN, params);
	        if (m == null)
	            throw new NoSuchMethodException(target.getClass().getName() + " "
	                    + methodN);
	        return m.invoke(target, new Object[] { param1 });
	    }
	
	
	public static String capitalize(String name) {
		
		 if (name == null || name.length() == 0) {
	            return name;
	        }
	        char chars[] = name.toCharArray();
	        chars[0] = Character.toUpperCase(chars[0]);
	        return new String(chars);
	}
	
	public static String unCapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
	
	
	public static Object convert(String object, Class paramType) {
        Object result = null;
        if ("java.lang.String".equals(paramType.getName())) {
            result = object;
        } else if ("java.lang.Integer".equals(paramType.getName())
                || "int".equals(paramType.getName())) {
            try {
                result = new Integer(object);
            } catch (NumberFormatException ex) {
            }
            // Try a setFoo ( boolean )
        } else if ("java.lang.Boolean".equals(paramType.getName())
                || "boolean".equals(paramType.getName())) {
            result = new Boolean(object);

            // Try a setFoo ( InetAddress )
        } else if ("java.net.InetAddress".equals(paramType
                .getName())) {
            try {
                result = InetAddress.getByName(object);
            } catch (UnknownHostException exc) {
                d("Unable to resolve host name:" + object);
            }

            // Unknown type
        } else {
            d("Unknown type " + paramType.getName());
        }
        if (result == null) {
            throw new IllegalArgumentException("Can't convert argument: " + object);
        }
        return result;
    }
	
	
	 public static Object callMethodN(Object target, String methodN,
	            Object params[], Class typeParams[]) throws Exception {
	        Method m = null;
	        m = findMethod(target.getClass(), methodN, typeParams);
	        if (m == null) {
	            d("Can't find method " + methodN + " in " + target + " CLASS "
	                    + target.getClass());
	            return null;
	        }
	        Object o = m.invoke(target, params);

	        if (dbg > 0) {
	            // debug
	            StringBuffer sb = new StringBuffer();
	            sb.append("" + target.getClass().getName() + "." + methodN + "( ");
	            for (int i = 0; i < params.length; i++) {
	                if (i > 0)
	                    sb.append(", ");
	                sb.append(params[i]);
	            }
	            sb.append(")");
	            d(sb.toString());
	        }
	        return o;
	    }
	
	
	
	// debug --------------------
    static final int dbg = 0;

    static void d(String s) {
        if (log.isDebugEnabled())
            log.debug("IntrospectionUtils: " + s);
    }
	
}
