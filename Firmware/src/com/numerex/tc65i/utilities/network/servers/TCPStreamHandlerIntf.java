package com.numerex.tc65i.utilities.network.servers;

import javax.microedition.io.SocketConnection;

public interface TCPStreamHandlerIntf {
	public void handleStream(SocketConnection socketConnection, String ip, int port, byte[] payload);
}
