package ch.aplu.ev3;
import ch.aplu.ev3.*;

public class TouchDemo
{
  public TouchDemo()
  {
    LegoRobot robot = new LegoRobot();
    Gear gear = new Gear();
    robot.addPart(gear);
    TouchSensor ts = new TouchSensor(SensorPort.S1);
    robot.addPart(ts);
    gear.forward();
    
    while (!Tools.isEscapePressed())
    {
      if (ts.isPressed())
      {
        gear.backward(1000);
        gear.left(500);
        gear.forward();
      }
      Tools.delay(1);
    }
    robot.exit();
  }
  
  public static void main(String[] args)
  {
    new TouchDemo();
  }
} 
