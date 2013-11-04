package com.numerex.tc65i.micromed;

import com.numerex.tc65i.utilities.DiagnosticsThread;
import com.numerex.tc65i.utilities.OTAMessage.OTAMessageIntf;
import com.numerex.tc65i.utilities.OTAMessage.OTAMessageMO;
import com.numerex.tc65i.utilities.OTAMessage.OTAMessageMOE;
import com.numerex.tc65i.utilities.OTAMessage.OTA_Object_Byte;
import com.numerex.tc65i.utilities.OTAMessage.OTA_Object_Float;
import com.numerex.tc65i.utilities.OTAMessage.OTA_Object_Float_Array;
import com.numerex.tc65i.utilities.OTAMessage.OTA_Object_Int;
import com.numerex.tc65i.utilities.OTAMessage.OTA_Object_String;

public class SerialDataObjectConverter {
	private static SerialDataObjectConverter serialDataObjectConverter = null;
	private String version = "9.9.9";
	
	private int seqId = -1;
	private String lastIccid = null;
	private long startTime = -1;
	
	private String lastPumpId = null;
	private String lastHospitalId = null;
	private String lastPatientId = null;
	private String lastImplantDate = null;
	
	private byte lastGain = -1;
	private byte lastBalance = -1;
	private byte lastNormA = -1;
	private byte lastNormB = -1;
	
	private float lastSpeedThreshold = -1;
	private float lastFlowThreshold = -1;
	private float lastPowerThreshold = -1;

	private SerialDataObjectConverter() {
		startTime = System.currentTimeMillis();
		seqId = 2;
		lastIccid = null;
		lastPumpId = null;
		lastGain = -1;
		lastBalance = -1;
		lastNormA = -1;
		lastNormB = -1;		
	}
	
	public static SerialDataObjectConverter getInstance() {
		if (serialDataObjectConverter == null) {
			serialDataObjectConverter = new SerialDataObjectConverter();
		}
		return serialDataObjectConverter;
	}
	
