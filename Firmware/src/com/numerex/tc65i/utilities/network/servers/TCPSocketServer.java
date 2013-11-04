package com.numerex.tc65i.utilities.network.servers;

import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

import com.numerex.tc65i.utilities.DiagnosticsThread;


/*
 * NOTE... this class is incomplete
 * 
 */

public class TCPSocketServer extends Thread {
	private String serverConnectionProfileString = null;
	public boolean isReady = false;
	private ServerSocketConnection serverSocketConnection = null; 
	private SocketConnection socketConnection = null;
	private TCPStreamHandlerIntf tcpStreamHandler = null;
	
	public TCPSocketServer(String APN, int port, TCPStreamHandlerIntf tcpStreamHandler) {
		this.tcpStreamHandler = tcpStreamHandler;
		serverConnectionProfileString = "socket://:" + port + ";bearer_type=gprs;access_point=" + APN + ";timeout=6000";
	}

	public void run() {
		for (;;) {
			try {
				Thread.yield();
				
				if (DiagnosticsThread.checkGSM() == false) {
					System.out.println("UDPSocketServer run GSM ATTACH not ready");
					DiagnosticsThread.validateGSM();
					Thread.sleep(5000);
					continue;
				}

				if (serverSocketConnection == null || socketConnection == null || !isReady) {
					System.out.println("TCPSocketServer serverSocketConnection open connection");
					System.out.println("TCPSocketServer serverConnectionProfileString=<" + serverConnectionProfileString + ">");
					serverSocketConnection = (ServerSocketConnection)Connector.open(serverConnectionProfileString);
					//TODO:  set socket opts here
					isReady = true;
				}
	
				System.out.println("TCPSocketServer socketServerConnection block recv");
				socketConnection = (SocketConnection)serverSocketConnection.acceptAndOpen();
				if (tcpStreamHandler != null) {
					//tcpStreamHandler.handleStream(socketConnection);
				}
				
				try {
					if (socketConnection != null) {
						socketConnection.close();
					}
					socketConnection = null;
				} catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}
				
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				isReady = false;
				try {
					if (socketConnection != null) {
						socketConnection.close();
					}
					socketConnection = null;

					if (serverSocketConnection == null) {
						serverSocketConnection.close();
					}
					serverSocketConnection = null;
					Thread.sleep(5000);
				} catch (Exception e2) {
					System.out.println(e2);
					e.printStackTrace();
				}
			}
		}
	}
	
	public void send(String ip, int port, byte[] payload) throws Exception {
		try {
			throw new Exception("NOT IMPLEMENTED!!");
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			try {
			} catch (Exception e2) {
				e = e2;
				System.out.println(e2);
				e2.printStackTrace();
			}
			throw e;
		}
	}
}
