package com.numerex.tc65i.micromed;

import java.io.*;
import java.util.Date;
import javax.microedition.io.*;

import com.numerex.tc65i.utilities.DiagnosticsThread;
import com.numerex.tc65i.utilities.queues.MOPriorityMessagingQueue;
import com.numerex.tc65i.utilities.queues.Message;
import com.numerex.tc65i.utilities.queues.SerialMessagingQueue;
import com.numerex.tc65i.utilities.OTAMessage.*;

public class SerialReceiverThread extends Thread {

	int x,m;
	private static int valid0 = 0;
	private static int valid1 = 0;
	private CommConnection cc;
	private InputStream is;
	private OutputStream os;
	private static int crcErrors = 0;
	private int waveformOffset = 0;
	private int waveformFront = 0;
	private int eventHappened = 0;
	private int noController = 0;
	private int numbytes;
	
	private byte patientID[] = new byte[20];
	private byte hospitalID[] = new byte[20];
	private byte implantDate[] = new byte[20];
	private byte pumpID[] = new byte[22];
	
	private short byteValue;
	private byte inBytes[] = new byte[187];
	private byte outBytes[] = new byte[96]; // message to controller
	private float waveform[] = new float[1200];
	private float Flow[] = new float[1000];
	private float Battery[] = new float[1000];
	private float Speed[] = new float[1000];
	private float Power[] = new float[1000];
	private float offsetFloat;
	private float flowAverage;
	private float batteryAverage;
	private float speedAverage;
	private float powerAverage;
	private float flowMin;
	private float batteryMin = 30;
	private float speedMin;
	private float powerMin = 30;
	private float flowMax;
	private float batteryMax;
	private float speedMax;
	private float powerMax;

	private float speedThreshold;
	private float flowThreshold;
	private float powerThreshold;
	
	//will likely move this stuff to SIM
	private String oldPatientId = null;
	private String oldHospitalId = null;
	private String oldPumpId = null;
	private String oldImplantDate = null;
	private String alarm1String = null;
	private String alarm2String = null;

	private int status;
	private byte status1;
	private byte status2;
	private int alarm;
	private byte alarm1;
	private byte alarm2;
	private byte alarm1prev = 0;
	private byte alarm2prev = 0;
	private byte crcHigh;
	private byte crcLow;
	private byte Gain;
	private byte Balance;
	private byte NormA;
	private byte NormB;
	private int myCRC;
	private int rxCRC;
	private int ack = 60;
	private int testMode = 0;
	private int testOffset = 0;
	private int nextMessage = 0;
	private String tempStr;
	private boolean airMode = false;
	private boolean sendWaveForm = false;
	private int readingOffset = 0;

	public static int getVBCRC16(byte[] bytes) throws Exception {
		int value = 0xFFFFFFFF;
		for (int i = 0; i < (bytes.length - 2); i++) {
			value = (((value & 0x000000FF) * 256) + ((value & 0x0000FF00) / 256));
			value = (value ^ (bytes[i] & 0xFF));
			value = (value ^ ((value & 0x000000F0) / 16));
			value = (value ^ ((value & 0x0000000F) * 4096));
			value = (value ^ ((value & 0x000000FF) * 32));
		}
		value = (~value) & 0xFFFF;
		return value;
	}

	public void requestWaveForm() {
		System.out.println("***********WaveForm Request");
		sendWaveForm = true;
	}

	public static String asciiBytesToString( byte[] bytes ) {
	      if ((bytes == null) || (bytes.length == 0)) {
	          return "";
		      }
	      char[] result = new char[bytes.length];
	      for ( int i = 0; i < bytes.length; i++ ) {
	          result[i] = (char)bytes[i];
	      }
	      return new String(result).trim();
	}
	  
