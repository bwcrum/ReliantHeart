package com.numerex.tc65i.utilities.queues;

public class MessagingQueue {
	protected Object[] queue = null;
	protected String name = null;
	protected int capacity = -1;
	protected int size = -1;
	protected int head = -1;
	protected int tail = -1;
	
	public MessagingQueue(String name, int capacity) {
		this.capacity = (capacity > 0) ? capacity : 1;
		queue = new Object[capacity];
		head = 0;
		tail = 0;
		size = 0;
		this.name = name;
	}

	public void reset() {
		queue = new Object[capacity];
		head = 0;
		tail = 0;
		size = 0;
	}
	
	protected synchronized void resetPositions() {
		if (size <= 0) {
			System.out.println("	resetPositions reset all to 0");
			head = 0;
			tail = 0;
			size = 0;
		}
	}
	
	public synchronized String name() {
		return name;
	}
	
	public synchronized int size() {
		return size;
	}

	public synchronized int capacity() {
		return capacity;
	}

	public synchronized boolean full() {
		return (size == capacity);
	}

	public synchronized int add(Object obj) throws Exception {
		if (full()) {
			System.out.println("MessagingQueue add discarding oldest");
			remove();
		}
		int returnHead = head;
		queue[head] = obj;
		head = (head + 1) % capacity;
		size++;
		//resetPositions();
		notifyAll();
		return returnHead;
	}

	public synchronized Object remove() throws Exception {
		if (size == 0) {
			return null;
		}	
		
		Object obj = queue[tail];
		queue[tail] = null;
		tail = (tail + 1) % capacity;
		size--;
		//resetPositions();
		notifyAll();
		return obj;
	}
	
	public synchronized Object get() throws Exception {
		if (size == 0) {
			return null;
		}	
		Object obj = queue[tail];
		//resetPositions();
		notifyAll();
		return obj;
	}
}
