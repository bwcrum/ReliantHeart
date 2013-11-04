package com.numerex.tc65i.utilities.network.gsm;

public class GSMHelper {
	public static int rssiToDbm(int rssi) {
		int dBm = -113;
		/*
			 99 > Not detectable
			 0 = -113 dBm
			 1 = -111 dBm
			 2 to 30 -> -109 to -53 dBm
			 31 = -51 dBm
		*/
		if (rssi == 0 || rssi > 31) {
			dBm = -113;
		} else if (rssi == 1) {
			dBm = -111;
		} else if (rssi < 31) {
			int[] array = new int[34];
			array[0] = -109;
			array[1] = -109;
			array[2] = -109;
			
			int val = -105;
			for (int i = 3; i < 34; i++) {
				val += 2;
				array[i] = val;
			}
			dBm = array[rssi];
		} else if (rssi == 31) {
			dBm = -51;
		}
		return dBm;
	}
}