	public void run() {
		System.out.println("-------------------Serial Receiver Thread---------");

		try {
			// create a serial connection to controller
			try {
				// create comm connection
				cc = (CommConnection) Connector.open("comm:com0;baudrate=115200;autocts=on;autorts=on");
				// get stream to read/write
				is = cc.openInputStream();
				os = cc.openOutputStream();

				if (SIM.isSIMReady()) {
					Gain = SIM.getGain();
					Balance = SIM.getBalance();
					NormA = SIM.getNormA();
					NormB = SIM.getNormB();
				}
				
				System.out.println("Gain:" + Integer.toString((int) Gain));
				System.out.println("Balance:" + Integer.toString((int) Balance));
				System.out.println("NormA:" + Integer.toString((int) NormA));
				System.out.println("NormB:" + Integer.toString((int) NormB));

				System.out.println("++++++++++++++++++++++++++Serial INIT");
				
				// flush the buffer
				while (is.available() > 0) {
					if (is.available() > 187)
						is.read(inBytes, 0, 187);
					else
						is.read(inBytes, 0, is.available());
				}
			} catch (Exception e) {
				System.out.println("There was some problem with I/O communication.");
				return;
			}

			for (;;) {
				Thread.yield();

				Thread.sleep(200);
				noController++;
				// wait for packet from controller
				// Once a second send a request for data
				//System.out.print("-");

				while (is.available() > 187) {
					numbytes = is.available();
					System.out.println("------------------------Something Bad happened " + numbytes);
					while (is.available() > 0) {
						if (is.available() > 187)
							is.read(inBytes, 0, 187);
						else
							is.read(inBytes, 0, is.available());
					}
				}
				
				if (is.available() > 186) {
					numbytes = is.available();
					x = is.read(inBytes, 0, 187);
					// validate checksum
					// send Ack
					crcHigh = inBytes[186];
					crcLow = inBytes[185];
					rxCRC = (int) (0x00ff & crcHigh) * 256;
					rxCRC += (int) (0x00ff & crcLow);
					myCRC = getVBCRC16(inBytes);

					if (myCRC == rxCRC) {
						noController = 0;
						System.out.println("-------------------------ZZZZ---------------GOOD CRC " + readingOffset);
	//					x = readingOffset % 10;
	//					if(x == 0)
	//					{
	//					System.out.println("------READING-----------");
	//						for(x=0;x<1000;x++)
	//						{
	//							System.out.print(waveform[x] +  " ");
	//							m = x % 5;
	//							if(m == 0)
	///								System.out.println("");
	//						}
	//						System.out.println("-----------------");
	//					}
						if (ack++ > 60) {
							ack = 0;

							// TODO send ACK to controller Keep alive
							//
							outBytes[0] = (byte) 0xff; // header
							outBytes[1] = (byte) 0xff; // header
							outBytes[2] = (byte) 0xff; // header
							outBytes[3] = (byte) 0xff; // header
							valid0 = 0;
							valid1 = 0;
							
							if (SIM.isSIMReady()) {
								valid1 = valid1 | 0x02;
								valid0 = valid0 | 0x08; // pumpid valid
								outBytes[4] = (byte) valid0;
								outBytes[5] = (byte) valid1;
								
								//System.out.println("----------------------------------------SIM READY");
								Gain = SIM.getGain();
								Balance = SIM.getBalance();
								NormA = SIM.getNormA();
								NormB = SIM.getNormB();
					
								outBytes[77] = Gain;
								outBytes[78] = Balance;
								outBytes[79] = 0x00;
								outBytes[80] = NormA;
								outBytes[81] = NormB;

								//System.out.println("------------------------GAIN " + Gain);
								//System.out.println("------------------------Balance " + Balance);
								//System.out.println("------------------------NormA " + NormA);
								//System.out.println("------------------------NormB " + NormB);
							} else {
								outBytes[4] = (byte) valid0;
								outBytes[5] = (byte) valid1;
								System.out.println("----------------------------------------SIM OUT");
							}
							
							// set the valid flags
							// outBytes[4] = valid0; // data valid bits
							// outBytes[5] = valid1; // data valid bits

							for (x = 0; x < 6; x++) {
								outBytes[46 + x] = implantDate[x]; // Implant
																	// date
							}
							for (x = 0; x < 20; x++) {
								outBytes[6 + x] = patientID[x]; // patient ID
								outBytes[26 + x] = hospitalID[x]; // hospital
								outBytes[52 + x] = '\0'; // Pump ID 20 bytes
							}

							outBytes[72] = inBytes[75]; // speed 1 byte
							outBytes[73] = inBytes[76]; // speed alarm 1 byte
							outBytes[74] = inBytes[77]; // reserved
							outBytes[75] = inBytes[78]; // flow alarm 1 byte
							outBytes[76] = (byte) 0x00; // current alarm 1 byte

							// time string 12 bytes
							// YYMMDDHHMMSS ascii
							Date now = new Date();
							//System.out.println(" 1. " + now.toString());
							String nowTime = now.toString();

							if (nowTime.length() > 27) {
								tempStr = nowTime.substring(4, 7);
								if (tempStr.compareTo("Jan") == 0) // month
								{
									outBytes[84] = (byte) '0';
									outBytes[85] = (byte) '1';
								} else if (tempStr.compareTo("Feb") == 0) // month
								{
									outBytes[84] = (byte) '0';
									outBytes[85] = (byte) '2';
								} else if (tempStr.compareTo("Mar") == 0) // month
								{
									outBytes[84] = (byte) '0';
									outBytes[85] = (byte) '3';
								} else if (tempStr.compareTo("Apr") == 0) // month
								{
									outBytes[84] = (byte) '0';
									outBytes[85] = (byte) '4';
								} else if (tempStr.compareTo("May") == 0) // month
								{
									outBytes[84] = (byte) '0';
									outBytes[85] = (byte) '5';
								} else if (tempStr.compareTo("Jun") == 0) // month
								{
									outBytes[84] = (byte) '0';
									outBytes[85] = (byte) '6';
								} else if (tempStr.compareTo("Jul") == 0) // month
								{
									outBytes[84] = (byte) '0';
									outBytes[85] = (byte) '7';
								} else if (tempStr.compareTo("Aug") == 0) // month
								{
									outBytes[84] = (byte) '0';
									outBytes[85] = (byte) '8';
								} else if (tempStr.compareTo("Sep") == 0) // month
								{
									outBytes[84] = (byte) '0';
									outBytes[85] = (byte) '9';
								} else if (tempStr.compareTo("Oct") == 0) // month
								{
									outBytes[84] = (byte) '1';
									outBytes[85] = (byte) '0';
								} else if (tempStr.compareTo("Nov") == 0) // month
								{
									outBytes[84] = (byte) '1';
									outBytes[85] = (byte) '1';
								} else if (tempStr.compareTo("Dec") == 0) // month
								{
									outBytes[84] = (byte) '1';
									outBytes[85] = (byte) '2';
								}

								outBytes[82] = (byte) nowTime.charAt(26);
								outBytes[83] = (byte) nowTime.charAt(27);

								outBytes[86] = (byte) nowTime.charAt(8);
								outBytes[87] = (byte) nowTime.charAt(9);

								outBytes[88] = (byte) nowTime.charAt(11);
								outBytes[89] = (byte) nowTime.charAt(12);
								outBytes[90] = (byte) nowTime.charAt(14);
								outBytes[91] = (byte) nowTime.charAt(15);
								outBytes[92] = (byte) nowTime.charAt(17);
								outBytes[93] = (byte) nowTime.charAt(18);

							}

							myCRC = getVBCRC16(outBytes);
							outBytes[95] = (byte) ((myCRC >> 8) & 0xff);
							outBytes[94] = (byte) (myCRC & 0xff);

							os.write(outBytes, 0, 96);
							for (x = 0; x < 10; x++) {
								//System.out.print(outBytes[x] + " ");
							}

							//System.out.println();
							//System.out.println("========================================================Sent ACK");
						}
						// done with ack to controller
//						System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&check alarms");
						alarm1 = inBytes[5];
						alarm2 = inBytes[6];
						
						byte reducedMotorSpeed = 0;
						byte lowInternalBattery = 0;
						byte reducedFlowRate = 0;
						byte excessCurrent = 0;
						byte vadDisconnected = 0;
						byte bothBatteriesDisconnected = 0;
						byte controllerFailure = 0;
						byte pumpStopped = 0;
						
						if (alarm1 != alarm1prev) {
							if ((alarm1 & 0x80) > 0) {
								System.out.println("----------------------------------Reduced Motor Speed");
								reducedMotorSpeed = 1;
							}
							if ((alarm1 & 0x40) > 0) {
								System.out.println("----------------------------------Low Internal Battery");
								lowInternalBattery = 1;
							}
							if ((alarm1 & 0x20) > 0) {
								System.out.println("----------------------------------Reduced Flow Rate");
								reducedFlowRate = 1;
							}
							if ((alarm1 & 0x10) > 0) {
								System.out.println("----------------------------------Excess Current");
								excessCurrent = 1;
							}
							if ((alarm1 & 0x08) > 0) {
								System.out.println("----------------------------------VAD Disconnected");
								vadDisconnected = 1;
							}
							if ((alarm1 & 0x04) > 0) {
								System.out.println("----------------------------------Both Batteries Disconnected");
								bothBatteriesDisconnected = 1;
							}
							if ((alarm1 & 0x02) > 0) {
								System.out.println("----------------------------------Controller Failure");
								controllerFailure = 1;
							}
							if ((alarm1 & 0x01) > 0) {
								System.out.println("----------------------------------Pump Stopped");
								pumpStopped = 1;
							}

							System.out.println("Alarm changed, send event" + alarm1 + " " + alarm1prev);
							alarm1prev = alarm1;
							eventHappened = 1;
						}
						
						byte battery2Expired = 0;
						byte battery2Discharged = 0;
						byte battery2Disconnected = 0;
						byte battery1Expired = 0;
						byte battery1Discharged = 0;
						byte battery1Disconnected = 0;
						byte excessSuctionRPMSreduced = 0;
						byte pumpRestarting = 0;
						
						if (alarm2 != alarm2prev) {
							if ((alarm2 & 0x80) > 0) {
								System.out.println("----------------------------------Battery #2 Expired");
								battery2Expired = 1;
							}
							if ((alarm2 & 0x40) > 0) {
								System.out.println("----------------------------------Battery #2 Discharged");
								battery2Discharged = 1;
							}
							if ((alarm2 & 0x20) > 0) {
								System.out.println("----------------------------------Battery #2 Disconnected");
								battery2Disconnected = 1;
							}
							if ((alarm2 & 0x10) > 0) {
								System.out.println("----------------------------------Battery #1 Expired");
							}
							if ((alarm2 & 0x08) > 0) {
								System.out.println("----------------------------------Battery #1 Discharged");
								battery1Discharged = 1;
							}
							if ((alarm2 & 0x04) > 0) {
								System.out.println("----------------------------------Battery #1 Disconnected");
								battery1Disconnected = 1;
							}
							if ((alarm2 & 0x02) > 0) {
								System.out.println("----------------------------------Excess Suction RPMs Reduced");
								excessSuctionRPMSreduced = 1;
							}
							if ((alarm2 & 0x01) > 0) {
								System.out.println("----------------------------------Pump Restarting");
								pumpRestarting = 1;
							}
							System.out.println("Alarm changed, send event" + alarm2 + " " + alarm2prev);
							alarm2prev = alarm2;
							eventHappened = 1;
						}
						
						//now let's reorder the alarm bits as per
						/*
						BIT	 EVOLUTION	 							CONQUEST
						F	 Battery #2 Expired	 					Battery #2 Expired
						E	 Battery #2 Discharged	 				Battery #2 Discharged
						D	 Battery #2 Disconnected	 			Battery #2 Disconnected
						C	 Battery #1 Expired	 					Battery #1 Expired
						B	 Battery #1 Discharged	 				Battery #1 Discharged
						A	 Battery #1 Disconnected	 			Battery #1 Disconnected
						9	Controller Failure						Excess Suction RPM’s Reduced
						8	Alarm #9								Pump Restarting

						7	 Low Speed	 							Low Speed
						6	 Low Internal Battery	 				Low Internal Battery
						5	 Low Flow	 							Low Flow
						4	 Excess Power	 						Excess Power
						3	 Pump Disconnected From Controller	 	Pump Disconnected From Controller
						2	 Both Batteries Disconnected			Both Batteries Disconnected
						1	Alarm #2								Controller Failure
						0	 Pump Stopped	 						Pump Stopped
						
						The least change from Evolution to Conquest would be for Controller Failure to stay BIT 9 and make the new Excess Suction RPM’s Reduced BIT 1 instead of BIT 9.
						*/

						//System.out.println("Status: " + Integer.toHexString(inBytes[7]) + " " + Integer.toHexString(inBytes[8]));
						status1 = inBytes[8];
						status2 = inBytes[7];
						if ((status2 & 0x08) > 0) // SIM values may have
													// changed
						{
							System.out.println("Status: Attendant has different values over SIM");
						}

						if ((status2 & 0x04) > 0) // airplane mode
						{
							if (airMode == false) {
								System.out.println("Airplane Mode on");
								DiagnosticsThread.setAirplaneMode(true);
							}
							airMode = true;
						} else {
							if (airMode == true) {
								DiagnosticsThread.setAirplaneMode(false);
								System.out.println("Airplane Mode off");
							}
							airMode = false;
						}
						
						// byte 9 patient ID
						for (x = 0; x < 20; x++) {
							patientID[x] = inBytes[9 + x]; // patient ID
							hospitalID[x] = inBytes[29 + x]; // hospital ID
							implantDate[x] = inBytes[39 + x]; // Implant date
							pumpID[x] = inBytes[55 + x]; // Pump ID 20 bytes
						}
						for (x = 0; x < 6; x++) {
							implantDate[x] = inBytes[49 + x];
						}
						// save to sim if bit set

						if ((status1 & 0x08) > 0) {
							pumpID[19] = '\0';
							String pumpid = new String(pumpID);
						}

						//System.out.println("Patient ID: " + asciiBytesToString(patientID));
						// //9-28
						//System.out.println("Hospital ID: " + asciiBytesToString(hospitalID));
						// //29-48
						//System.out.println("Implant Date: " + asciiBytesToString(implantDate));
						// //49-54
						//System.out.println("Pump ID: " + asciiBytesToString(pumpID)); //55-74
						
						oldPatientId = asciiBytesToString(patientID);
						oldHospitalId = asciiBytesToString(hospitalID);
						oldImplantDate = asciiBytesToString(implantDate);
						oldPumpId = asciiBytesToString(pumpID);
											
						// System.out.println("Pump speed: " +
						// Integer.toHexString(inBytes[75]));
						// System.out.println("Speed alarm threshold: " +
						// Integer.toHexString(inBytes[76]));
						// System.out.println("Flow alarm threshold: " +
						// Integer.toHexString(inBytes[77]));
						// System.out.println("Current alarm threshold: " +
						// Integer.toHexString(inBytes[78]));
						// System.out.println("Reserved: " +
						// Integer.toHexString(inBytes[79]));
						// System.out.println("1 Second Speed: " +
						// Integer.toHexString(inBytes[80]));

						// speed threshold
						byteValue = (short) (inBytes[76] & 0x00FF);
						if (byteValue == 51)
							speedThreshold = 7500f;
						else
							speedThreshold = 12600f - (((float) byteValue) * 100f);

						// flow threshold
						byteValue = (short) (inBytes[77] & 0x00FF);

						flowThreshold = (.0547f * (float) byteValue) - 4f;

						// power threshold
						byteValue = (short) (inBytes[78] & 0x00FF);
						powerThreshold = (.0117f * (float) byteValue) * 13.5f;

						// 1 second speed
						byteValue = (short) (inBytes[80] & 0x00FF);
						if (byteValue == 51)
							Speed[readingOffset] = 7500f;
						else
							Speed[readingOffset] = 12600f - (((float) byteValue) * 100f);
						if (Speed[readingOffset] > speedMax)
							speedMax = Speed[readingOffset];
						else if (Speed[readingOffset] < speedMin)
							speedMin = Speed[readingOffset];

						byteValue = (short) (inBytes[81] & 0x00FF);

						Flow[readingOffset] = (.0547f * (float) byteValue) - 4f;

						if (Flow[readingOffset] > flowMax)
							flowMax = Flow[readingOffset];
						else if (Flow[readingOffset] < flowMin)
							flowMin = Flow[readingOffset];

						byteValue = (short) (inBytes[84] & 0x00FF);
						Battery[readingOffset] = .0586f * (float) byteValue;
						if (Battery[readingOffset] > batteryMax)
							batteryMax = Battery[readingOffset];
						else if (Battery[readingOffset] < batteryMin)
							batteryMin = Battery[readingOffset];

						// aw -- moving power calculation to here
						// inBytes[82] contains current, not power value. One
						// Second Average Power (in Watts) = (One Second Average
						// Battery Voltage * 0.059) * (One Second Average
						// Current * 0.117)
						byteValue = (short) (inBytes[82] & 0x00FF);
						Power[readingOffset] = (.0117f * (float) byteValue) * Battery[readingOffset];

						if (Power[readingOffset] > powerMax)
							powerMax = Power[readingOffset];
						else if (Power[readingOffset] < powerMin)
							powerMin = Power[readingOffset];
//						System.out.println("1 Second Power: " + Power[readingOffset]);

//						System.out.println("==================");
//						for (x = 0; x < 100; x++) {
//							System.out.print(" " + Integer.toHexString( (int) ( inBytes[x + 85])));
//						}
//						System.out.println("====================");
							for (x = 0; x < 100; x++) {
							byteValue = (short) (inBytes[x + 85] & 0x00FF);
							waveform[waveformOffset] = (.0547f * (float) byteValue) - 4f;
							Flow[readingOffset] = (.0547f * (float) byteValue) - 4f;
							//waveformOffset++;
//							System.out.print(" " + waveform[waveformOffset]);
							waveformOffset++;
						}
						if (waveformOffset >= 1099) {
							waveformOffset = 0;
						}
						if (waveformFront == waveformOffset) {
							waveformFront += 100;
							if (waveformFront >= 1099) {
								waveformFront = 0;
							}
						}

						readingOffset++;
						testOffset++;
						System.out.println("");
						System.out.println("wfront=[" + waveformFront + "]");
						System.out.println("wback=[" + waveformOffset);
						if (sendWaveForm) {
							sendWaveForm = false;
							/*
							 * When you get a serial event parse it and populate
							 * a SerialDataObject like this replace the random
							 * number value of each with your calculated values
							 */
							System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!Send WaveForm");
							SerialDataObjectMicroMed sdo = new SerialDataObjectMicroMed();
							float newWaveform[] = new float[1000];
							System.out.println("Front = " + waveformFront + " Offset = " + waveformOffset);
							int x = waveformFront;
							int y = waveformOffset;
							int z = 0;

							while (x != y) {
								newWaveform[z] = waveform[x];
								z++;
								x++;
								if (x > 1099)
									x = 0;
							}
//							for(z=0;z<1000;z++)
//							{
//								System.out.print(waveform[z] +  " ");
//								x = z % 5;
//								if(x == 0)
//									System.out.println("");
//							}
//							System.out.println("-----------------bw");
							
//							for(z=0;z<1000;z++)
//							{
//								System.out.print(newWaveform[z] +  " ");
//								x = z % 5;
//								if(x == 0)
//									System.out.println("");
//							}
							sdo.setAsWaveformEvent();
							sdo.setWaveform(newWaveform);
							sdo.setPriority(1);
							SerialMessagingQueue.getInstance().add(sdo);
							System.out.println("!!!!!!!!!!!!!!!!!>>!!!!!!!!!!!Sent WaveForm");
						}
						
						if (eventHappened > 0) {
							//00100100
							//00000000
							alarm2String = ""
								+ battery2Expired  //f
								+ battery2Discharged //e
								+ battery2Disconnected //d
								+ battery1Expired //c
								+ battery1Discharged //b
								+ battery1Disconnected //a
								+ controllerFailure //1//+ excessSuctionRPMSreduced //9
								+ pumpRestarting;  //8
							
							//00100000
							//00001000
							alarm1String = ""
								+ reducedMotorSpeed  //7
								+ lowInternalBattery //6
								+ reducedFlowRate //5
								+ excessCurrent //4
								+ vadDisconnected //3
								+ bothBatteriesDisconnected  //2
								+ excessSuctionRPMSreduced //9//+ controllerFailure //1
								+ pumpStopped; //0

							System.out.println("alarm1=<" + alarm1 + ">, alarm1String=<" + alarm1String + ">, alarm2=<" + alarm2 + ">, alarm2String=<" + alarm2String + ">");
							
							alarm1 = (byte) Integer.parseInt(alarm1String, 2);
							alarm2 = (byte) Integer.parseInt(alarm2String, 2);
							//alarm1 = 0x00;
							//alarm2 = 0x00;
							System.out.println("alarm1=<" + alarm1 + ">, alarm1String=<" + alarm1String + ">, alarm2=<" + alarm2 + ">, alarm2String=<" + alarm2String + ">");

							// create message and add to queue
							SerialDataObjectMicroMed sdo = new SerialDataObjectMicroMed();
							alarm = alarm2 << 8;
							alarm &= 0xff00;
							alarm |= alarm1;
							sdo.setAlarmMaskString(alarm1String + alarm2String);
							//sdo.setAlarmMask(alarm);
							status = status1 << 8;
							status &= 0xff00;
							status |= status2;

							sdo.setStatusMask(status);
							sdo.setPriority(1);
							eventHappened = 0;
							sdo.setAsAlertEvent();
							System.out.println("SerialReceiverThread stuff a packet");
							SerialMessagingQueue.getInstance().add(sdo);
						}

						if (readingOffset == 900) // 15 minutes
						{
							// do the average and send.
							offsetFloat = (float) readingOffset; // in case
																	// an event
																	// happened
							if (offsetFloat == 0.0f)
								offsetFloat = 1.0f; // should never happen
						
							flowAverage = 0.0f;
							batteryAverage = 0.0f;
							speedAverage = 0.0f;
							powerAverage = 0.0f;
							
							for (x = 0; x < readingOffset; x++) {
								flowAverage += Flow[x];
								batteryAverage += Battery[x];
								speedAverage += Speed[x];
								powerAverage += Power[x];
							}
							
							flowAverage = flowAverage / offsetFloat;
							batteryAverage = batteryAverage / offsetFloat;
							speedAverage = speedAverage / offsetFloat;
							powerAverage = powerAverage / offsetFloat;

							System.out.println("---------------------------------------------------");
							System.out.println("Alarm Mask " + (int) alarm1);
							System.out.println("AVERAGES:");
							System.out.println("flow=" + flowAverage + " battery=" + batteryAverage);
							System.out.println("speed=" + speedAverage + " power=" + powerAverage);
							System.out.println("IDENTIFIERS:");
							System.out.println("patientId=" + oldPatientId + " pumpId=" + oldPumpId);
							System.out.println("hospitalId=" + oldHospitalId + " implantDate=" + oldImplantDate);
							System.out.println("---------------------------------------------------");

							// add to send queue
							System.out.println("SerialReceiverThread make a packet");

							/*
							 * When you get a serial event parse it and populate
							 * a SerialDataObject like this replace the random
							 * number value of each with your calculated values
							 */
							SerialDataObjectMicroMed sdo = new SerialDataObjectMicroMed();
							alarm = alarm2 << 8;
							alarm &= 0xff00;
							alarm |= alarm1;
							sdo.setAlarmMaskString(alarm1String + alarm2String);
//							sdo.setAlarmMaskStr(alarm);
							
							status = status1 << 8;
							status &= 0xff00;
							status |= status2;
							sdo.setStatusMask(status);

							sdo.setBatteryAvg(batteryAverage);
							sdo.setBatteryMax(batteryMax);
							sdo.setBatteryMin(batteryMin);

							sdo.setFlowAvg(flowAverage);
							sdo.setFlowMax(flowMax);
							sdo.setFlowMin(flowMin);
							sdo.setFlowAlarm(flowThreshold);

							sdo.setPowerAvg(powerAverage);
							sdo.setPowerMax(powerMax);
							sdo.setPowerMin(powerMin);
							sdo.setPowerAlarm(powerThreshold);

							sdo.setSpeedAvg(speedAverage);
							sdo.setSpeedMax(speedMax);
							sdo.setSpeedMin(speedMin);
							sdo.setSpeedAlarm(speedThreshold);

							sdo.setStatusMask((int) status1);
							sdo.setAlarmMaskString(alarm1String + alarm2String);
//							sdo.setAlarmMask((int) alarm1);
							
							sdo.setPatientId(oldPatientId);
							sdo.setPumpId(oldPumpId);
							sdo.setHospitalId(oldHospitalId);
							sdo.setImplantDate(oldImplantDate);

							/*
							 * if you set priority > 0, it will assume it's an
							 * alarm. if you don't set it it will assume the
							 * message is a periodic message.
							 */
							if (eventHappened > 0) {
								sdo.setPriority(1);
								eventHappened = 0;
								sdo.setAsAlertEvent();
							} else {
								sdo.setPriority(1);
								sdo.setAsPeriodicEvent();
								readingOffset = 0;
								batteryMax = 0f;
								batteryMin = 15f;
								flowMax = 0f;
								flowMin = 10f;
								speedMax = 0f;
								speedMin = 12500f;
								powerMax = 0f;
								powerMin = 30f;
							}

							/*
							 * Add your serial object to the message queue. I
							 * might add a flag to the return value of the add()
							 * method so you know if the queue had to pop an old
							 * one off to add your latest value
							 */

							System.out.println("SerialReceiverThread stuff a packet");
							SerialMessagingQueue.getInstance().add(sdo);
						}
					} else {
						crcErrors++;
						System.out.println("----------------------------------------BAD CRC " + crcErrors);
					}
				}

				if (noController > 500) {
					SerialDataObjectMicroMed sdo = new SerialDataObjectMicroMed();
					sdo.setPriority(1);
					sdo.setAsControllerEvent();
					noController = 0;
					SerialMessagingQueue.getInstance().add(sdo);
				}
				
				if (testMode > 0) {
					if (testOffset >= 60) {
						testOffset = 0;
						SerialDataObjectMicroMed sdo = new SerialDataObjectMicroMed();
						switch (nextMessage) {
						case 0: // no controller
							sdo.setPriority(1);
							sdo.setAsControllerEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 1: // alarm
							alarm = 0x0001;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 2: // alarm
							alarm = 0x0002;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;

						case 3: // alarm
							alarm = 0x0004;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 4: // alarm
							alarm = 0x0008;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 5: // alarm
							alarm = 0x0010;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 6: // alarm
							alarm = 0x0020;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 7: // alarm
							alarm = 0x0040;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 8: // alarm
							alarm = 0x0080;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 9: // alarm
							alarm = 0x0100;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 10: // alarm
							alarm = 0x0200;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 11: // alarm
							alarm = 0x0400;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 12: // alarm
							alarm = 0x0800;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 13: // alarm
							alarm = 0x1000;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 14: // alarm
							alarm = 0x2000;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 15: // alarm
							alarm = 0x4000;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 16: // alarm
							alarm = 0x8000;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 17: // alarm
							alarm = 0x0000;
							status = 0x0000;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 18: // alarm
							// create message and add to queue
							alarm = 0x0000;
							status = 0x0001;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 19: // alarm
							// create message and add to queue
							alarm = 0x0000;
							status = 0x0002;
							sdo.setAlarmMask(alarm);
							sdo.setStatusMask(status);
							sdo.setPriority(1);
							sdo.setAsAlertEvent();
							SerialMessagingQueue.getInstance().add(sdo);
							break;
						case 20: // alarm
							System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!Send WaveForm");
							float newWaveform[] = new float[1000];
							System.out.println("Front = " + waveformFront + " Offset = " + waveformOffset);
							int x = waveformFront;
							int y = waveformOffset;
							int z = 0;

							while (x != y) {
								newWaveform[z] = waveform[y];
								// System.out.print(" " + newWaveform[z] + "<" +
								// z + ">");
								z++;
								x++;
								if (x > 1099)
									x = 0;
							}
							sdo.setAsWaveformEvent();
							sdo.setWaveform(newWaveform);
							sdo.setPriority(1);
							SerialMessagingQueue.getInstance().add(sdo);
							System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!Sent WaveForm");
							break;
						case 26:
							try {
								OTAMessageMO msg = new OTAMessageMOE(0);
								msg.overrideTimestamp(DiagnosticsThread.getSystemDateTime());

								String imei = null;
								String iccid = null;

								msg.setSeqId(1);
								// add imei
								imei = DiagnosticsThread.getIMEI();
								if (imei != null && imei.length() > 0) {
									msg.addObject(new OTA_Object_String(0, imei));
								} else {
									throw new Exception("invalid IMEI <" + imei + ">");
								}

								// add iccid
								iccid = DiagnosticsThread.getICCID();
								if (iccid != null && iccid.length() > 0) {
									msg.addObject(new OTA_Object_String(2, iccid));
								}
								System.out.println("imei=<" + imei + ">, iccid=<" + iccid + ">");

								// add diagnostics to the payload
								try {
									msg.addObject(new OTA_Object_Int(100, (byte) DiagnosticsThread.checkRSSI()));
									msg.addObject(new OTA_Object_Int(101, DiagnosticsThread.getMOPriorityQueueSize()));
									msg.addObject(new OTA_Object_Int(102, DiagnosticsThread.getMOPeriodicQueueSize()));
									msg.addObject(new OTA_Object_Int(103, DiagnosticsThread.getMORetryQueueSize()));
									msg.addObject(new OTA_Object_Int(104, DiagnosticsThread.getSerialQueueSize()));
									msg.addObject(new OTA_Object_Int(105, 0));
								} catch (Exception e) {
									System.out.println(e);
									e.printStackTrace();
								}

								// add SIM card pump controller values
								try {
									if (SIM.isSIMReady() == true) {
										msg.addObject(new OTA_Object_String(106, SIM.getUnitID()));
										msg.addObject(new OTA_Object_Byte(107, SIM.getGain()));
										msg.addObject(new OTA_Object_Byte(108, SIM.getBalance()));
										msg.addObject(new OTA_Object_Byte(109, SIM.getNormA()));
										msg.addObject(new OTA_Object_Byte(110, SIM.getNormB()));
										msg.addObject(new OTA_Object_Byte(111, (byte) 1));
									} else {
										msg.addObject(new OTA_Object_Byte(111, (byte) 0));
									}
								} catch (Exception e) {
									System.out.println(e);
									e.printStackTrace();
								}

								// translator.object.map.3=event_code

								// event code 0x0ff = power up
								// msg.addObject(new OTA_Object_Int(3, 0));
								// //x0ff));
								// msg.addObject(new OTA_Object_Int(4, 0));

								Message message = new Message(msg.getSeqId(), msg.getBytes(), (1000 * 60 * 2));
								MOPriorityMessagingQueue.getInstance().add(message);
							} catch (Exception e) {
								System.out.println(e);
								e.printStackTrace();

							}
							break;

						}
						nextMessage++;
						if (nextMessage > 26) {
							nextMessage = 0;
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
}
