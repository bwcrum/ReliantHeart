package com.numerex.tc65i.micromed;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Enumeration;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.numerex.tc65i.utilities.DiagnosticsThread;
import com.numerex.tc65i.utilities.GetRandomizedSequenceId;
import com.numerex.tc65i.utilities.OTAMessage.OTAMessageIntf;
import com.numerex.tc65i.utilities.OTAMessage.OTAMessageMO;
import com.numerex.tc65i.utilities.OTAMessage.OTAMessageMOE;
import com.numerex.tc65i.utilities.OTAMessage.OTA_Object_Byte;
import com.numerex.tc65i.utilities.OTAMessage.OTA_Object_Int;
import com.numerex.tc65i.utilities.OTAMessage.OTA_Object_String;
import com.numerex.tc65i.utilities.OTAMessage.iOTAMessage;
import com.numerex.tc65i.utilities.network.servers.TCPSendRecv;
import com.numerex.tc65i.utilities.network.servers.UDPSocketServer;
import com.numerex.tc65i.utilities.queues.MOMessagingQueue;
import com.numerex.tc65i.utilities.queues.MOPriorityMessagingQueue;
import com.numerex.tc65i.utilities.queues.MORetryQueue1;
import com.numerex.tc65i.utilities.queues.MTMessagingQueue;
import com.numerex.tc65i.utilities.queues.Message;
import com.numerex.tc65i.utilities.queues.SerialMessagingQueue;
import com.numerex.tc65i.utilities.strings.StringHelper;

public class StartUp extends MIDlet {
	
	//TODO:  read this data from the SIM card.
	private int DEFAULT_PORT_UDP = 9010;
	private int DEFAULT_PORT_TCP = 9011;
//	private String DEFAULT_APN = "nmrx10.com.attz"; /*"eagle01";*/ 
	private String DEFAULT_APN = "numerex.cxn"; /*"eagle01";*/ 
	private String DEFAULT_SERVER_IP = "172.29.12.254"; //"12.71.216.53"; //"192.168.11.53";
	private String DEFAULT_USER = "";
	private String DEFAULT_PASS = "";
	
	private MOMessagingQueue moQueue = null;
	private MOPriorityMessagingQueue moPriorityQueue = null;
	private MTMessagingQueue mtQueue = null;
  	private SerialMessagingQueue serialQueue = null;
  	private UDPSocketServer udpSocketServer = null;
	private MORetryQueue1 moRetryQueue = null;
	private SerialReceiverThread serialReceiverThread = null;
	