	public OTAMessageIntf createMOE(
			int priority,
			int numerexStatusMask,
			int alarmMask,
			int statusMask,

			float batteryAlarm,
			float batteryAvg,
			float batteryMax,
			float batteryMin,
			
			float flowAlarm,
			float flowAvg,
			float flowMax,
			float flowMin,
			
			float powerAlarm,
			float powerAvg,
			float powerMax,
			float powerMin,
			
			float speedAlarm,
			float speedAvg,
			float speedMax,
			float speedMin,
			
			String pumpId2,
			String patientId,
			String hospitalId,
			String implantDate,
			String alarmMaskString,
			
			float[] waveform
	) throws Exception {
			OTAMessageMO msg = new OTAMessageMOE(priority);
		try {
			
			msg.overrideTimestamp(DiagnosticsThread.getSystemDateTime());
			
			String imei = null;
			String iccid = null;
			
			if (this.seqId >= 32767) this.seqId = 2;
			msg.setSeqId(this.seqId);
			
			//add imei
			imei = DiagnosticsThread.getIMEI();
			if (imei != null && imei.length() > 0) {
				msg.addObject(new OTA_Object_String(0, imei));
			} else {
				throw new Exception("invalid IMEI <" + imei + ">");
			}
			
			//add iccid
			//TODO:  persist this across reboots
			iccid = DiagnosticsThread.getICCID();
			if (iccid != null && iccid.length() > 0) {
				if (!iccid.equalsIgnoreCase(lastIccid)) {
					System.out.println("detected SIM swap");
					msg.addObject(new OTA_Object_String(2, iccid));
					lastIccid = iccid;
				}
			} else {
				msg.addObject(new OTA_Object_String(2, "None"));
			}
			System.out.println("imei=<" + imei + ">, iccid=<" + iccid + ">");

			//add diagnostics to the payload
			//TODO:  determine if this should be infrequent or it's own message??
			try {
				msg.addObject(new OTA_Object_String(10, DiagnosticsThread.getVersion()));
				msg.addObject(new OTA_Object_Int(100, (byte)DiagnosticsThread.checkRSSI()));
				msg.addObject(new OTA_Object_Int(101, DiagnosticsThread.getMOPriorityQueueSize()));
				msg.addObject(new OTA_Object_Int(102, DiagnosticsThread.getMOPeriodicQueueSize()));
				msg.addObject(new OTA_Object_Int(103, DiagnosticsThread.getMORetryQueueSize()));
				msg.addObject(new OTA_Object_Int(104, DiagnosticsThread.getSerialQueueSize()));
				msg.addObject(new OTA_Object_Int(105, ((System.currentTimeMillis() - startTime) / 1000) / 60));
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
			//add SIM card pump controller values
			try {
				if (SIM.isSIMReady() == true) {
					String pumpId = SIM.getUnitID();
					byte gain = SIM.getGain();
					byte balance = SIM.getBalance();
					byte normA = SIM.getNormA();
					byte normB = SIM.getNormB();
					
					if (
							!pumpId.equalsIgnoreCase(lastPumpId) ||
							gain != lastGain ||
							balance != lastBalance ||
							normA != lastNormA ||
							normB != lastNormB
						) {
						msg.addObject(new OTA_Object_String(106, pumpId));
						msg.addObject(new OTA_Object_Byte(107, gain));
						msg.addObject(new OTA_Object_Byte(108, balance));
						msg.addObject(new OTA_Object_Byte(109, normA));
						msg.addObject(new OTA_Object_Byte(110, normB));
						msg.addObject(new OTA_Object_Byte(111, (byte)1));
						lastPumpId = pumpId;
						lastGain = gain;
						lastBalance = balance;
						lastNormA = normA;
						lastNormB = normB;
					}
				} else {
					msg.addObject(new OTA_Object_Byte(111, (byte)0));
				}
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
			//add non-SIMCARD IDs
			try {
				if (pumpId2 != null) pumpId2 = pumpId2.trim();
				if (patientId != null) patientId = patientId.trim();
				if (hospitalId != null) hospitalId = hospitalId.trim();
				if (implantDate != null) implantDate = implantDate.trim();

				if (lastPatientId != null) lastPatientId = lastPatientId.trim();
				if (lastHospitalId != null) lastHospitalId = lastHospitalId.trim();
				if (lastImplantDate != null) lastImplantDate = lastImplantDate.trim();

				try {
					System.out.println("*** patientId=<" + patientId + "> lastPatientId=<" + lastPatientId + ">");
					System.out.println("*** hospitalId=<" + hospitalId + "> lastHospitalId=<" + lastHospitalId + ">");
					System.out.println("*** implantDate=<" + implantDate + "> lastImplantDate=<" + lastImplantDate + ">");
					
					System.out.println("*** patientId=<" + patientId + ">");
					System.out.println("*** patientId.length()=<" + patientId.length() + ">");
					System.out.println("*** patientId equals=<" + !patientId.equalsIgnoreCase(lastPatientId)+ ">");
				} catch (Exception e) {
					System.out.println(e);
					e.printStackTrace();
				}
				if (pumpId2 != null && pumpId2 != lastPumpId) {
					//TODO:  what should happen with this?
				}
				if (patientId != null  && patientId.length() > 0 && !patientId.equalsIgnoreCase(lastPatientId)) {
					lastPatientId = patientId;
					msg.addObject(new OTA_Object_String(112, lastPatientId));
				}
				if (hospitalId != null && hospitalId.length() > 0 && !hospitalId.equalsIgnoreCase(lastHospitalId)) {
					lastHospitalId = hospitalId;
					msg.addObject(new OTA_Object_String(113, lastHospitalId));
				}
				if (implantDate != null && implantDate.length() > 0 && !implantDate.equalsIgnoreCase(lastImplantDate)) {
					lastImplantDate = implantDate;
					msg.addObject(new OTA_Object_String(114, lastImplantDate.substring(0, 6)));
				}
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
			//translator.object.map.70=speed_min
			if (speedMin != -1) msg.addObject(new OTA_Object_Float(70, speedMin));
			//translator.object.map.71=speed_avg
			if (speedAvg != -1) msg.addObject(new OTA_Object_Float(71, speedAvg));			
			//translator.object.map.72=speed_max
			if (speedMax != -1) msg.addObject(new OTA_Object_Float(72, speedMax));
			//translator.object.map.73=speed_alarm			
			if (speedAlarm != -1 && speedAlarm != lastSpeedThreshold) {
				msg.addObject(new OTA_Object_Float(73, speedAlarm));
				lastSpeedThreshold = speedAlarm;
			}
			
			//translator.object.map.74=flow_min
			if (flowMin != -1) msg.addObject(new OTA_Object_Float(74, flowMin));
			//translator.object.map.75=flow_avg
			if (flowAvg != -1) msg.addObject(new OTA_Object_Float(75, flowAvg));
			//translator.object.map.76=flow_max
			if (flowMax != -1) msg.addObject(new OTA_Object_Float(76, flowMax));
			//translator.object.map.77=flow_alarm
			if (flowAlarm != -1 && flowAlarm != lastFlowThreshold) {
				msg.addObject(new OTA_Object_Float(77, flowAlarm));
				lastFlowThreshold = flowAlarm;
			}

			//translator.object.map.78=power_min
			if (powerMin != -1) msg.addObject(new OTA_Object_Float(78, powerMin));
			//translator.object.map.79=power_avg
			if (powerAvg != -1) msg.addObject(new OTA_Object_Float(79, powerAvg));
			//translator.object.map.80=power_max
			if (powerMax != -1) msg.addObject(new OTA_Object_Float(80, powerMax));
			//translator.object.map.81=power_alarm
			if (powerAlarm != -1 && powerAlarm != lastPowerThreshold) {
				msg.addObject(new OTA_Object_Float(81, powerAlarm));
				lastPowerThreshold = powerAlarm;
			}

			//translator.object.map.82=battery_min
			if(batteryMin != -1) msg.addObject(new OTA_Object_Float(82, batteryMin));
			//translator.object.map.83=battery_avg
			if (batteryAvg != -1) msg.addObject(new OTA_Object_Float(83, batteryAvg));
			//translator.object.map.84=battery_max
			if (batteryMax != -1) msg.addObject(new OTA_Object_Float(84, batteryMax));
			//translator.object.map.85=battery_alarm
			if (batteryAlarm != -1) msg.addObject(new OTA_Object_Float(85, batteryAlarm));
			
			//translator.object.map.86=alarm_mask
//			if (alarmMask != -1) msg.addObject(new OTA_Object_Int(85, alarmMask));
			if (alarmMaskString != null) msg.addObject(new OTA_Object_String(85, alarmMaskString));

			
			//translator.object.map.87=status_mask
			if (statusMask != -1) msg.addObject(new OTA_Object_Int(86, statusMask));
			
			//translator.object.map.20=waveform
			if (waveform != null && waveform.length > 0) msg.addObject(new OTA_Object_Float_Array(20, waveform));
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		this.seqId++;
		return msg;
	}
}
