package com.numerex.tc65i.micromed;

import com.numerex.tc65i.utilities.serial.SerialDataObject;

public class SerialDataObjectMicroMed implements SerialDataObject {
	private float speedMin = -1;
	private float speedAvg = -1;
	private float speedMax = -1;
	private float speedAlarm = -1;
	
	private float flowMin = -1;
	private float flowAvg = -1;
	private float flowMax = -1;
	private float flowAlarm = -1;
	
	private float powerMin = -1;
	private float powerAvg = -1;
	private float powerMax = -1;
	private float powerAlarm = -1;
	
	private float batteryMin = -1;
	private float batteryAvg = -1;
	private float batteryMax = -1;
	private float batteryAlarm = -1;
	
	private float powerThreshold = -1;
	private float speedThreshold = -1;
	private float flowThreshold = -1;

	private int alarmMask = -1;
	private int statusMask = -1;
	private String alarmMaskStr = null;

	private String pumpId = null;
	private String patientId = null;
	private String hospitalId = null; 
	private String implantDate = null;
	
	private float[] waveform = null;
	
	private int numerexStatusMask = -1;
	private int priority = 0;
	
	private int eventType = -1;
		
	public SerialDataObjectMicroMed() {}
	
	public void setAsWaveformEvent() {
		eventType = 1;
	}
	public void setAsPeriodicEvent() {
		eventType = 2;
	}
	public void setAsAlertEvent() {
		eventType = 3;
	}
	public void setAsControllerEvent() {
		eventType = 5;
	}
	
	public int getEventType() {
		return eventType;
	}
	
	public SerialDataObjectMicroMed(
			float speedMin, float speedAvg, float speedMax, int speedAlarm, 
			float flowMin, float flowAvg, float flowMax, int flowAlarm,
			float powerMin, float powerAvg, float powerMax, int powerAlarm,
			float batteryMin, float batteryAvg, float batteryMax, int batteryAlarm,
			float[] waveform,
			String alarmMaskStr, int statusMask, int numerexStatusMask
	) {
		this.speedMin = speedMin;
		this.speedAvg = speedAvg;
		this.speedMax = speedMax;
		this.speedAlarm = speedAlarm;
		
		this.flowMin = flowMin;
		this.flowAvg = flowAvg;
		this.flowMax = flowMax;
		this.flowAlarm = flowAlarm;

		this.powerMin = powerMin;
		this.powerAvg = powerAvg;
		this.powerMax = powerMax;
		this.powerAlarm = powerAlarm;

		this.batteryMin = batteryMin;
		this.batteryAvg = batteryAvg;
		this.batteryMax = batteryMax;
		this.batteryAlarm = batteryAlarm;
		
		this.alarmMaskStr = alarmMaskStr;
		this.statusMask = statusMask;

		this.numerexStatusMask = numerexStatusMask;
	}

	public void setPatientId(String value) { this.patientId = value; }
	public void setPumpId(String value) { this.pumpId = value; }
	public void setHospitalId(String value) { this.hospitalId = value; }
	public void setImplantDate(String value) { this.implantDate = value; }

	public String getPatientId() { return this.patientId; }
	public String getPumpId() { return this.pumpId; }
	public String getHospitalId() { return this.hospitalId; }
	public String getImplantDate() { return this.implantDate; }
	
	public void setSpeedMin(float value) { this.speedMin = value; }
	public void setSpeedAvg(float value) { this.speedAvg = value; }
	public void setSpeedMax(float value) { this.speedMax = value; }
	public void setSpeedAlarm(float value) { this.speedAlarm = value; }

	public void setFlowMin(float value) { this.flowMin = value; }
	public void setFlowAvg(float value) { this.flowAvg = value; }
	public void setFlowMax(float value) { this.flowMax = value; }
	public void setFlowAlarm(float value) { this.flowAlarm = value; }

	public void setPowerMin(float value) { this.powerMin = value; }
	public void setPowerAvg(float value) { this.powerAvg = value; }
	public void setPowerMax(float value) { this.powerMax = value; }
	public void setPowerAlarm(float value) { this.powerAlarm = value; }

	public void setBatteryMin(float value) { this.batteryMin = value; }
	public void setBatteryAvg(float value) { this.batteryAvg = value; }
	public void setBatteryMax(float value) { this.batteryMax = value; }
	public void setBatteryAlarm(float value) { this.batteryAlarm = value; }

	public void setAlarmMask(int value) { this.alarmMask = value; }
	public void setAlarmMaskString(String value) { this.alarmMaskStr = value; }
	public void setStatusMask(int value) { this.statusMask = value; }
	public void setNumerexStatusMask(int value) { this.numerexStatusMask = value; }
	
	public float getSpeedMin() { return speedMin; }
	public float getSpeedAvg() { return speedAvg; }
	public float getSpeedMax() { return speedMax; }
	public float getSpeedAlarm() { return speedAlarm; }

	public float getFlowMin() { return flowMin; }
	public float getFlowAvg() { return flowAvg; }
	public float getFlowMax() { return flowMax; }
	public float getFlowAlarm() { return flowAlarm; }

	public float getPowerMin() { return powerMin; }
	public float getPowerAvg() { return powerAvg; }
	public float getPowerMax() { return powerMax; }
	public float getPowerAlarm() { return powerAlarm; }

	public float getBatteryMin() { return batteryMin; }
	public float getBatteryAvg() { return batteryAvg; }
	public float getBatteryMax() { return batteryMax; }
	public float getBatteryAlarm() { return batteryAlarm; }

	public int getAlarmMask() { return alarmMask; }
	public String getAlarmMaskStr() { return alarmMaskStr; }

	public int getStatusMask() { return statusMask; }
	public int getNumerexStatusMask() { return numerexStatusMask; }
	
	public void setPriority(int value) { if (value < 1) this.priority = 0; else this.priority = value;}
	public int getPriority() { return this.priority; }
	
	public void setWaveform(float[] value) { if (value != null && value.length > 0) this.waveform = value; }
	public float[] getWaveform() { return this.waveform; }	
}
