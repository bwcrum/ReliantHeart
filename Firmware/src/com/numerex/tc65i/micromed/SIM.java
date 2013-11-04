package com.numerex.tc65i.micromed;

import com.numerex.tc65i.utilities.at.ATCommands;
import com.numerex.tc65i.utilities.strings.StringHelper;

public class SIM {
	public static final int SIM_LOCATION_ID = 23;  //this is pump id
	public static final int SIM_LOCATION_GAIN = 28;
	public static final int SIM_LOCATION_BALANCE = 29;
	public static final int SIM_LOCATION_NORM_A = 31;
	public static final int SIM_LOCATION_NORM_B = 32;
	public static final int SIM_LOCATION_IP_ADDRESS = 34;
	public static final int SIM_LOCATION_APN = 36;
	public static final int SIM_LOCATION_PATIENT_ID = 45;
	public static final int SIM_LOCATION_USER = 40;
	public static final int SIM_LOCATION_PASS = 41;
	public static final int SIM_LOCATION_HOSPITAL_ID = 47;
	public static final int SIM_LOCATION_IMPLANT_DATE = 48;
	public static final int SIM_LOCATION_CONTROLLER_ID = 49;
	
	private static String write(int location, String value) throws Exception {		
    	ATCommands atCommands = ATCommands.getInstance();
    	String send = "AT+CPBW=" + location + ",1,145," + value.trim();
    	String recv = atCommands.sendRecv(send);
    	System.out.println("SIM send=<" + send + ">, AT recv=<" + recv + ">");    	
    	if (recv != null && recv.toLowerCase().indexOf("error") > 0) {
    		throw new Exception("SIM error with send=<" + send + ">, recv=<" + recv + ">");    
    	}
    	return recv;
	}
	
	private static String read(int location) throws Exception {		
    	ATCommands atCommands = ATCommands.getInstance();
    	String send = "AT+CPBR=" + location;
    	String recv = atCommands.sendRecv(send);
    	System.out.println("SIM send=<" + send + ">, AT recv=<" + recv + ">");    	
    	if (recv != null && recv.toLowerCase().indexOf("error") > 0) {
    		throw new Exception("SIM error with send=<" + send + ">, recv=<" + recv + ">");    
    	}
    	
		String remove1 = "AT+CPBR";
		String remove2 = "+CPBR: ";
		recv = StringHelper.stringReplace(recv, remove1, "");
		recv = StringHelper.stringReplace(recv, remove2, "");
		recv = StringHelper.stringReplace(recv, "OK", "");
		recv = StringHelper.stringReplace(recv, "\r", "");
		recv = StringHelper.stringReplace(recv, "\n", "");
		recv = StringHelper.stringReplace(recv, "\"", "");
		recv = recv.trim();
		String value = recv.substring(recv.lastIndexOf(',') + 1).trim();
		System.out.println("SIM value=<" + value + ">");
		return value;
 	}
	
