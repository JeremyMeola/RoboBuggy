package com.roboclub.robobuggy.nodes;

import com.roboclub.robobuggy.messages.EncoderMeasurement;
import com.roboclub.robobuggy.messages.StateMessage;
import com.roboclub.robobuggy.ros.Publisher;
import com.roboclub.robobuggy.ros.SensorChannel;
import com.roboclub.robobuggy.sensors.Arduino;
import com.roboclub.robobuggy.sensors.SensorState;
import com.roboclub.robobuggy.sensors.SensorType;

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

public class EncoderNode extends Arduino {
	private static final long TICKS_PER_REV = 5;
	private static final double M_PER_REV = 5.0;
	private static final int MAX_TICKS = 0xFFFF;
	
	private int encReset;
	private int encTicks;
	private int encTime;
	private double distLast;
	
	public EncoderNode(SensorChannel sensor) {
		super(sensor, "Encoder");
		msgPub = new Publisher(sensor.getMsgPath());
		statePub = new Publisher(sensor.getStatePath());
		sensorType = SensorType.ENCODER;
		statePub.publish(new StateMessage(this.currState));

	}
	
	private void estimateVelocity() {
		double dist = ((double)(encTicks)/TICKS_PER_REV) / M_PER_REV;
		double velocity = (dist - distLast)/ (double)encTime;
		distLast = dist;
		msgPub.publish(new EncoderMeasurement(dist, velocity));
	}
	
	/* Methods for reading from Serial */
	@Override
	public boolean validId(char value) {
		switch (value) {
			case ENC_TIME:
			case ENC_RESET:
			case ENC_TICK:
			case ERROR:
			case MSG_ID:
				return true;
			default:
				return false;
		}
	}
	
	@Override
	public void publish() {
		lastUpdateTime = System.currentTimeMillis();
		
		System.out.println("publishing encoder");
		
		int value = parseInt(inputBuffer[1], inputBuffer[2],
				inputBuffer[3], inputBuffer[4]);
		try {
			switch (inputBuffer[0]) {
			case ENC_TIME:
				encTime = value;
				break;
			case ENC_RESET:
				encReset = value;
				break;
			case ENC_TICK:
				encTicks = value;
				estimateVelocity();
				break;
			case ERROR:
				// TODO handle errors
				break;
			}
		} catch (Exception e) {
			System.out.println("Encoder Exception on port: " + this.getName());
			if (this.currState != SensorState.FAULT) {
				this.currState = SensorState.FAULT;
				statePub.publish(new StateMessage(this.currState));
			}
			return;
		}
		
		if (this.currState != SensorState.ON) {
			this.currState = SensorState.ON;
			statePub.publish(new StateMessage(this.currState));
		}
	}
	
	
}
