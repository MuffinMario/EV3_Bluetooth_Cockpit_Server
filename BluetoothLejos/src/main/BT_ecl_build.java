package main;

/**
  *
  * description
  *
  * @version 1.0 from 22.11.2017
  * @author 
  */
import ch.aplu.ev3.*;
import lejos.hardware.Sound;
import java.io.*;
import java.util.Random;

import lejos.remote.nxt.*;
import lejos.hardware.*;

public class BT_ecl_build {
	/* Car */
	private MotorPort defaultMotorPort1 = MotorPort.B;
	private MotorPort defaultMotorPort2 = MotorPort.C;
	private MotorPort defaultRotationPort = MotorPort.A;
	private Driver mah_car = new Driver(defaultMotorPort1, defaultMotorPort2, defaultRotationPort);

	/* BlueTooth stuff */
	private BluetoothConnection2 con;
	private BluetoothConnector2 bt;
	private BufferedReader reader;

	/* Sound thread */
	private Thread t;

	/* Debug stuff */
	private File debugFile = new File("BT_dbg.txt");
	private FileWriter writer;
	/* Direction of car (forward/stop/backwards) */
	private int move_direction = Driver.MOVE_DIRECTION_STOP;
	/* Rotation of car */
	private int rotation = 0;
	/* Speed of car */
	private int speed = 0;
	private int previous_speed = 0;
	/* Is car powered (User choice) */
	private boolean power = true;
	
	/* User connected ? */
	private boolean initiated = false;
	

	/* Tells if beeping is enabled */
	private boolean beep = false;

	/* "Warning" last play time in time-since-epoch */
	private long warning_last = 0L;
	/* "Warning" cooldown */
	private static final long WARNING_COOLDOWN = 1800L; // ms

	BT_ecl_build() {
		try {
			writer = new FileWriter(debugFile);
			writer.flush();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}

	}
	public void onExit()
	{
		mah_car.setRotation(rotation = 0);
		mah_car.setSpeed(speed = 0);
		mah_car.setDirection(move_direction = Driver.MOVE_DIRECTION_STOP);
		mah_car.exit();
		playSoundFile("voice_standby.wav");
	}
	// toodo: expand but unneccesary
	void initKeyListener(Key key) {
		key.addKeyListener(new KeyListener() {
			public void keyPressed(Key b) {

			}

			public void keyReleased(Key b) {
				if(mah_car != null)
				{
					onExit();
				}
				try {
					
				} catch (Exception e) {

				}
				System.exit(0);
			}
		});
	}

	void playRussianAnthem() {
		playSoundFile("russian_anthem.wav");
	}
	public void handleUltraSonicNear()
	{
		if(warning_last + WARNING_COOLDOWN < System.currentTimeMillis() && power && initiated)
		{
			warning_last = System.currentTimeMillis();
			playSoundFile("voice_warning.wav");
		}
	}
	