  	public StartUp() throws Exception {
	    try {
	    	System.out.println("\r\n\r\n\r\n\r\n*******");
	    	System.out.println("StartUp Version <" + DiagnosticsThread.getVersion() + ">");
	    	System.out.println("*******");
	    } catch (Exception e) {
	        System.out.println(e);
	        destroyApp(true);
	    }
	}
	
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
    	System.out.println("**********");
    	System.out.println("destroyApp");
    	System.out.println("**********");
		notifyDestroyed();
	}

	protected void pauseApp() {
	}	
	
	protected void startApp() throws MIDletStateChangeException {
	    try {
	    	moQueue = MOMessagingQueue.getInstance();
	    	moPriorityQueue = MOPriorityMessagingQueue.getInstance();
	    	mtQueue = MTMessagingQueue.getInstance();
	      	serialQueue = SerialMessagingQueue.getInstance();
	    	moRetryQueue = MORetryQueue1.getInstance();
	      	
	    	generateStartUpMessage();  //do this here to eliminate confusion with early alarms
	    	Thread.sleep(30 * 1000);
	    		    	
	    	//start diagnostics thread
	    	new DiagnosticMessageHandler().start();
	    	
			//start checking for messages
	    	int periodicMessageLoopCounter = (60 * 15);  //15 minutes
	    	int diagnosticLoopCounter = (60 * 1);

    		try { 
    			DEFAULT_SERVER_IP = SIM.getIP();
    		} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				DEFAULT_SERVER_IP = null;
    		}
    		
    		try { 
	    		DEFAULT_APN = SIM.getAPN();
    		} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				DEFAULT_APN = null;
    		}
    		
    		try { 
	    		DEFAULT_USER = SIM.getUSER();
    		} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				DEFAULT_USER = "";
    		}
		
    		try { 
	    		DEFAULT_PASS = SIM.getPASS();
    		} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
				DEFAULT_PASS = "";
    		}
		    
	      	//start serial port listener
	    	serialReceiverThread = new SerialReceiverThread();
	    	serialReceiverThread.start();

	    	for (;;) {
	    		try {
	    			Thread.yield();
		    		Thread.sleep(5000);
		    		
		    		DiagnosticsThread.getSystemDateTime();
		    		
		    		//check diagnostics
		    		if (diagnosticLoopCounter < 1) {
			    		//get IP and APN info from the SIM card
			    		try { 
			    			DEFAULT_SERVER_IP = SIM.getIP();
			    		} catch (Exception e) {
							System.out.println(e);
							e.printStackTrace();
							DEFAULT_SERVER_IP = null;
			    		}
			    		
			    		try { 
				    		DEFAULT_APN = SIM.getAPN();
			    		} catch (Exception e) {
							System.out.println(e);
							e.printStackTrace();
							DEFAULT_APN = null;
			    		}
		    		
			    		try { 
				    		DEFAULT_USER = SIM.getUSER();
			    		} catch (Exception e) {
							System.out.println(e);
							e.printStackTrace();
							DEFAULT_USER = "";
			    		}
		    		
			    		try { 
				    		DEFAULT_PASS = SIM.getPASS();
			    		} catch (Exception e) {
							System.out.println(e);
							e.printStackTrace();
							DEFAULT_PASS = "";
			    		}
		    		
		    			diagnosticLoopCounter = 36; //(60 * 3);
			    		handleDiagnostics();
		    		}
		    		diagnosticLoopCounter--;
		    		
		    		if (DEFAULT_SERVER_IP == null || DEFAULT_APN == null) {
		    			System.out.println("Server IP <" + DEFAULT_SERVER_IP + ">, and/or APN <" + DEFAULT_APN + "> are not valid.  Insert SIM and configure.");
//		    			Thread.sleep(5000);
		    			continue;
		    		}
		    		//check udp socket server
					handleUDPSocketServer();

		    		//look at acks
					handleMobileTerminatedMessages();
		    		
					//handle resending retries
					handleRetries();
					
		    		//look at priority messages
		    		handlePriorityMessages();

		    		//process serial event messages
		    		handleSerialMessages();
		    		
		    		//look at periodic messages
		    		if (periodicMessageLoopCounter <= 1) {  //offset account for processing?
		    			periodicMessageLoopCounter = (60 * 15);  //15 minutes
		    			handlePeriodicMessages();
		    		}
		    		periodicMessageLoopCounter--;
				} catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}
		    }
	    } catch (Exception e) {
	    	System.out.println(e);
	    	e.printStackTrace();
	    } finally {
	    }
	} 
	
	private synchronized void generateStartUpMessage() throws Exception {
		System.out.println("\r\n---------------------------------------------");
    	System.out.println("generateStartUpMessage,");
        System.out.println("-----------------------------------------");

        try {
			OTAMessageMO msg = new OTAMessageMOE(0);
			msg.overrideTimestamp(DiagnosticsThread.getSystemDateTime());
			
			String imei = null;
			String iccid = null;
			
			//msg.setSeqId(1);
			msg.setSeqId(GetRandomizedSequenceId.getInstance().getOne());
			//add imei
			imei = DiagnosticsThread.getIMEI();
			if (imei != null && imei.length() > 0) {
				msg.addObject(new OTA_Object_String(0, imei));
			} else {
				throw new Exception("invalid IMEI <" + imei + ">");
			}
			
			//add iccid
			iccid = DiagnosticsThread.getICCID();
			if (iccid != null && iccid.length() > 0) {
				msg.addObject(new OTA_Object_String(2, iccid));
			}
			System.out.println("imei=<" + imei + ">, iccid=<" + iccid + ">");

			//add diagnostics to the payload
			try {
				msg.addObject(new OTA_Object_Int(100, (byte)DiagnosticsThread.checkRSSI()));
				msg.addObject(new OTA_Object_Int(101, DiagnosticsThread.getMOPriorityQueueSize()));
				msg.addObject(new OTA_Object_Int(102, DiagnosticsThread.getMOPeriodicQueueSize()));
				msg.addObject(new OTA_Object_Int(103, DiagnosticsThread.getMORetryQueueSize()));
				msg.addObject(new OTA_Object_Int(104, DiagnosticsThread.getSerialQueueSize()));
				msg.addObject(new OTA_Object_Int(105, 0));
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}

			//add SIM card pump controller values
			try {
				if (SIM.isSIMReady() == true) {
					msg.addObject(new OTA_Object_String(106, SIM.getUnitID()));
					msg.addObject(new OTA_Object_Byte(107, SIM.getGain()));
					msg.addObject(new OTA_Object_Byte(108, SIM.getBalance()));
					msg.addObject(new OTA_Object_Byte(109, SIM.getNormA()));
					msg.addObject(new OTA_Object_Byte(110, SIM.getNormB()));
					msg.addObject(new OTA_Object_Byte(111, (byte)1));
				} else {
					msg.addObject(new OTA_Object_Byte(111, (byte)0));
				}
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		
			Message message = new Message(msg.getSeqId(), msg.getBytes(), (1000 * 60 * 2));
			MOPriorityMessagingQueue.getInstance().add(message);
		} catch (Exception e){
			System.out.println(e);
			e.printStackTrace();
	
		}
	}
	
	private synchronized void handleMobileTerminatedMessages() throws Exception {
		try {
			if (mtQueue.size() > 0) {
		    	System.out.println("\r\n-------------------------------------");
		    	System.out.println("handleMobileTerminatedMessages start,");
		    	System.out.println("	queue size=<" + mtQueue.size() + ">");
		    	
		    	System.out.println("	moRetryQueue size=<" + moRetryQueue.size() + ">");
				Enumeration keys = moRetryQueue.keys();
				while (keys.hasMoreElements()) {
					System.out.println("	moRetryQueue2 key=<" + (String)keys.nextElement() + ">");
				}
		    	System.out.println("-------------------------------------");
			}
			
			while (mtQueue.size() > 0) {
				DataInputStream payload = new DataInputStream(new ByteArrayInputStream((byte[])mtQueue.remove()));
				OTAMessageIntf msgInf = OTAMessageIntf.recv(payload);
				String id = String.valueOf(msgInf.getSeqId()).trim();
				String serverTimeStamp = msgInf.getTimestamp();
				byte messageType = (byte)msgInf.getMessageType();
				
				System.out.println("handleMobileTerminatedMessages MTAck size=<" + (mtQueue.size() + 1) + ">, type=<" + messageType + ">, id=<" + id + ">, timestamp=<" + serverTimeStamp + ">");
				
				//set system time
				try {
					/*
						 server timestamp:  2011-08-29 16:42:42
						 device timestamp:  
						 
						 at+cclk?
							+CCLK: "02/01/07,01:02:07"
					 */
					
					if (serverTimeStamp != null && serverTimeStamp.length() > 5) {
						serverTimeStamp = serverTimeStamp.substring(2);
						serverTimeStamp = StringHelper.stringReplace(serverTimeStamp, "-", "/");
						serverTimeStamp = StringHelper.stringReplace(serverTimeStamp, " ", ",");
						serverTimeStamp = "\"" + serverTimeStamp + "\"";  //must include quotes
						DiagnosticsThread.setSystemDateTime(serverTimeStamp);
					}
				} catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}
				
				if (messageType == iOTAMessage.MOBILE_TERMINATED_ACK) {
					System.out.println("handleMobileTerminatedMessages detected MTA, id=<" + id + ">");
					if (moRetryQueue.containsKey(id)) {
						System.out.println("handleMobileTerminatedMessages found an ACK'd message id=<" + id + ">, removing");
						moRetryQueue.remove(id);
					} else {
						System.out.println("handleMobileTerminatedMessages did NOT find an ACK'd message id=<" + id + ">, retrying");
					}
				} else if (messageType == iOTAMessage.MOBILE_TERMINATED_EVENT) {
					System.out.println("handleMobileTerminatedMessages detected MTE:  Waveform Request");
					serialReceiverThread.requestWaveForm();
					moRetryQueue.remove(id);
					
					//parse the message object for request IDs and event types.
					//for now, it's safe to say any request is a waveform request.
				} else {
					System.out.println("handleMobileTerminatedMessages detected unsupported message type=<" + messageType + ">, removing");
					moRetryQueue.remove(id);
				}
				Thread.yield();
				Thread.sleep(1);
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
 	}
	
	private synchronized void handleDiagnostics() throws Exception {
    	System.out.println("\r\n-----------------------");
    	System.out.println("handleDiagnostics start");
    	System.out.println("-----------------------");
    	DiagnosticsThread.checkFileSystem();
    	DiagnosticsThread.checkFreeMemory();
    	DiagnosticsThread.checkGSM();
    	DiagnosticsThread.checkSIM();
    	DiagnosticsThread.validateGSM();
    	//DiagnosticsThread.getAirplaneMode();
    	DiagnosticsThread.checkRSSI();
    	DiagnosticsThread.checkQueues();
 	}

	private synchronized void send(Message message) throws Exception {
		if (message.getPayload().length > 300) {
			new TCPSendRecv(DEFAULT_APN, DEFAULT_SERVER_IP, DEFAULT_PORT_TCP, DEFAULT_USER, DEFAULT_PASS, new TCPMessageHandler()).sendRecv(DEFAULT_SERVER_IP, DEFAULT_PORT_TCP, message.getPayload());
		} else {
			udpSocketServer.send(DEFAULT_SERVER_IP, DEFAULT_PORT_UDP, message.getPayload());
		}
	}
	
	private synchronized void handlePriorityMessages() throws Exception {	  	
		if (moPriorityQueue.size() > 0) {
			System.out.println("\r\n---------------------------------------------");
	    	System.out.println("handlePriorityMessages start,");
	       	System.out.println("	queue size=<" + moPriorityQueue.size() + ">");
	        System.out.println("-----------------------------------------");
		}
		
		while (moPriorityQueue.size() > 0) {
			Message message = (Message)moPriorityQueue.remove();
			String id = String.valueOf(message.getId()).trim();
			System.out.println("handlePriorityMessages detected PRIORITY message size=<" + (moPriorityQueue.size() + 1) + ">, id=<" + id + ">, unspool, send, and queue");
			moRetryQueue.put(message);
			send(message);
			Thread.yield();
			Thread.sleep(1);
		}
	}
	
	private synchronized void handlePeriodicMessages() throws Exception {
		if (moQueue.size() > 0) {
			System.out.println("\r\n---------------------------------------------");
	    	System.out.println("handlePeriodicMessages start,");
	       	System.out.println("	queue size=<" + moQueue.size() + ">");
	        System.out.println("-----------------------------------------");
		}
		
    	while (moQueue.size() > 0) {
			Message message = (Message)moQueue.remove();
			String id = String.valueOf(message.getId()).trim();
			System.out.println("handlePeriodicMessages detected PERIODIC message size=<" + (moQueue.size() + 1) + ">, id=<" + id + ">, unspool, send, and queue");
			moRetryQueue.put(message);
			send(message);
			Thread.yield();
			Thread.sleep(1);
		}
 	}
	
	private synchronized void handleRetries() throws Exception {
		try {
			if (moRetryQueue.size() > 0) {
			  	System.out.println("\r\n------------------------------------------");
		    	System.out.println("handleRetries start,");
		    	System.out.println("	table size=<" + moRetryQueue.size() + ">");
		    	System.out.println("------------------------------------------");
			}
	    	Enumeration keys = moRetryQueue.keys();
	    	
	    	if (keys == null) return;
	    	
	    	while (keys.hasMoreElements()) {
				String key = (String)keys.nextElement();
				Message message = (Message)moRetryQueue.get(key);
				
				long now = System.currentTimeMillis();
				long retryCheck = message.getBornDate() + message.getTTL();
				
				System.out.println("handleRetryTableExpiry now=<" + now + ">, retryCheck=<" + retryCheck + ">, retryDiff=<" + (now - retryCheck) + /*">, expiryCheck=<" + expiryCheck + ">, expiryDiff=<" + (now - expiryCheck) + */">");
				
				if (retryCheck <= now){
					System.out.println("handleRetries detected RETRY message size=<" + moRetryQueue.size() + ">, id=<" + key + ">");
					send(message);
				} else {
					System.out.println("handleRetries detected RETRY WAIT message size=<" + moRetryQueue.size() + ">, id=<" + key + ">");
				}
				
				Thread.yield();
				Thread.sleep(1);
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	private synchronized void handleSerialMessages() throws Exception {
		try {
//	    		System.out.println("----------------------------------------	serial queue size=<" + serialQueue.size() + ">");
			
			
			if (serialQueue.size() > 0) {
//			  	System.out.println("\r\n-----------------------------------------");
//		    	System.out.println("handleSerialMessages start,");
//		    	System.out.println("	queue size=<" + serialQueue.size() + ">");
//		    	System.out.println("-----------------------------------------");
			}

//			System.out.println("Detected airplane mode <" + (serialQueue.getAirplaneMode() == true? "ON" : "OFF") + ">");
			if (serialQueue.getAirplaneMode() == true) {
//				System.out.println("Detected airplane mode.  Holding queue.");
				return;
			}
			
	    	while (serialQueue.size() > 0) {
	    		SerialDataObjectMicroMed sdomm = (SerialDataObjectMicroMed) SerialMessagingQueue.getInstance().remove();
				int priority = sdomm.getPriority();					
				int alarmMask = sdomm.getAlarmMask();
				int statusMask = sdomm.getStatusMask();
				int numerexStatusMask = sdomm.getNumerexStatusMask();
			
				float batteryAlarm = sdomm.getBatteryAlarm();
				float batteryAvg = sdomm.getBatteryAvg();
				float batteryMax = sdomm.getBatteryMax();
				float batteryMin = sdomm.getBatteryMin();
				
				float flowAlarm = sdomm.getFlowAlarm();
				float flowAvg = sdomm.getFlowAvg();
				float flowMax = sdomm.getFlowMax();
				float flowMin = sdomm.getFlowMin();
				
				float powerAlarm = sdomm.getPowerAlarm();
				float powerAvg = sdomm.getPowerAvg();
				float powerMax = sdomm.getPowerMax();
				float powerMin = sdomm.getPowerMin();
				
				float speedAlarm = sdomm.getSpeedAlarm();
				float speedAvg = sdomm.getSpeedAvg();
				float speedMax = sdomm.getSpeedMax();
				float speedMin = sdomm.getSpeedMin();
				
				String pumpId = sdomm.getPumpId();
				String patientId = sdomm.getPatientId();
				String hospitalId = sdomm.getHospitalId();
				String implantDate = sdomm.getImplantDate();
				String alarmMaskString = sdomm.getAlarmMaskStr();
				float[] waveform = sdomm.getWaveform();
				
				int eventType = sdomm.getEventType();
				
				OTAMessageIntf otaMessage = SerialDataObjectConverter.getInstance().createMOE(
						eventType, 
						numerexStatusMask, 
						alarmMask, 
						statusMask, 
						batteryAlarm, 
						batteryAvg, 
						batteryMax, 
						batteryMin, 
						flowAlarm, 
						flowAvg, 
						flowMax, 
						flowMin, 
						powerAlarm, 
						powerAvg, 
						powerMax, 
						powerMin, 
						speedAlarm, 
						speedAvg, 
						speedMax, 
						speedMin, 
						pumpId,
						patientId,
						hospitalId,
						implantDate,
						alarmMaskString,
						waveform);
				Message message = new Message(otaMessage.getSeqId(), otaMessage.getBytes(), (1000 * 60 * 2));
				//if (priority < 1) {
				//	MOMessagingQueue.getInstance().add(message);
				//} else {
					MOPriorityMessagingQueue.getInstance().add(message);
				//}
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	} 
	
	private synchronized void handleUDPSocketServer() throws Exception {
		try {
			if (udpSocketServer == null || !udpSocketServer.isAlive()) {
				System.out.println("\r\n----------------------------");
		    	System.out.println("handleUDPSocketServer, start");
		    	System.out.println("----------------------------");
		    	udpSocketServer = new UDPSocketServer(DEFAULT_APN, DEFAULT_PORT_UDP, DEFAULT_USER, DEFAULT_PASS);
		    	udpSocketServer.start();
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			udpSocketServer = null;
		}
	}
	
	public class DiagnosticMessageHandler extends Thread {
		private synchronized void generateDiagnosticMessage() throws Exception {
			System.out.println("\r\n---------------------------------------------");
	    	System.out.println("generateDiagnosticMessage,");
	        System.out.println("-----------------------------------------");
	        
	        try {
				OTAMessageMO msg = new OTAMessageMOE(4);
				String imei = null;
				String iccid = null;
				
				msg.setSeqId(GetRandomizedSequenceId.getInstance().getOne());
				//msg.setSeqId(1);
				
				//add imei
				imei = DiagnosticsThread.getIMEI();
				if (imei != null && imei.length() > 0) {
					msg.addObject(new OTA_Object_String(0, imei));
				} else {
					throw new Exception("invalid IMEI <" + imei + ">");
				}
				
				//add iccid
				iccid = DiagnosticsThread.getICCID();
				if (iccid != null && iccid.length() > 0) {
					msg.addObject(new OTA_Object_String(2, iccid));
				}
				System.out.println("imei=<" + imei + ">, iccid=<" + iccid + ">");

				//add diagnostics to the payload
				try {
					msg.addObject(new OTA_Object_String(10, DiagnosticsThread.getVersion()));
					msg.addObject(new OTA_Object_Int(100, (byte)DiagnosticsThread.checkRSSI()));
					msg.addObject(new OTA_Object_Int(101, DiagnosticsThread.getMOPriorityQueueSize()));
					msg.addObject(new OTA_Object_Int(102, DiagnosticsThread.getMOPeriodicQueueSize()));
					msg.addObject(new OTA_Object_Int(103, DiagnosticsThread.getMORetryQueueSize()));
					msg.addObject(new OTA_Object_Int(104, DiagnosticsThread.getSerialQueueSize()));
					msg.addObject(new OTA_Object_Int(105, 0));
				} catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}

				//add SIM card pump controller values
				try {
					if (SIM.isSIMReady() == true) {
						String pumpId = SIM.getUnitID();
						byte gain = SIM.getGain();
						byte balance = SIM.getBalance();
						byte normA = SIM.getNormA();
						byte normB = SIM.getNormB();
						
						msg.addObject(new OTA_Object_String(106, pumpId));
						msg.addObject(new OTA_Object_Byte(107, SIM.getGain()));
						msg.addObject(new OTA_Object_Byte(108, SIM.getBalance()));
						msg.addObject(new OTA_Object_Byte(109, SIM.getNormA()));
						msg.addObject(new OTA_Object_Byte(110, SIM.getNormB()));
						msg.addObject(new OTA_Object_Byte(111, (byte)1));
					} else {
						msg.addObject(new OTA_Object_Byte(111, (byte)0));
					}
				} catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}

				//translator.object.map.3=event_code
//				msg.addObject(new OTA_Object_Int(3, 4));
//				msg.addObject(new OTA_Object_Int(4, 4));

				Message message = new Message(msg.getSeqId(), msg.getBytes(), (1000 * 60 * 2));
				MOPriorityMessagingQueue.getInstance().add(message);
			} catch (Exception e){
				System.out.println(e);
				e.printStackTrace();
		
			}
		}

		public void run() {
			try {
				Thread.yield();
				Thread.sleep(1000 * 60 * 1440);
				generateDiagnosticMessage();
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}
	
}
