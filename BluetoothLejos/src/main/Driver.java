package main;

import ch.aplu.ev3.*;

public class Driver extends LegoRobot {
	protected Motor motorA;
	protected Motor motorB;
	protected MediumMotor rotator;

	public static void print(String str) {
		// System.out.println(str);
		DebugConsole.show(str);
	}

	public Driver(MotorPort a, MotorPort b, MotorPort rot) {
		super();
		motorA = new Motor(a);
		motorB = new Motor(b);
		rotator = new MediumMotor(rot);
		addPart(motorA);
		addPart(motorB);
		addPart(rotator);
		rotator.setSpeed(255); // overkill
		motorA.setSpeed(speed);
		motorB.setSpeed(speed);
		motorA.setAcceleration(acceleration);
		motorB.setAcceleration(acceleration);
	}

	public void setSpeed(int speed) {
		this.speed = speed;
		motorA.setSpeed(speed);
		motorB.setSpeed(speed);
	}

	public static int MOVE_DIRECTION_FORWARD = 0;
	public static int MOVE_DIRECTION_BACKWARD = 1;
	public static int MOVE_DIRECTION_STOP = 2;
	private int direction = MOVE_DIRECTION_STOP;
	int acceleration = 10;
	int speed = 100;
	int rotation = 0;

	public void setAcceleration(int acc) {
		acceleration = acc;
		motorA.setAcceleration(acc);
		motorB.setAcceleration(acc);
	}

	public void setDirection(int direction) {
		assert (direction >= 0 && direction <= 2);
		// if(direction == this.direction)
		// return;
		if (direction == MOVE_DIRECTION_FORWARD) {
			setAcceleration(acceleration);
			System.out.println("yeah forward");
			motorA.forward();
			motorB.forward();
		} // end of if
		else if (direction == MOVE_DIRECTION_BACKWARD) {
			setAcceleration(acceleration);
			motorA.backward();
			motorB.backward();
		} else if (direction == MOVE_DIRECTION_STOP) {
			motorA.setAcceleration(255);
			motorB.setAcceleration(255);
			motorA.stop();
			motorB.stop();
		}
	}

	public MediumMotor getRotator() {
		return rotator;
	}

	public void setRotation(int deg) {
		int difference = deg - rotation;
		rotation = deg;
		rotator.rotateTo(difference);
	}

	public Motor getMotor(int type) {
		if (type == 1) {
			return motorA;
		} else {
			return motorB;
		}
	}

	public boolean addListener(Object listener, SensorPort port) {
		if (listener instanceof UltrasonicListener) {
			UltrasonicSensor sensor = new UltrasonicSensor(port);
			addPart(sensor);
			sensor.addUltrasonicListener((UltrasonicListener) listener);
			print("+Ultrasonic Listener!");
		} // end of if
		else if (listener instanceof TouchListener) {
			TouchSensor sensor = new TouchSensor(port);
			addPart(sensor);
			sensor.addTouchListener((TouchListener) listener);
			print("+Touch Listener!");
		} // end of if-else
		else if (listener instanceof LightListener) {
			LightSensor sensor = new LightSensor(port);
			addPart(sensor);
			sensor.addLightListener((LightListener) listener);
			print("+Light Listener!");
		} // end of if-else
		else {
			return false;
		}
		return true;
	}

	public boolean addListener(Object listener, SensorPort port, int trigger) {
		if (listener instanceof UltrasonicListener) {
			UltrasonicSensor sensor = new UltrasonicSensor(port);
			addPart(sensor);
			sensor.addUltrasonicListener((UltrasonicListener) listener, trigger);
			print("+Ultrasonic Listener!");
		} // end of if
		else if (listener instanceof TouchListener) {
			TouchSensor sensor = new TouchSensor(port);
			addPart(sensor);
			sensor.addTouchListener((TouchListener) listener);
			print("+Touch Listener! (no custom trigger)");
		} // end of if-else
		else if (listener instanceof LightListener) {
			LightSensor sensor = new LightSensor(port);
			addPart(sensor);
			sensor.addLightListener((LightListener) listener, trigger);
			print("+Light Listener!");
		} // end of if-else
		else {
			return false;
		}
		return true;
	}

}