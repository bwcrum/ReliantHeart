package com.numerex.tc65i.utilities.queues;

public class SerialMessagingQueue extends MessagingQueue {
	private static SerialMessagingQueue instance = null;
	private boolean airplaneMode = false;
	
	public void setAirplaneModeOn(boolean mode) {
		airplaneMode = mode;
	}
	
	public boolean getAirplaneMode() {
		return airplaneMode;
	}
	
	private SerialMessagingQueue() {
		super("serialQueue", 1000);
		airplaneMode = false;
	}
	
	public static SerialMessagingQueue getInstance() {
		if (instance == null) {
			instance = new SerialMessagingQueue();
		}
		return instance;	
	}
}
