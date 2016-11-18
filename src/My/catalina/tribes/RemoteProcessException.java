package My.catalina.tribes;

public class RemoteProcessException extends RuntimeException{
	public RemoteProcessException() {
        super();
    }

    public RemoteProcessException(String message) {
        super(message);
    }

    public RemoteProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteProcessException(Throwable cause) {
        super(cause);
    }
}
