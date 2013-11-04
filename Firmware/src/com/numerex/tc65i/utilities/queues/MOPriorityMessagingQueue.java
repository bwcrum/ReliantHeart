package com.numerex.tc65i.utilities.queues;

public class MOPriorityMessagingQueue extends MessagingQueue {
	private static MOPriorityMessagingQueue instance = null;
	
	private MOPriorityMessagingQueue() {
		super("moPriorityQueue", 1000);
	}

	public static MOPriorityMessagingQueue getInstance() {
		if (instance == null) instance = new MOPriorityMessagingQueue(); 
		return instance;	
	}
}
