package My.catalina;

/**
* An <b>Engine</b> is a Container that represents the entire Catalina servlet
* engine.  It is useful in the following types of scenarios:
* <ul>
 * <li>You wish to use Interceptors that see every single request processed
 *     by the entire engine.
 * <li>You wish to run Catalina in with a standalone HTTP connector, but still
 *     want support for multiple virtual hosts.
 * </ul>
 * 
 * 
 * 
*/
	

public interface Engine extends Container{

}
