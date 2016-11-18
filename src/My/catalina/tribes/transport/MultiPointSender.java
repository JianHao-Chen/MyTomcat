package My.catalina.tribes.transport;

import My.catalina.tribes.ChannelException;
import My.catalina.tribes.ChannelMessage;
import My.catalina.tribes.Member;

public interface MultiPointSender extends DataSender{

	public void sendMessage(Member[] destination, ChannelMessage data) throws ChannelException;
}
