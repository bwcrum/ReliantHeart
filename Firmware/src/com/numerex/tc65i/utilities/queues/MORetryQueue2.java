package com.numerex.tc65i.utilities.queues;

import java.util.Enumeration;
import java.util.Vector;

public class MORetryQueue2 extends MessagingQueue {
	private static MORetryQueue2 instance = null;
	
	private MORetryQueue2() {
		super("moRetryQueue", 10000);
	}

	public static MORetryQueue2 getInstance() {
		if (instance == null) instance = new MORetryQueue2(); 
		return instance;	
	}
	
	public synchronized int put(Message message) throws Exception {
		int returnHead = add(message);
		notifyAll();
		return returnHead;
	}
	
	public synchronized Message get(String key) throws Exception {
		Message message = null;
		int startPosition = (size - head) < 0 ? 0 : (size - head); 
		int queueSize = size + startPosition;
		System.out.println("		get startPosition=<" + startPosition + ">, size=<" + size + ">");
		
		if (queue != null) {
			for (int i = startPosition; i < queueSize; i++) {
				message = (Message)queue[i];
				if ((message.getId() + "").equals(key))
					break;
				else message = null;
			}
		}
		resetPositions();
		notifyAll();
		return message;
	}
	
	public synchronized void remove(String key) throws Exception {
		Message message = null;
		int startPosition = (size - head) < 0 ? 0 : (size - head); 
		int queueSize = size + startPosition;
		System.out.println("		remove startPosition=<" + startPosition + ">, queueSize=<" + queueSize + ">");
		
		if (queue != null) {
			for (int i = startPosition; i < queueSize; i++) {
				message = (Message)queue[i];
				if (message != null && (message.getId() + "").equals(key)) {
					System.out.println("	MORetryQueue2 remove <" + key + ">");
					queue[i] = null;
					for (int j = i; j < (queueSize - 1); j++) {
						queue[j] = queue[j + 1];
						System.out.println("	MORetryQueue2 cullNulls move <" + (j + 1) + "> to <" + j + ">");
					}
					tail = (tail + 1) % capacity;
					size--;
					System.out.println("	MORetryQueue2 cullNulls reducing size by 1 to <" + size + ">");
					break;
				} else {
					System.out.println("	MORetryQueue2 cullNulls skip <" + i + ">");
				}
			}
		}
		resetPositions();
		notifyAll();
	}
	
	public synchronized boolean containsKey(String key) throws Exception {
		boolean found = false;
		int startPosition = (size - head) < 0 ? 0 : (size - head); 
		int queueSize = size + startPosition;
		System.out.println("		containsKey startPosition=<" + startPosition + ">, queueSize=<" + queueSize + ">");

		if (queue != null) {
			for (int i = startPosition; i < queueSize; i++) {
				Message message = (Message)queue[i];
				if (message != null) {
					String keyCheck = String.valueOf(message.getId());
					if (keyCheck.equals(key) == true) {
						found = true;
						break;
					}
				} else {
					System.out.println("MORetryQueue2 containsKey size=<" + size + ">, detected a null message at <" + i + ">");
				}
			}
		}
		resetPositions();
		notifyAll();
		return found;
	}
	
	public synchronized Enumeration keys() throws Exception {
		Vector keysV = new Vector();
		int startPosition = (size - head) < 0 ? 0 : (size - head); 
		int queueSize = size + startPosition;
		System.out.println("		keys startPosition=<" + startPosition + ">, queueSize=<" + queueSize + ">");

		if (queue != null) {
			for (int i = startPosition; i < queueSize ; i++) {
				Message message = (Message)queue[i];
				if (message != null) {
					keysV.addElement(message.getId() + "");
				} else {
					System.out.println("found a null element at <" + i + ">");
				}
			}
		}
		resetPositions();
		notifyAll();
		return keysV.elements();
	}
	
