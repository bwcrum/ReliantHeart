package com.numerex.tc65i.utilities.queues;

public class Message {
	private byte[] payload = null;
	private int id = -1;
	private long bornDateTime = -1;
	private long ttlMilliSeconds = -1;
	
	public Message(int id, byte[] payload, long ttlMilliseconds) throws Exception {
		this.id = id;
		this.payload = payload;
		this.bornDateTime = System.currentTimeMillis();
		this.ttlMilliSeconds = ttlMilliseconds;
	}
	
	public long getBornDate() { return this.bornDateTime; }
	public long getTTL() { return this.ttlMilliSeconds; }
	public int getId() { return id; }
	public byte[] getPayload() { return payload; }
}