	public static void setPatientID(String value) throws Exception {
		if (value == null || value.length() > 18) throw new Exception ("value <" + value + "> is invalid or too long");
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  setPatientID");
	    	System.out.println("-------------------------------");
	    	write(SIM_LOCATION_PATIENT_ID, value);	    	
	    } catch (Exception e) {
	    	System.out.println(e);
			e.printStackTrace();
			throw e;
		}
	}

	public static String getPatientID() throws Exception {
		String value = null;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getPatientID");
	    	System.out.println("-------------------------------");
	    	value = read(SIM_LOCATION_PATIENT_ID);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return value;
	}
	
	public static void setHospitalID(String value) throws Exception {
		if (value == null || value.length() > 18) throw new Exception ("value <" + value + "> is invalid or too long");
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  setHospitalID");
	    	System.out.println("-------------------------------");
	    	write(SIM_LOCATION_HOSPITAL_ID, value);	    	
	    } catch (Exception e) {
	    	System.out.println(e);
			e.printStackTrace();
			throw e;
		}
	}

	public static String getHospitalID() throws Exception {
		String value = null;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getHospitalID");
	    	System.out.println("-------------------------------");
	    	value = read(SIM_LOCATION_HOSPITAL_ID);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return value;
	}

	public static void setImplantDate(String value) throws Exception {
		if (value == null || value.length() > 18) throw new Exception ("value <" + value + "> is invalid or too long");
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  setImplantDate");
	    	System.out.println("-------------------------------");
	    	write(SIM_LOCATION_IMPLANT_DATE, value);	    	
	    } catch (Exception e) {
	    	System.out.println(e);
			e.printStackTrace();
			throw e;
		}
	}

	public static String getImplantDate() throws Exception {
		String value = null;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getImplantDate");
	    	System.out.println("-------------------------------");
	    	value = read(SIM_LOCATION_IMPLANT_DATE);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return value;
	}

	public static void setUnitID(String value) throws Exception {
		if (value == null || value.length() > 18) throw new Exception ("value <" + value + "> is invalid or too long");
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  setUnitID");
	    	System.out.println("-------------------------------");
	    	write(SIM_LOCATION_ID, value);	    	
	    } catch (Exception e) {
	    	System.out.println(e);
			e.printStackTrace();
			throw e;
		}
	}
	
	public static String getUnitID() throws Exception {
		String value = null;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getUnitID");
	    	System.out.println("-------------------------------");
	    	value = read(SIM_LOCATION_ID);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return value;
	}
	
	public static void setControllerID(String value) throws Exception {
		if (value == null || value.length() > 18) throw new Exception ("value <" + value + "> is invalid or too long");
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  setControllerID");
	    	System.out.println("-------------------------------");
	    	write(SIM_LOCATION_CONTROLLER_ID, value);	    	
	    } catch (Exception e) {
	    	System.out.println(e);
			e.printStackTrace();
			throw e;
		}
	}
	
	public static String getControllerID() throws Exception {
		String value = null;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getControllerID");
	    	System.out.println("-------------------------------");
	    	value = read(SIM_LOCATION_CONTROLLER_ID);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return value;
	}

	
	public static void setGain(byte value) throws Exception {
		if (value < 1 || value > 255) throw new Exception ("value <" + value + "> is invalid or too long");
		try {
//	    	System.out.println("-------------------------------");
//	    	System.out.println("SIM:  setGain");
//	    	System.out.println("-------------------------------");
	    	write(SIM_LOCATION_GAIN, (value + ""));	    	
	    } catch (Exception e) {
	    	System.out.println(e);
			e.printStackTrace();
			throw e;
		}
	}

	public static byte getGain() throws Exception {
		byte value = 0;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getGain");
	    	System.out.println("-------------------------------");
			String returnVal = read(SIM_LOCATION_GAIN);
			value = (byte) Integer.parseInt(returnVal);
			//value = Byte.parseByte(returnVal);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return value;
	}
	
	public static void setBalance(byte value) throws Exception {
		if (value < 1 || value > 255) throw new Exception ("value <" + value + "> is invalid or too long");
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  setBalance");
	    	System.out.println("-------------------------------");
	    	write(SIM_LOCATION_BALANCE, (value + ""));	    	
	    } catch (Exception e) {
	    	System.out.println(e);
			e.printStackTrace();
			throw e;
		}
	}

	public static byte getBalance() throws Exception {
		byte value = 0;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getBalance");
	    	System.out.println("-------------------------------");
			String returnVal = read(SIM_LOCATION_BALANCE);
			value = (byte) Integer.parseInt(returnVal);
//			value = Byte.parseByte(returnVal);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return value;
	}
	
	public static void setNormA(byte value) throws Exception {
		if (value < 1 || value > 255) throw new Exception ("value <" + value + "> is invalid or too long");
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  setNormA");
	    	System.out.println("-------------------------------");
	    	write(SIM_LOCATION_NORM_A, (value + ""));	    	
	    } catch (Exception e) {
	    	System.out.println(e);
			e.printStackTrace();
			throw e;
		}
	}

	public static byte getNormA() throws Exception {
		byte value = 0;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getNormA");
	    	System.out.println("-------------------------------");
			String returnVal = read(SIM_LOCATION_NORM_A);
			value = (byte) Integer.parseInt(returnVal);
//			value = Byte.parseByte(returnVal);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return value;
	}
	
	public static void setNormB(byte value) throws Exception {
		if (value < 1 || value > 255) throw new Exception ("value <" + value + "> is invalid or too long");
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  setNormB");
	    	System.out.println("-------------------------------");
	    	write(SIM_LOCATION_NORM_B, (value + ""));	    	
	    } catch (Exception e) {
	    	System.out.println(e);
			e.printStackTrace();
			throw e;
		}
	}

	public static byte getNormB() throws Exception {
		byte value = 0;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getNormB");
	    	System.out.println("-------------------------------");
			String returnVal = read(SIM_LOCATION_NORM_B);
			value = (byte) Integer.parseInt(returnVal);
//			value = Byte.parseByte(returnVal);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return value;
	}
	
	public static String getIP() throws Exception {
		String returnVal = null;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getIPAddress");
	    	System.out.println("-------------------------------");
			returnVal = read(SIM_LOCATION_IP_ADDRESS).trim();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return returnVal;
	}

	public static String getAPN() throws Exception {
		String returnVal = null;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getAPN");
	    	System.out.println("-------------------------------");
			returnVal = read(SIM_LOCATION_APN).trim();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return returnVal;
	}

	public static String getUSER() throws Exception {
		String returnVal = null;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getUSER");
	    	System.out.println("-------------------------------");
			returnVal = read(SIM_LOCATION_USER).trim();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return returnVal;
	}

	public static String getPASS() throws Exception {
		String returnVal = null;
		try {
	    	System.out.println("-------------------------------");
	    	System.out.println("SIM:  getPASS");
	    	System.out.println("-------------------------------");
			returnVal = read(SIM_LOCATION_PASS).trim();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return returnVal;
	}

	public static boolean isSIMReady() throws Exception {
	   	boolean ready = false;
		ATCommands atCommands = ATCommands.getInstance();
    	String send = "at+cnum";
    	String recv = atCommands.sendRecv(send);
//    	System.out.println("AT send=<" + send + ">, AT recv=<" + recv + ">");    	
    	if (recv != null && recv.toLowerCase().indexOf("error") > -1) {
    		ready = false;
    	} else if (recv != null && recv.toLowerCase().indexOf("ok") > -1) {
    	   	ready = true;
    	} else {
       		throw new Exception("ATCommands error with send=<" + send + ">, recv=<" + recv + ">");    
    	}
    	return ready;
	}
	
	public static void main(String[] args) throws Exception {
		//at+cpbr=28
		//+CPBR: 28,"7",209,"7"

		//at+cpbw=23,14321,145,"Arthur"
		//OK

		String recv = "+CPBR: 28,\"7\",209,\"7\"";
		System.out.println("recv=<" + recv + ">");
		
		String remove1 = "AT+CPBR=29\r\n+CPBR: 29,\"+1\",145,\"11\"";
		String remove2 = "+CPBR: ";
		
		recv = StringHelper.stringReplace(recv, remove1, "");
		recv = StringHelper.stringReplace(recv, remove2, "");
		recv = StringHelper.stringReplace(recv, "\r", "");
		recv = StringHelper.stringReplace(recv, "\n", "");
		recv = StringHelper.stringReplace(recv, "\"", "");
		recv = recv.trim();

		System.out.println("recv=<" + recv + ">");

		String value = recv.substring(recv.lastIndexOf(',') + 1);
		System.out.println("value=<" + value + ">");
	}
}
