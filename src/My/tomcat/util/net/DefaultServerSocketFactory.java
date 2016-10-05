package My.tomcat.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class DefaultServerSocketFactory extends ServerSocketFactory{

	
	DefaultServerSocketFactory () {
        /* NOTHING */
    }
	
	
	public ServerSocket createSocket (int port)
    throws IOException {
        return  new ServerSocket (port);
    }
	
	
	public ServerSocket createSocket (int port, int backlog)
    throws IOException {
        return new ServerSocket (port, backlog);
    }
	
	
	public ServerSocket createSocket (int port, int backlog,
	        InetAddress ifAddress)
	    throws IOException {
	        return new ServerSocket (port, backlog, ifAddress);
	    }
	 
	public Socket acceptSocket(ServerSocket socket)
	 	throws IOException {
	 	return socket.accept();
	}
	
	
	 public void handshake(Socket sock)
	 	throws IOException {
	 	; // NOOP
	 }
}
