// RFIDSensor.java

/*
This software is part of the EV3JLib library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.
*/

package ch.aplu.ev3;

/**
 * Class that represents a RFID sensor from CODATEX (www.codatex.com).
 */
public class RFIDSensor extends Sensor
{
  private lejos.hardware.sensor.RFIDSensor rs;
  private boolean isContinuous = false;

  /**
   * Creates a sensor instance connected to the given port.
   * The sensor may be in two modes: Single Read or Continuous Read
   * (default: Single Read). In Single Read mode the sensor is
   * somewhat slower because it returns into a sleep state after
   * 2 seconds of inactivity for power saving reasons. It is woke up
   * automatically at the next call of getTransponderId(). To change the
   * mode, use setContMode().
   * @param port the port where the sensor is plugged-in
   */
  public RFIDSensor(SensorPort port)
  {
    super(port);
    rs = new lejos.hardware.sensor.RFIDSensor(getLejosPort());
  }

  /**
   * Creates a sensor instance connected to port S1.
   */
  public RFIDSensor()
  {
    this(SensorPort.S1);
  }

  protected void init()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("rs.init()");
  }

  protected void cleanup()
  {
    if (LegoRobot.getDebugLevel() >= SharedConstants.DEBUG_LEVEL_MEDIUM)
      DebugConsole.show("rs.cleanup()");
    rs.stop();
    rs.close();
 }

 /**
  * Returns the reference of the the underlying lejos.hardware.sensor.RFIDSensor.
  * @return the reference of the lejos.hardware.sensor.RFIDSensor
  */
  public lejos.hardware.sensor.RFIDSensor getLejosSensor()
  {
    return rs;
  }

  private void checkConnect()
  {
    if (robot == null)
     new ShowError("RFIDSensor is not a part of the LegoRobot.\n" +
        "Call addPart() to assemble it.");
  }

  /**
   * Returns the product identifier (if available).
   * @return the product identifier (PID)
   */
  public String getProductID()
  {
    checkConnect();
    return rs.getProductID();
  }

  /**
   * Returns the sensor version number (if available).
   * @return the sensor version number
   */
  public String getVersion()
  {
    checkConnect();
    return rs.getVersion();
  }

  /**
   * Returns the serial number of the RFID sensor.
   * @return the 12 byte serial number or null, if sensor not available.
   */
  public byte[] getSerialNo()
  {
    return rs.getSerialNo();
  }

  /**
   * Selects between Single Read and Continous Read mode.
   * @param continuous if true, the sensor is put in Continous Read mode; otherwise
   * it is put in Single Read mode.
   */
  public void setContMode(boolean continuous)
  {
    if (isContinuous && !continuous)
       rs.stop();
    isContinuous = continuous;
  }

  /**
   * Polls the sensor. If the sensor is in Single Read mode and in sleep
   * state, it is automatically woke up.
   * @return the current transponder id (0: no transponder detected)
   */
  public long getTransponderId()
  {
    checkConnect();
    return rs.readTransponderAsLong(isContinuous);
  }
}
