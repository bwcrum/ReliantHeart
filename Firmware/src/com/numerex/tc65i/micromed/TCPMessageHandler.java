package com.numerex.tc65i.micromed;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.SocketConnection;

import com.numerex.tc65i.utilities.OTAMessage.OTAMessageIntf;
import com.numerex.tc65i.utilities.network.servers.TCPStreamHandlerIntf;
import com.numerex.tc65i.utilities.queues.MTMessagingQueue;

public class TCPMessageHandler implements TCPStreamHandlerIntf {

	public void handleStream(SocketConnection socketConnection, String ip, int port, byte[] payload) {
		if (socketConnection == null) {
			System.out.println("TCPMessageHandler socketConnection is null");
			return;
		}
		//DataInputStream dataInputStream = null;
		//DataOutputStream dataOutputStream = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			
			inputStream = socketConnection.openInputStream();
			outputStream = socketConnection.openOutputStream();
			//dataInputStream = socketConnection.openDataInputStream();
			//dataOutputStream = socketConnection.openDataOutputStream();
			
			{
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
				System.out.println("TCPMessageHandler handleStream send length=<" + payload.length + ">, payload=<" + payloadHexString + ">");
			}
			
			//dataOutputStream.write(payload);
			//dataOutputStream.flush();
			/*
			outputStream.write(payload);
			outputStream.flush();
			outputStream.flush();
			outputStream.flush();
			outputStream.flush();
			*/
			
			//try sending the data 1 byte at a time
			/** /
			for (int i = 0; i < payload.length; i++) {
				System.out.println("debug sending byte[" + i + "]=<" + Integer.toHexString(payload[i])+ ">");
				dataOutputStream.writeByte(payload[i]);
				dataOutputStream.flush();
			}
			/**/
			
			//chunk the output stream in 500 byte slices
			/**/
			for (int i = 0; i < payload.length;) {
				int chunkSize = 50;
				if ((payload.length - i) < chunkSize) {
					chunkSize = payload.length - i;
				}
				byte[] chunk = new byte[chunkSize];
				System.arraycopy(payload, i, chunk, 0, chunkSize);
				i += chunkSize;
				System.out.println("	sending chunk[" + chunkSize + "] of [" + i + "][" + payload.length + "]");
				{
					String payloadHexString = "{\r\n";
					for (int j = 0; j < chunk.length; j++) {
						String hexString = Integer.toHexString(chunk[j]);
						if (hexString.length() < 2) hexString = "0" + hexString;
						if (hexString.length() > 2) hexString = hexString.substring(hexString.length() - 2);
						payloadHexString += hexString;
						if (j < chunk.length - 1) payloadHexString += ", ";
						if (j > 0 && j != (chunk.length - 1) && j % 15 == 0) payloadHexString += "\r\n";
					}
					payloadHexString = payloadHexString + "\r\n}";
					System.out.println("TCPMessageHandler handleStream send length=<" + chunk.length + ">, payload=<" + payloadHexString + ">");
				}

				outputStream.write(chunk);
				outputStream.flush();
				
				//try { Thread.sleep(100); } catch (Exception e) {}
			}
			/**/
			
			{
				//OTAMessageIntf recvMessage = OTAMessageIntf.recv(dataInputStream);
				OTAMessageIntf recvMessage = OTAMessageIntf.recv(new DataInputStream(inputStream));
				byte[] recvPayload = recvMessage.getBytes();
				String payloadHexString = "{\r\n";
				for (int j = 0; j < recvPayload.length; j++) {
					String hexString = Integer.toHexString(recvPayload[j]);
					if (hexString.length() < 2) hexString = "0" + hexString;
					if (hexString.length() > 2) hexString = hexString.substring(hexString.length() - 2);
					payloadHexString += hexString;
					if (j < recvPayload.length - 1) payloadHexString += ", ";
					if (j > 0 && j != (recvPayload.length - 1) && j % 15 == 0) payloadHexString += "\r\n";
				}
				payloadHexString = payloadHexString + "\r\n}";
				System.out.println("TCPMessageHandler handleStream recv length=<" + recvPayload.length + ">, payload=<" + payloadHexString + ">");
				MTMessagingQueue.getInstance().add(recvPayload);
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			try {
				//if (dataInputStream != null) {
				//dataInputStream.close();
				//dataInputStream = null;
				//}
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
			try {
				//if (dataOutputStream != null) {
				//	dataOutputStream.close();
				//	dataOutputStream = null;
				//}
				if (outputStream != null) {
					outputStream.close();
					outputStream = null;
				}
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}
}
