package com.roboclub.robobuggy.sensors;

import gnu.io.SerialPortEvent;

import com.roboclub.robobuggy.main.Robot;
import com.roboclub.robobuggy.messages.ImuMeasurement;
import com.roboclub.robobuggy.ros.Publisher;
import com.roboclub.robobuggy.serial.SerialConnection;

/**
 * 
 * @author Kevin Brennan
 *
 * @version 0.5
 * 
 * CHANGELOG: NONE
 * 
 * DESCRIPTION: TODO
 */

public class Imu extends SerialConnection implements Sensor {
	/** Header for choosing serial port */
	private static final String HEADER = "#ACG=";
	/** Baud rate for serial port */
	private static final int BAUDRATE = 57600;
	/** Index of accel x data as received during serial communication */
	private static final int AX = 0;
	/** Index of accel y data as received during serial communication */
	private static final int AY = 1;
	/** Index of accel z data as received during serial communication */
	private static final int AZ = 2;
	/** Index of gyro x data as received during serial communication */
	private static final int RX = 3;
	/** Index of gyro y data as received during serial communication */
	private static final int RY = 4;
	/** Index of gyro z data as received during serial communication */
	private static final int RZ = 5;
	/** Index of magnetometer x data as received during serial communication */
	private static final int MX = 6;
	/** Index of magnetometer y data as received during serial communication */
	private static final int MY = 7;
	/** Index of magnetometer z data as received during serial communication */
	private static final int MZ = 8;
	// how long the system should wait until a sensor switches to Disconnected
	private static final long SENSOR_TIME_OUT = 5000;

	private SensorType thisSensorType;
	
	long lastUpdateTime;

	private Publisher imuPub;

	private SensorState currentState;

	public double angle;
	
	public Imu(String publishPath) {
		super("IMU", BAUDRATE, HEADER);
		publisher = new Publisher("/sensor/IMU");
		thisSensorType = SensorType.IMU;
	}

	public boolean reset(){
		//TODO
		return false;
	}
	
	@Override
	public long timeOfLastUpdate() {
		return lastUpdateTime;
	}

	@Override
	public SensorState getState() {
		if (System.currentTimeMillis() - lastUpdateTime > SENSOR_TIME_OUT) {
			currentState = SensorState.DISCONECTED;
		} 
		return currentState;
	}

	@Override
	public SensorType getSensorType() {
		return thisSensorType;
	}
	
	@Override
	public boolean isConnected() {
		return this.connected;
	}



	@Override
	public boolean close() {
		try {
			input.close();
			output.close();
			port.close();
			return true;
		} catch (Exception e) {
			System.out.println("Failed to Close Port: " + this.getName());
		}
		
		return false;
	}

	@Override
	public void publish() {
		float aX = 0, aY = 0, aZ = 0, 
				rX = 0, rY = 0, rZ = 0,
				mX = 0, mY = 0, mZ = 0;
		String val = "";
		int state = 0;

		lastUpdateTime = System.currentTimeMillis();
		currentState = SensorState.ON;


		try {
			for (int i = 0; i < index; i++) {
				if (inputBuffer[i] == '\n' || inputBuffer[i] == ',' || i == index) {
					switch (state) {
					case AX:
						aX = Float.valueOf(val);
						break;
					case AY:
						aY = Float.valueOf(val);
						break;
					case AZ:
						aZ = Float.valueOf(val);
						break;
					case RX:
						rX = Float.valueOf(val);
						break;
					case RY:
						rY = Float.valueOf(val);
						break;
					case RZ:
						rZ = Float.valueOf(val);
						break;
					case MX:
						mX = Float.valueOf(val);
						break;
					case MY:
						mY = Float.valueOf(val);
						break;
					case MZ:
						mZ = Float.valueOf(val);
					/*	System.out.println("ax: " + aX + " ay: " + aY + " az: " + aZ + 
								" rx: " + rX + " ry: " + rY + " mx: " + mX + " my: " + mY +
								" mz: " + mZ); */
						angle = rY;
						Robot.UpdateImu(aX, aY, aZ, rX, rY, rZ, mX, mY, mZ);
						publisher.publish(new ImuMeasurement(
								aX, aY, aZ, rX, rY, rZ, mX, mY, mZ));
						
						break;
					}
					
					val = "";
					state++;
				} else {
					val += inputBuffer[i];
				}
			}
		} catch (Exception e) {
			System.out.println("Failed to parse Imu Message");
			currentState = SensorState.ERROR;
		}
	}
	
	/*%*****		Serial Methods			*****%*/
	@Override
	protected void serialWrite(byte[] data) {
		if (connected && output != null) {
			try {
				output.write(data);
				System.out.println("Wrote: " + data);
				//output.flush();
			} catch (Exception e) {
				System.out.println("Unable to write: " + data);
			}
		}
	} 

	@Override
	protected void serialWrite(String data) {
		if (connected && data != null && output != null) {
			try {
				output.write(data.getBytes());
				output.flush();
			} catch (Exception e) {
				System.out.println("Unable to write: " + data);
			}
		}
	}
	
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.DATA_AVAILABLE:
			try {
				char data = (char)input.read();
				
				switch (state) {
				case 0:
					if (data == HEADER.charAt(index)) index++;
					else index = 0;
					
					if (index == HEADER.length()) {
						index = 0;
						state++;
					}
					break;
				case 1:
					inputBuffer[index++] = data;
					
					if (data == '\n' || index >= BUFFER_SIZE) {
						publish();
						index = 0;
						state = 0;
					}
				}
			} catch (Exception e) {
				System.out.println(this.getName() + " exception!");
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}
	}