	public static void main(String[] args) throws Exception {
		MORetryQueue2 moRetryQueue2 = MORetryQueue2.getInstance();
		
		/**/
		//add data set
		System.out.println("\r\n****ADD DATA SET");
		for (int i = 1; i <= 10; i++) {
			Message message = new Message(i, ("this is payload <" + i + ">").getBytes(), (long)(300 * 1000));
			int head = moRetryQueue2.put(message);
			System.out.println("moRetryQueue2 add id=<" + i + ">, size=<" + moRetryQueue2.size() + ">, to index=<" + head + ">");
		}
		
		//inspect data set object ids
		System.out.println("\r\n****INSPECT DATA SET");
		{
			Enumeration keys = moRetryQueue2.keys();
			while (keys.hasMoreElements()) {
				System.out.println("moRetryQueue2 has key=<" + (String)keys.nextElement() + ">");
			}
		}
		
		//remove some of the data set
		System.out.println("\r\n****REMOVE 1/2 DATA SET");
		for (int i = 1; i < 10; i += 2) {
			if (moRetryQueue2.containsKey((i + ""))) {
				moRetryQueue2.remove((i + ""));
				System.out.println("moRetryQueue2 remove id=<" + i + ">, size=<" + moRetryQueue2.size() + ">");
			} else {
				System.out.println("moRetryQueue2 did not remove id=<" + i + ">, size=<" + moRetryQueue2.size() + ">");
			}
		}
		
		//get some of the data set
		System.out.println("\r\n****GET DATA SET");
		for (int i = 1; i < 10; i++) {
			if (moRetryQueue2.containsKey((i + ""))) {
				moRetryQueue2.get((i + ""));
				System.out.println("moRetryQueue2 get id=<" + i + ">, size=<" + moRetryQueue2.size() + ">");
			} else {
				System.out.println("moRetryQueue2 did not get id=<" + i + ">, size=<" + moRetryQueue2.size() + ">");
			}
		}

		//inspect data set object ids
		System.out.println("\r\n****INSPECT DATA SET");
		{
			Enumeration keys = moRetryQueue2.keys();
			while (keys.hasMoreElements()) {
				System.out.println("moRetryQueue2 has key=<" + (String)keys.nextElement() + ">");
			}
		}
		
		//remove the rest of the data set
		{
			System.out.println("\r\n****CLEAN THE DATA SET");
			Enumeration keys = moRetryQueue2.keys();
			while (keys.hasMoreElements()) {
				String key = (String)keys.nextElement();
				System.out.println("moRetryQueue2 has key=<" + key + ">, remove it");
				moRetryQueue2.remove(key);
			}
		}
		
		//get keys on an empty set
		System.out.println("\r\n****INSPECT DATA SET");
		{
			Enumeration keys = moRetryQueue2.keys();
			while (keys.hasMoreElements()) {
				System.out.println("moRetryQueue2 has empty set key=<" + (String)keys.nextElement() + ">");
			}
		}
		/**/
		
		/**/
		//add one delete one add one delete one
		for (int i = 0; i < 5; i++) {
			Message message1 = new Message(i, ("this is payload <" + i + ">").getBytes(), (long)(300 * 1000));
			int return1 = moRetryQueue2.put(message1);
			System.out.println("moRetryQueue2 add id=<" + i + ">, size=<" + moRetryQueue2.size() + ">, to index=<" + return1 + ">");

			//if (i % 2 == 0) {
				Message message2 = new Message((i + 11), ("this is payload <" + i + ">").getBytes(), (long)(300 * 1000));
				int return2 = moRetryQueue2.put(message2);
				System.out.println("moRetryQueue2 add id=<" + i + ">, size=<" + moRetryQueue2.size() + ">, to index=<" + return2 + ">");

				//}
			
			System.out.println("	\r\n*** moRetryQueue2 add id=<" + i + ">, size=<" + moRetryQueue2.size() + ">");

			if (moRetryQueue2.containsKey((i + ""))) {
				moRetryQueue2.get((i + ""));
				System.out.println("	moRetryQueue2 get id=<" + i + ">, size=<" + moRetryQueue2.size() + ">");
			} else {
				System.out.println("	moRetryQueue2 did not get id=<" + i + ">, size=<" + moRetryQueue2.size() + ">");
			}

			if (moRetryQueue2.containsKey((i + ""))) {
				moRetryQueue2.remove((i + ""));
				System.out.println("	moRetryQueue2 remove id=<" + i + ">, size=<" + moRetryQueue2.size() + ">");
			} else {
				System.out.println("	moRetryQueue2 did not remove id=<" + i + ">, size=<" + moRetryQueue2.size() + ">");
			}			
		}/**/
		
		//get keys on an empty set
		System.out.println("\r\n****INSPECT DATA SET");
		{
			Enumeration keys = moRetryQueue2.keys();
			while (keys.hasMoreElements()) {
				System.out.println("moRetryQueue2 has empty set key=<" + (String)keys.nextElement() + ">");
			}
		}
		/**/

		/*
		MessagingQueue mq = new MessagingQueue("queue", 5);
		mq.add("new string1");
		mq.remove();
		mq.add("new string2");
		mq.remove();
		mq.add("new string3");
		mq.remove();
		mq.add("new string4");
		mq.remove();
		mq.add("new string5");
		mq.remove();
		mq.add("new string6");
		mq.remove();
		*/
	}
}