	void initMahCar() {
		// beep stuff
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					try {
						if (beep) {
							Sound.playTone(800, 500, 10);
							Thread.sleep(400);
						}
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						break;
					}
				}
			}

		});
		t.start();

		// Adding Listeners ... later
		mah_car.addListener(new TouchListener() {
			@Override
			public void pressed(SensorPort port) {
				sendMessage("OUCH! Stopping car!");
				stop();
			}

			@Override
			public void released(SensorPort port) {

			}
		}, SensorPort.S1);
		mah_car.addListener(new UltrasonicListener() {
			
			@Override
			public void near(SensorPort port, int level) {
				// TODO Auto-generated method stub
				handleUltraSonicNear();
			}
			
			@Override
			public void far(SensorPort port, int level) {
				// TODO Auto-generated method stub
				
			}
		}, SensorPort.S3,30);
		mah_car.addListener(new UltrasonicListener() {
			
			@Override
			public void near(SensorPort port, int level) {
				// TODO Auto-generated method stub
				handleUltraSonicNear();
			}
			
			@Override
			public void far(SensorPort port, int level) {
				// TODO Auto-generated method stub
				
			}
		}, SensorPort.S4,30);
		// options
		mah_car.setAcceleration(255);
	}

	void sendMessage(String msg) {
		if (con != null) {
			msg += "\r\n";
			con.write(msg.getBytes(), msg.length());
		}
	}

	void handleMessage(String msg) {
		String packetHeader = msg.trim();
		try {

			if (packetHeader.startsWith("O")) {
				power = Integer.parseInt(packetHeader.substring(1)) != 0 ? true : false;
				if (power) {
					mah_car.setSpeed(speed);
					mah_car.setDirection(move_direction);
					if (move_direction == Driver.MOVE_DIRECTION_FORWARD) {
						startBeeping();
					}
				} else {
					mah_car.setDirection(Driver.MOVE_DIRECTION_STOP);
					if (move_direction == Driver.MOVE_DIRECTION_FORWARD) {
						stopBeeping();
					}
				}
			} else if (packetHeader.startsWith("S")) {
				rotation = Integer.parseInt(packetHeader.substring(1));
				// sendMessage(packetHeader.substring(1));
				mah_car.setRotation(rotation);
			} // end of if-else
			else if (packetHeader.startsWith("M")) {
				speed = Integer.parseInt(packetHeader.substring(1));

				if (speed < 0) {
					move_direction = Driver.MOVE_DIRECTION_FORWARD;
				} else if (speed > 0) {
					move_direction = Driver.MOVE_DIRECTION_BACKWARD;
				} else if (speed == 0) {
					// if(move_direction !=
					move_direction = Driver.MOVE_DIRECTION_STOP;
				} // end of if-else

				if (power) {
					mah_car.setSpeed(speed);
					mah_car.setDirection(move_direction);
					if (previous_speed < 0 && speed >= 0) {
						// Meaning it was beeping
						stopBeeping();
					} else if (speed < 0 && previous_speed >= 0) {
						startBeeping();
					}
					previous_speed = speed;
				}

			} // end of if-else
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			String error = errors.toString();
			try {

				writer.write(error);
				writer.flush();
			} catch (IOException ioe) {
				System.out.println("aahhhh");
			}
		}
	}

	private synchronized void print(String str) {
		System.out.println(str);
	}

	void initBTConnector() {
		bt = new BluetoothConnector2();
	}

	class SoundFileRunnable implements Runnable {
		String soundFile;

		public SoundFileRunnable(String file) {
			soundFile = file;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			File sound_all_systems_ready = new File(soundFile);
			Sound.playSample(sound_all_systems_ready);
		}

	}

	void playSoundFile(String soundFile) {
		Thread t = new Thread(new SoundFileRunnable(soundFile));
		t.start();
	}

	void createConnection() {
		Sound.setVolume(100);
		playSoundFile("voice_activate.wav");
		con = (BluetoothConnection2) bt.waitForConnection(10000, NXTConnection.RAW);
		reader = new BufferedReader(new InputStreamReader(con.openInputStream()), 1);
		initiated = true;
		playSoundFile("voice_ready.wav");
		OnConnectSend();
	}
	/* Information about the car */
	private void sendInformation()
	{
		sendMessage("I|" + this.speed + "|" + this.rotation + "|" + this.move_direction + "|" + (this.power?"1":"0"));
	}
	private void OnConnectSend() {
		sendInformation();
	}

	void stop() {
		mah_car.getMotor(1).setAcceleration(255);
		mah_car.getMotor(2).setAcceleration(255);
		mah_car.getMotor(1).stop();
		mah_car.getMotor(2).stop();
	}

	void go() {
		mah_car.getMotor(1).setAcceleration(50);
		mah_car.getMotor(2).setAcceleration(50);
		mah_car.getMotor(1).forward();
		mah_car.getMotor(2).forward();
	}

	boolean wantsToExit = false;

	boolean listen() {
		boolean succ = true;
		String message = "";

		try {
			while ((message = reader.readLine()) != null) {
				DebugConsole.show(message);
				handleMessage(message);
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		if (con.eof) {
			succ = false;
		}
		if (wantsToExit)
			succ = false;
		return succ;
	}

	void startBeeping() {
		beep = true;
	}

	void stopBeeping() {
		beep = false;
	}
	void reset()
	{
		mah_car.setSpeed(this.speed = 0);
		mah_car.setDirection(this.move_direction = Driver.MOVE_DIRECTION_STOP);
		mah_car.setRotation(this.rotation = 0);
		this.power = true;
	}
	void disconnect()
	{
		try {
			initiated = false;
			con.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error closing socket: " + e.getMessage());
		}
	}
	void init() {
		DebugConsole.show("Initiating Key Listener...");
		initKeyListener(Button.ENTER);

		DebugConsole.show("Initiating Car...");
		initMahCar();
		

		DebugConsole.show("Starting Connector, waiting for Bluetooth Client...");
		initBTConnector();
		while (wantsToExit == false) {
			createConnection();

			DebugConsole.show("Connection found! ");

			DebugConsole.show("We are now going to listen...");
			while (listen());
			if (!wantsToExit)
			{
				DebugConsole.show("Connection lost, waiting for next Client");
			}
			reset();
			disconnect();
			wantsToExit = true; // 1 round yet because of internal Linux C-Library binding error
		}
		DebugConsole.show("Leaving. Goodbye");
		onExit();
		Tools.delay(3000); //"Standby..." wav
	}

	public static void main(String[] args) {
		BT_ecl_build mainrun = new BT_ecl_build();
		mainrun.init();
		System.exit(0);
	}
}