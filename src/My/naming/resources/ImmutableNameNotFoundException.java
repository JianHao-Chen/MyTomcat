package My.naming.resources;

import javax.naming.Name;
import javax.naming.NameNotFoundException;

/**
 * Immutable exception to avoid useless object creation by the proxy context.
 * This should be used only by the proxy context. Actual contexts should return
 * properly populated exceptions.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1055461 $
 */
public class ImmutableNameNotFoundException
    extends NameNotFoundException {

    public void appendRemainingComponent(String name) {}
    public void appendRemainingName(Name name) {}
    public void setRemainingName(Name name) {}
    public void setResolverName(Name name) {}
    public void setRootCause(Throwable e) {}

    @Override
    public synchronized Throwable fillInStackTrace() {
        // This class does not provide a stack trace
        return this;
    }
}
