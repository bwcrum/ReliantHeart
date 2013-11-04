package com.numerex.tc65i.utilities.network.servers;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

public class TCPSendRecv {
	private String serverConnectionProfileString = null;
	public boolean isReady = false;
	private TCPStreamHandlerIntf tcpStreamHandler = null;

	public TCPSendRecv(String APN, String IP, int port, String username, String password, TCPStreamHandlerIntf tcpStreamHandler) {
		this.tcpStreamHandler = tcpStreamHandler;
		serverConnectionProfileString = "datagram://:" + port + ";bearer_type=gprs;access_point=" + APN + ";username=" + username + ";password=" + password +";timeout=6000";
		serverConnectionProfileString = "socket://" + IP + ":" + port + ";bearer_type=gprs;access_point=" + APN + ";username=" + username + ";password=" + password + ";timeout=6000";
	}
	
	public void sendRecv(String ip, int port, byte[] payload) throws Exception {
		SocketConnection socketConnection = null;
		try {
			System.out.println("TCPSendRecv sendRecv serverSocketConnection open connection");
			System.out.println("TCPSendRecv sendRecv serverConnectionProfileString=<" + serverConnectionProfileString + ">");
			socketConnection = (SocketConnection) Connector.open(serverConnectionProfileString);
			
			socketConnection.setSocketOption(SocketConnection.DELAY, 0);
			socketConnection.setSocketOption(SocketConnection.KEEPALIVE, 1);
			socketConnection.setSocketOption(SocketConnection.LINGER, 600);

			if (tcpStreamHandler != null) {
				tcpStreamHandler.handleStream(socketConnection, ip, port, payload);
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			throw e;
		} finally {
			if (socketConnection != null) {
				socketConnection.close();
				socketConnection = null;
			}
		}
	}
}
