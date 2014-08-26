package com.numerex.tc65i.utilities.network.servers;


import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.UDPDatagramConnection;

import com.numerex.tc65i.utilities.queues.MTMessagingQueue;
import com.numerex.tc65i.utilities.DiagnosticsThread;

public class UDPSocketServer extends Thread {
	private String serverConnectionProfileString = null;
	private UDPDatagramConnection udpDatagramConnection = null;
	public boolean isReady = false;
	
	public UDPSocketServer(String APN, int port, String username, String password) {
//		serverConnectionProfileString = "datagram://:" + port + ";bearer_type=gprs;access_point=nmrx10.com.attz;timeout=6000";
//		serverConnectionProfileString = "datagram://:" + port + ";bearer_type=gprs;access_point=numerex.cxn;timeout=6000";
//		serverConnectionProfileString = "datagram://:" + port + ";bearer_type=gprs;access_point=" + APN + ";username=" + username + ";password=" + password +";timeout=6000";
		serverConnectionProfileString = "datagram://:" + port + ";bearer_type=gprs;access_point=" + APN + ";timeout=6000";
//		serverConnectionProfileString = "datagram://:" + port + ";bearer_type=gprs;access_point=" + "nmrx.intl.apn" + ";timeout=6000";
//		serverConnectionProfileString = "datagram://:" + port + ";bearer_type=gprs;access_point=arkessa.com;username=arkessa;password=arkessa;timeout=6000";
	}
	
	public void run() {
		udpDatagramConnection = null;
		
		for (;;) {
			try {
				Thread.yield();

				if (DiagnosticsThread.checkGSM() == false) {
//					System.out.println("UDPSocketServer run GSM ATTACH not ready");
					DiagnosticsThread.validateGSM();
					Thread.sleep(5000);
					continue;
				}

				if (udpDatagramConnection == null || isReady == false) {
//					System.out.println("UDPSocketServer udpDatagramConnection open connection");
//					System.out.println("UDPSocketServer serverConnectionProfileString=<" + serverConnectionProfileString + ">");
					udpDatagramConnection = (UDPDatagramConnection)Connector.open(serverConnectionProfileString);
					isReady = true;
				}
				
				Datagram datagram = udpDatagramConnection.newDatagram(udpDatagramConnection.getMaximumLength());
//				System.out.println("UDPSocketServer udpDatagramConnection block recv");
				udpDatagramConnection.receive(datagram);
				
				if (datagram != null && datagram.getLength() > 0) {
//					System.out.println("UDPSocketServer udpDatagramConnection payload recv");
					int datagramLength = datagram.getLength();
					byte[] datagramPayload = datagram.getData();
					
					byte[] payload = new byte[datagramLength];
					System.arraycopy(datagramPayload, 0, payload, 0, datagramLength);
					
					String payloadHexString = "{\r\n";
					for (int j = 0; j < payload.length; j++) {
						String hexString = Integer.toHexString(payload[j]);
						if (hexString.length() < 2) hexString = "0" + hexString;
						if (hexString.length() > 2) hexString = hexString.substring(hexString.length() - 2);
						payloadHexString += hexString;
						if (j < payload.length - 1) payloadHexString += ", ";
						if (j > 0 && j != (payload.length - 1) && j % 15 == 0) payloadHexString += "\r\n";
					}
					payloadHexString = payloadHexString + "\r\n}";
//					System.out.println("UDPSocketServer udpDatagramConnection recv length=<" + payload.length + ">, payload=<" + payloadHexString + ">");
					MTMessagingQueue.getInstance().add(payload);
				} else {
//					System.out.println("UDPSocketServer udpDatagramConnection recv[0]");
				}
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				isReady = false;
				try {
					if (udpDatagramConnection != null) {
						udpDatagramConnection.close();
						udpDatagramConnection = null;
					}
					Thread.sleep(5000);
				} catch (Exception e2) {
					System.out.println(e2);
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean send(String ip, int port, byte[] payload) throws Exception {
		try {
			if (udpDatagramConnection == null && isReady == false) {
				System.out.println("UDPDatagram send UDPDatagramConnection not ready");
				return false;
			}
			
			if (DiagnosticsThread.checkGSM() == false) {
				System.out.println("UDPDatagram send GSM ATTACH not ready");
				DiagnosticsThread.validateGSM();
				return false;
			}
			//ip = "12.107.219.67";
			String clientConnectionProfileString = "datagram://" + ip + ":" + port + "";
			System.out.println("UDPSocketServer send clientConnectionProfileString=<" + clientConnectionProfileString + ">");
			Datagram datagram = udpDatagramConnection.newDatagram(payload, payload.length, clientConnectionProfileString);
			udpDatagramConnection.send(datagram);

			String payloadHexString = "{\r\n";
			for (int j = 0; j < payload.length; j++) {
				String hexString = Integer.toHexString(payload[j]);
				if (hexString.length() < 2) hexString = "0" + hexString;
				if (hexString.length() > 2) hexString = hexString.substring(hexString.length() - 2);
				payloadHexString += hexString;
				if (j < payload.length - 1) payloadHexString += ", ";
				if (j > 0 && j != (payload.length - 1) && j % 15 == 0) payloadHexString += "\r\n";
			}
			payloadHexString = payloadHexString + "\r\n}";
			System.out.println("UDPSocketServer udpDatagramConnection sent length=<" + payload.length + ">, payload=<" + payloadHexString + ">");

		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			try {
				if (udpDatagramConnection != null) {
					udpDatagramConnection.close();
					udpDatagramConnection = null;
				}
			} catch (Exception e2) {
				e = e2;
				System.out.println(e2);
				e2.printStackTrace();
			}
			throw e;
		}
		return true;
	}
}
