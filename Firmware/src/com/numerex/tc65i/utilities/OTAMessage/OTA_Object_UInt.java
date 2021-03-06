package com.numerex.tc65i.utilities.OTAMessage;

public class OTA_Object_UInt extends OTAObject implements iOTAMessage {
	
	public OTA_Object_UInt(int objectid) throws Exception {
		super(objectid, iOTAMessage.OBJTYPE_UINT);
		resize();
		
	}
	public OTA_Object_UInt(int objectid, int data) throws Exception {
		super(objectid, iOTAMessage.OBJTYPE_UINT);
		setValue(data);
	}
	
    public OTA_Object_UInt(int objectid, short data) throws Exception{
    	super(objectid, iOTAMessage.OBJTYPE_UINT);		
        setValue(data);
    }
	
    public OTA_Object_UInt(int objectid, byte data) throws Exception{
    	super(objectid, iOTAMessage.OBJTYPE_UINT);		
        setValue(data);
    }
    
    public OTA_Object_UInt(int objectid, long data) throws Exception{
    	super(objectid, iOTAMessage.OBJTYPE_UINT);		
        setValue(data);
    }

    public OTA_Object_UInt (byte[] incoming) throws Exception{
    	super(incoming);
    	try {
			intsize = byteBuffer.getByte();
			if(intsize == 0)
				return;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	byte[] payload = new byte[intsize];   	
    	byteBuffer.get(payload);
    	//obj_data = (ByteHelper.chompLong(payload, 0, intsize) & 0xffff);
    	//resize(byteBuffer.position());
    	long l = payload[0];
    	for(int i=1; i<intsize; i++)
    	{
    		l <<= 8;
    		l |= payload[i];
    	}
    	obj_data = l;
    }
  
    
    public void setValue(byte data) throws Exception{ 	
    	
    	obj_data = data;
    	intsize = 1;
    	resize(); 
    	setData(data, true);
    }
    
    public void setValue(short data) throws Exception {
    	
    	/*
    	if(data <= Byte.MAX_VALUE && data >= Byte.MIN_VALUE)
    		setValue((byte)data);
    	else
    	*/
    	{
    		obj_data = data;
			intsize = 2;
			resize();
			setData(data, true);
    	}
    }
    
    public void setValue(int data) throws Exception {
    
    	/*
    	if(data <= Short.MAX_VALUE && data >= Short.MIN_VALUE)
    		setValue((short) data);
    	else
    	*/
    	{
    		obj_data = data;
			intsize = 4;
			resize();
			setData(data, true);  
    	}
    }
    
    public void setValue(long data) throws Exception {
    	/*
    	if(data <= Integer.MAX_VALUE && data >= Integer.MIN_VALUE)
    		setValue((int)data);
    	else
    	*/
    	{
    		obj_data = data;
			intsize = 8;
			resize();
			setData(data, true);
    	}
    }
    
    
    
    public long getObjData(){
    	return obj_data;
    }
    
    
    public String toString() {
		
		StringBuffer msg = new StringBuffer("<Object>\n");
		msg.append("   <ID = \"" + objectID + "\"/>\n");
		msg.append("   <Type = \"" + strObjectType() + "\"/>\n");
		msg.append("   <Payload Size = \"" + intsize + "\"/>\n");
		msg.append("   <Payload = \"" + this.getValue() + "\"/>\n");
		msg.append("   <Obj Size = \"" + size() + "\"/>\n");
		msg.append("   " + OTAObject.bytesToHexString(byteBuffer.array()));
		msg.append("</Object>\n");
		return msg.toString();
		
	}
    
 
    
	 
	public int size() {
		return (2 + 1 + intsize);
		
	} 
	
	 
	public String tag() {
		
		return "OTA_OBJECT_UINT";
	}
		 
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return (intsize == 0)?true:false;
	}
 
	public Object getValue() {
		switch (intsize)
		{
		case 1:
			return new Short((short) (obj_data & 0xFF));
		case 2:
			return new Integer((int) (obj_data & 0xFFFF));
		case 4:
			return new Long((long) (obj_data & 0xFFFFFFFFL));
		case 8:
			/*
			BigInteger TWO_64 = BigInteger.ONE.shiftLeft(64);
			BigInteger b = BigInteger.valueOf(obj_data);
			   if(b.signum() < 0) {
			      b = b.add(TWO_64);
			   }
			return b;
			*/
			break;
		}
		
		return new Long(obj_data);
		
	}
	
	private long obj_data;
    private int intsize;
    
	static public void main (String args[]) {
		
		int objid = 10;
		System.out.println("----- Create empty object -----");
		OTA_Object_UInt obj = null;
		try {
			obj = new OTA_Object_UInt(objid);
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		System.out.println(obj);
		
		 System.out.println("----- Set byte to the object -----");
	     try {
			obj.setValue(-55);
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	     System.out.println(obj);
	     
	     System.out.println("----- Set short to the object -----");
	     try {
			obj.setValue((short)55);
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	     System.out.println(obj);
	     
	     
	     System.out.println("----- Set int to the object -----");
	     try {
			obj.setValue((int)55);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	     System.out.println(obj);
	     
	     System.out.println("---- Set long to the object -----");
	     try {
			obj.setValue((long)55);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     System.out.println(obj);
		
		
		System.out.println("------ Create object with int data -----");
		OTA_Object_UInt obj1;
		try {
			obj1 = new OTA_Object_UInt(objid++, 10);
			System.out.println(obj1);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		System.out.println("----- Create object with byte data -----");
		byte data = 125;
		OTA_Object_UInt obj2 = null;
		try {
			obj2 = new OTA_Object_UInt(objid++, data);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(obj2);
		
		System.out.println("----- Create object with short data -----");
		short data3 = -129;
		OTA_Object_UInt obj3 = null;
		try {
			obj3 = new OTA_Object_UInt(objid++, data3);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(obj3);
		
		System.out.println(" convert to byte then conver back  ");
		try {
			obj2 = new OTA_Object_UInt(obj3.getBytes());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(obj2);
		
		
		
		
		
		System.out.println("----- Create object with long data -----");
		long data4 = -129;
		OTA_Object_UInt obj4;
		try {
			obj4 = new OTA_Object_UInt(objid++, data4);

			System.out.println(obj4);

			System.out.println("----- Constuct object from byte stream -----");
			byte[] data5 = { 100, 1, 2, 123, 124 };
			OTA_Object_UInt obj5 = new OTA_Object_UInt(data5);
			System.out.println(obj5);
			System.out.println("----- Constuct object from byte stream -----");
			byte[] data6 = { 100, 1, 1, 123 };
			OTA_Object_UInt obj6 = new OTA_Object_UInt(data6);
			System.out.println(obj6);
			System.out.println("----- Constuct object from byte stream -----");
			byte[] data7 = { 101, 1, 4, 0, 0, (byte) 0xFF, (byte) 0xFE };
			OTA_Object_UInt obj7 = new OTA_Object_UInt(data7);
			System.out.println(obj7);
	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
}
