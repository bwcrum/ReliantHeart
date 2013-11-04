package com.numerex.tc65i.utilities;

import java.io.InputStream;
import java.util.Enumeration;

import javax.microedition.io.Connector;

import com.numerex.tc65i.utilities.at.ATCommands;
import com.numerex.tc65i.utilities.network.gsm.GSMHelper;
import com.numerex.tc65i.utilities.queues.MOMessagingQueue;
import com.numerex.tc65i.utilities.queues.MOPriorityMessagingQueue;
import com.numerex.tc65i.utilities.queues.MORetryQueue1;
import com.numerex.tc65i.utilities.queues.MTMessagingQueue;
import com.numerex.tc65i.utilities.queues.MessagingQueue;
import com.numerex.tc65i.utilities.queues.SerialMessagingQueue;
import com.numerex.tc65i.utilities.strings.StringHelper;
import com.siemens.icm.io.file.FileConnection;

public class DiagnosticsThread {
	
	public static String getVersion() throws Exception {
		String version = "9.9.9";
		try {
			FileConnection fileConnection = (FileConnection) Connector.open("file:///a:/ConsolidatedMicromed.jad");
			long fileSize = fileConnection.fileSize();
			byte[] data = new byte[(int)fileSize];
			InputStream is = fileConnection.openInputStream();
			is.read(data);
			String sData = new String(data);
			is.close();
			is = null;
			fileConnection.close();
			fileConnection = null;
			int startIndex = 0;
			int endIndex = 0;
			startIndex = sData.indexOf("FirmwareVersion:") + "FirmwareVersion:".length();
			sData = sData.substring(startIndex);
			endIndex = sData.indexOf("\r\n");
			version = sData.substring(0, endIndex).trim();
			//version = "1.0.3";
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return version;
	}

	public static void setAirplaneMode(boolean TF) throws Exception {
		try {
			ATCommands atCommands = ATCommands.getInstance();
			if(TF)
			{
				String airSetOn = "AT^SCFG=\"MEopMode/Airplane\",\"on\"";
		    	String airRecv = atCommands.sendRecv(airSetOn);
		    	String airExpect = "OK";
		    	System.out.println("AT send=<" + airSetOn + ">, AT recv=<" + airRecv + ">, expect=<" + airExpect + ">");
		    	if (airRecv == null || airRecv.toUpperCase().indexOf("E") > -1) {
		    		System.out.println("error retrieving air mode");
				} else {
		    		System.out.println("Air mode sent OK");
				}
			}
			else
			{
				String airSetOff = "AT^SCFG=\"MEopMode/Airplane\",\"off\"";
		    	String airRecv = atCommands.sendRecv(airSetOff);
		    	String airExpect = "OK";
		    	System.out.println("AT send=<" + airSetOff + ">, AT recv=<" + airRecv + ">, expect=<" + airExpect + ">");
		    	if (airRecv == null || airRecv.toUpperCase().indexOf("E") > -1) {
		    		System.out.println("error retrieving air mode");
				} else {
		    		System.out.println("Air mode sent OK");
				}
			}	
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	public static String getAirplaneMode() throws Exception {
		String iccid = null;
		try {
			ATCommands atCommands = ATCommands.getInstance();
	    	String send = "AT^SCFG?";
	    	String recv = atCommands.sendRecv(send);
	    	System.out.println("AT send=<" + send + ">, AT recv=\r\n<" + recv + "\r\n>");
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return iccid;
	}
	
	public static String getICCID() throws Exception {
		String iccid = null;
		try {
			ATCommands atCommands = ATCommands.getInstance();
	    	String iccidChk = "AT^SCID";
	    	String iccidRecv = atCommands.sendRecv(iccidChk);
	    	String iccidExpect = "^SCID: ";
	    	System.out.println("AT send=<" + iccidChk + ">, AT recv=<" + iccidRecv + ">, expect=<" + iccidExpect + ">");
	    	if (iccidRecv == null || iccidRecv.toUpperCase().indexOf("E") > -1) {
	    		System.out.println("error retrieving imei");
			} else {
				iccidRecv = StringHelper.stringReplace(iccidRecv, iccidChk, "");
				iccidRecv = StringHelper.stringReplace(iccidRecv, iccidExpect, "");
				iccidRecv = StringHelper.stringReplace(iccidRecv, "\r", "");
				iccidRecv = StringHelper.stringReplace(iccidRecv, "\n", "");
				iccidRecv = StringHelper.stringReplace(iccidRecv, "OK", "");
				iccidRecv = StringHelper.stringReplace(iccidRecv, " ", "");
				iccid = iccidRecv.trim();
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return iccid;
	}
	
	public static String getIMEI() throws Exception {
		String imei = null;
		try {
			ATCommands atCommands = ATCommands.getInstance();
	    	String imeiChk = "AT+CGSN";
	    	String imeiRecv = atCommands.sendRecv(imeiChk);
	    	String imeiExpect = "AT+CGSN";
	    	System.out.println("AT send=<" + imeiChk + ">, AT recv=<" + imeiRecv + ">, expect=<" + imeiExpect + ">");
	    	if (imeiRecv == null || imeiRecv.toUpperCase().indexOf("E") > -1) {
	    		System.out.println("error retrieving imei");
			} else {
				imeiRecv = StringHelper.stringReplace(imeiRecv, imeiChk, "");
				imeiRecv = StringHelper.stringReplace(imeiRecv, imeiExpect, "");
				imeiRecv = StringHelper.stringReplace(imeiRecv, "\r", "");
				imeiRecv = StringHelper.stringReplace(imeiRecv, "\r", "");
				imeiRecv = StringHelper.stringReplace(imeiRecv, "OK", "");
				imeiRecv = StringHelper.stringReplace(imeiRecv, " ", "");
				imei = imeiRecv.trim();
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return imei;
	}
	public static String setSystemDateTime(String timestamp) throws Exception {
		String dateTime = null;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("Diagnostics:  setSystemDateTime");
	    	System.out.println("-------------------------------");
			
	    	ATCommands atCommands = ATCommands.getInstance();
	    	String clkChkSend = "at+cclk=" + timestamp;
	    	String clkChkRecv = atCommands.sendRecv(clkChkSend);
	    	String expect = "+CCLK:";
	    	System.out.println("AT send=<" + clkChkSend + ">, AT recv=<" + clkChkRecv + ">, expect=<" + expect + ">");

	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, expect, "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "AT+CCLK", "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "at+cclk", "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "\r", "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "\n", "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "\"", "");
	    	clkChkRecv = clkChkRecv.trim();
	    	
	    	dateTime = clkChkRecv;
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return dateTime;
	}
	
	public static String getSystemDateTime() throws Exception {
		String dateTime = null;
		try {
//	    	System.out.println("-------------------------------");
//	    	System.out.println("Diagnostics:  getSystemDateTime");
//	    	System.out.println("-------------------------------");
			
	    	ATCommands atCommands = ATCommands.getInstance();
	    	String clkChkSend = "at+cclk?";
	    	String clkChkRecv = atCommands.sendRecv(clkChkSend);
	    	String expect = "+CCLK:";
	    	System.out.println("AT send=<" + clkChkSend + ">, AT recv=<" + clkChkRecv + ">, expect=<" + expect + ">");

	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, expect, "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "AT+CCLK?", "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "at+cclk?", "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "\r", "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "\n", "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "\"", "");
	    	clkChkRecv = StringHelper.stringReplace(clkChkRecv, "OK", "");
	    	clkChkRecv = clkChkRecv.trim();
	    	
	    	dateTime = clkChkRecv;
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return dateTime;
	}
	
	public static void validateGSM() throws Exception {
		try {
	    	System.out.println("-------------------------");
	    	System.out.println("Diagnostics:  validateGSM");
	    	System.out.println("-------------------------");

	    	ATCommands atCommands = ATCommands.getInstance();
	    	String gsmChkSend = "at+creg?";
	    	String gsmChkRecv = atCommands.sendRecv(gsmChkSend);
	    	String expect = "+CREG: 1,";
	    	System.out.println("AT send=<" + gsmChkSend + ">, AT recv=<" + gsmChkRecv + ">, expect=<" + expect + ">");
	    	
	    	if (gsmChkRecv != null && gsmChkRecv.indexOf(expect) < 0) {
	    		String gsmSetSend = "at+creg=1";
		    	String gsmSetRecv = atCommands.sendRecv(gsmSetSend);
		    	System.out.println("AT send=<" + gsmSetSend + ">, AT recv=<" + gsmSetRecv + ">");
	    	}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	public static boolean checkGSM() throws Exception {
		boolean state = false;
		try {
	    	System.out.println("----------------------");
	    	System.out.println("Diagnostics:  checkGSM");
	    	System.out.println("----------------------");

	    	ATCommands atCommands = ATCommands.getInstance();
	    	String gsmChkSend = "at+creg?";
	    	String gsmChkRecv = atCommands.sendRecv(gsmChkSend);
	    	String expect = "+CREG: 1,";
	    	System.out.println("AT send=<" + gsmChkSend + ">, AT recv=<" + gsmChkRecv + ">, expect=<" + expect + ">");
	    	
	    	if (gsmChkRecv != null && gsmChkRecv.indexOf(expect) > -1) {
	    		state = true;
	    	} 
	    	System.out.println("checkGSM state=<" + state + ">");
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		} 
		return state;
	}
	
	public static int checkRSSI() throws Exception {
		int rssi = -1;
		try {
	    	System.out.println("-----------------------");
	    	System.out.println("Diagnostics:  checkRSSI");
	    	System.out.println("-----------------------");

	    	ATCommands atCommands = ATCommands.getInstance();
	    	String gsmRSSIChk = "at+csq";
	    	String gsmRSSIRecv = atCommands.sendRecv(gsmRSSIChk);

	    	if (gsmRSSIRecv != null) {
	    		try {
	    			String csq = gsmRSSIRecv;
					int index = csq.indexOf(": ") + 2;
					csq = csq.substring(index);
					index = csq.indexOf(",");
					csq = csq.substring(0, index);
					rssi = GSMHelper.rssiToDbm(Integer.parseInt(csq));
	    		}  catch (Exception e) {
	    			System.out.println(e);
	    			e.printStackTrace();
	    		}
	    	}
	    	System.out.println("AT send=<" + gsmRSSIChk + ">, AT recv=<" + gsmRSSIRecv + ">, rssi=<" + rssi + ">");
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return rssi;
	}

	public static long checkFreeMemory() throws Exception {
		long freeMem = -1;
		try {
	    	System.out.println("-----------------------------");
	    	System.out.println("Diagnostics:  checkFreeMemory");
	    	System.out.println("-----------------------------");

			freeMem = Runtime.getRuntime().freeMemory();
			System.out.println("Free Memory=<" + freeMem + ">");
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return freeMem;
	}

	public static void checkFileSystem() throws Exception {
		try {
	    	System.out.println("-----------------------------");
	    	System.out.println("Diagnostics:  checkFileSystem");
	    	System.out.println("-----------------------------");

			FileConnection fileConnection = (FileConnection) Connector.open("file:///a:/");
			Enumeration fileList = fileConnection.list("*", true);
			System.out.println("Files=<");
			while (fileList != null && fileList.hasMoreElements()) {
				System.out.println("\t- " + fileList.nextElement());
			}
			System.out.println(">");
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	public static int getMOPriorityQueueSize() throws Exception {
		return MOPriorityMessagingQueue.getInstance().size();
	}
	
	public static int getMOPeriodicQueueSize() throws Exception {
		return MOMessagingQueue.getInstance().size();
	}

	public static int getSerialQueueSize() throws Exception {
		return SerialMessagingQueue.getInstance().size();
	}

	public static int getMTQueueSize() throws Exception {
		return MTMessagingQueue.getInstance().size();
	}
	
	public static int getMORetryQueueSize() throws Exception {
		return MORetryQueue1.getInstance().size();
	}

	public static void checkQueues() throws Exception {
		try {
	    	System.out.println("-------------------------");
	    	System.out.println("Diagnostics:  checkQueues");
	    	System.out.println("-------------------------");
	    	
	    	MessagingQueue moQueue = MOMessagingQueue.getInstance();
	    	MessagingQueue moPriorityQueue = MOPriorityMessagingQueue.getInstance();
	    	MessagingQueue mtQueue = MTMessagingQueue.getInstance();
	    	MessagingQueue sdQueue = SerialMessagingQueue.getInstance();
	    	
	    	System.out.println("queue=<" + moQueue.name() + ", maxSize=<" + moQueue.capacity() + ">, currentSize=<" + moQueue.size() + ">");
	    	System.out.println("queue=<" + moPriorityQueue.name() + ", maxSize=<" + moPriorityQueue.capacity() + ">, currentSize=<" + moPriorityQueue.size() + ">");
	    	System.out.println("queue=<" + mtQueue.name() + ", maxSize=<" + mtQueue.capacity() + ">, currentSize=<" + mtQueue.size() + ">");
	    	System.out.println("queue=<" + sdQueue.name() + ", maxSize=<" + sdQueue.capacity() + ">, currentSize=<" + sdQueue.size() + ">");

		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
}
