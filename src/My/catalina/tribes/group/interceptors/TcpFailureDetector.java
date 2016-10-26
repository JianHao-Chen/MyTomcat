package My.catalina.tribes.group.interceptors;

import My.catalina.tribes.group.ChannelInterceptorBase;

/**
 * <p>Title: A perfect failure detector </p>
 *
 * <p>Description: The TcpFailureDetector is a useful interceptor
 * that adds reliability to the membership layer.</p>
 * <p>
 * If the network is busy, or the system is busy so that the membership receiver thread
 * is not getting enough time to update its table, members can be "timed out";
 * This failure detector will intercept the memberDisappeared message(unless its a true shutdown message)
 * and connect to the member using TCP.
 * </p>
 * <p>
 * The TcpFailureDetector works in two ways. <br>
 * 1. It intercepts memberDisappeared events
 * 2. It catches send errors 
 * </p>
 */

public class TcpFailureDetector extends ChannelInterceptorBase{

	private static My.juli.logging.Log log = My.juli.logging.LogFactory.getLog( TcpFailureDetector.class );
	
	protected static byte[] TCP_FAIL_DETECT = new byte[] {
        79, -89, 115, 72, 121, -126, 67, -55, -97, 111, -119, -128, -95, 91, 7, 20,
        125, -39, 82, 91, -21, -15, 67, -102, -73, 126, -66, -113, -127, 103, 30, -74,
        55, 21, -66, -121, 69, 126, 76, -88, -65, 10, 77, 19, 83, 56, 21, 50,
        85, -10, -108, -73, 58, -6, 64, 120, -111, 4, 125, -41, 114, -124, -64, -43};
	
	
}
