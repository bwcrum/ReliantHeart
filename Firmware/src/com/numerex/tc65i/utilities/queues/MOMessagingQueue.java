package com.numerex.tc65i.utilities.queues;

public class MOMessagingQueue extends MessagingQueue {
	private static MOMessagingQueue instance = null;
	
	private MOMessagingQueue() {
		super("moQueue", 1000);
	}

	public static MOMessagingQueue getInstance() {
		if (instance == null) { 
			instance = new MOMessagingQueue(); 
		}
		return instance;	
	}
}
