package com.numerex.tc65i.utilities.queues;

public class MTMessagingQueue extends MessagingQueue {
	private static MTMessagingQueue instance = null;
	
	private MTMessagingQueue() { 
		super("mtQueue", 1000); 
	}

	public static MTMessagingQueue getInstance() {
		if (instance == null) {
			instance = new MTMessagingQueue();
		}
		return instance;	
	}
	
	/*
	public void run() {
		System.out.println("MTMessagingQueue start");
		for (;;) {
			try {
				Thread.yield();
				Thread.sleep(1000 * instance.unspoolFrequencySeconds);
				
				//try to chew through any acks and requests as fast as possible
				while (size > 0) {
					DataInputStream payload = new DataInputStream(new ByteArrayInputStream((byte[])remove()));
					OTAMessageIntf msgInf = OTAMessageIntf.recv(payload);
					System.out.println("MTAck type=<" + msgInf.getMessageType() + ">, seqId=<" + msgInf.getSeqId() + ">, timestamp=<" + msgInf.getTimestamp() + ">");
					
					if (msgInf.getMessageType() == iOTAMessage.MOBILE_TERMINATED_ACK) {
						System.out.println("MTMessagingQueue detected MTA");
					} else if (msgInf.getMessageType() == iOTAMessage.MOBILE_TERMINATED_EVENT) {
						System.out.println("MTMessagingQueue detected MTE");
					} else {
						System.out.println("MTMessagingQueue detected unsupported message type");
					}
				}
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}
	*/
}
