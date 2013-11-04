package com.numerex.tc65i.utilities.queues;

import java.util.Enumeration;
import java.util.Hashtable;

public class MORetryQueue1 {
	private int MAX_RETRY_TABLE_SIZE = 10000;
	
 	private Hashtable retryTable = new Hashtable(1);
 	private static MORetryQueue1 moRetryQueue = null;
 	
	private MORetryQueue1() {}
	
	public static MORetryQueue1 getInstance() throws Exception {
		if (moRetryQueue == null) {
			moRetryQueue = new MORetryQueue1();
		}
		return moRetryQueue;
	}
	
	public int size() throws Exception {
		return retryTable.size();
	}
	
	public void put(Message message)throws Exception {		
		if (retryTable.size() >= this.MAX_RETRY_TABLE_SIZE) {
			System.out.println("MORetryQueue put detected a need to purge a message");
		}
		String id = String.valueOf(message.getId()).trim();
		retryTable.put(id, message);
	}
	
	public Message remove(String messageId) throws Exception {
		return (Message)retryTable.remove(messageId);
	}
	
	public Message get(String messageId) throws Exception {
		return (Message)retryTable.get(messageId);
	}

	public boolean containsKey(String messageId) throws Exception {
		return retryTable.containsKey(messageId);
	}
	
	public boolean containsKey(int messageId) throws Exception {
		return retryTable.containsKey(messageId + "");
	}

	public Message remove(int messageId) throws Exception {
		return (Message)retryTable.remove(messageId + "");
	}
	
	public Message get(int messageId) throws Exception {
		return (Message)retryTable.get(messageId + "");
	}

	public Enumeration keys() throws Exception {
		return retryTable.keys();
	}
}
