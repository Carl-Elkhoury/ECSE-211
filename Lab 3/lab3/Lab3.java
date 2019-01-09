package ca.mcgill.ecse211.lab3;

import ca.mcgill.ecse211.lab3.Display;
import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**In Lab3 our robot takes individual points from an array of coordinates, calls upon the navigation class to calculate the needed angle and distance
 * and executes a series of actions to follow the array's path with minimal error.
 * This class is the brain of our robot. It takes user input and decides what thread to run in order to get the desired
 * action from the  robot. **/

public class Lab3 {
	  // Motor Objects, and Robot related parameters
	  private static final EV3LargeRegulatedMotor leftMotor =
	      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	  private static final EV3LargeRegulatedMotor rightMotor =
	      new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	  private static final Port usPort = LocalEV3.get().getPort("S1");
	  private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	  private static double coordinates2[][]= {{1,1},{0,2},{2,2},{2,1},{1,0}};
	  private static double coordinates3[][] = {{1,0},{2,1},{2,2},{0,2},{1,1}};
	  private static double coordinates4[][] = {{0,1},{1,2},{1,0},{2,1},{2,2}};
	  public static final double WHEEL_RAD = 2.1;
	  public static final double TRACK = 12.7;
	  public static Display odometryDisplay;

	  public static void main(String[] args) throws OdometerExceptions {

	    int buttonChoice;

	    // Odometer related objects
	    Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD); // TODO Complete implementation
	    final Navigation navigation = new Navigation();					//create an instance of the navigation class so that it's methods can be called.
	                                                                      // implementation
	    odometryDisplay = new Display(lcd); // No need to change
	    SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
	    SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
	                                                              // this instance
	    float[] usData = new float[usDistance.sampleSize()]; 
	    UltrasonicPoller usPoller = null;
	    


	    do {
	      // clear the display
	      lcd.clear();

	      // ask the user whether the motors should drive in a square or float
	      lcd.drawString("< Left | Right >", 0, 0);
	      lcd.drawString("       |        ", 0, 1);
	      lcd.drawString(" Float | Drive  ", 0, 2);
	      lcd.drawString("motors |  to    ", 0, 3);
	      lcd.drawString("       | coord  ", 0, 4);

	      buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)
	    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

	    if (buttonChoice == Button.ID_LEFT) {
	      // Float the motors
	      leftMotor.forward();
	      leftMotor.flt();
	      rightMotor.forward();
	      rightMotor.flt();

	      // Display changes in position as wheels are (manually) moved
	      Thread odoThread = new Thread(odometer);
	      odoThread.start();
	      Thread odoDisplayThread = new Thread(odometryDisplay);
	      odoDisplayThread.start();

	    } else {
	      // clear the display
	      lcd.clear();
	      ObstacleAvoid obstacleAvoid = new ObstacleAvoid(navigation);
	      // ask the user whether odometery correction should be run or not
	      lcd.drawString("< Left | Right >", 0, 0);
	      lcd.drawString("simple | with   ", 0, 1);
	      lcd.drawString(" navig-| obst-  ", 0, 2);
	      lcd.drawString(" ation | acle   ", 0, 3);
	      lcd.drawString("       |        ", 0, 4);

	      buttonChoice = Button.waitForAnyPress(); 									// Record choice (left or right press)

	      // Start odometer and display threads
	      Thread odoThread = new Thread(odometer);
	      odoThread.start();
	      Thread odoDisplayThread = new Thread(odometryDisplay);
	      odoDisplayThread.start();
	      
	      // Start correction if right button was pressed
	      if (buttonChoice == Button.ID_RIGHT) {									//if user chooses the obstacle avoidance option, the ultrasonic poller is initialized.
	    	  usPoller = new UltrasonicPoller(usDistance, usData, obstacleAvoid );
	          usPoller.start();
	      }
	      
	      (new Thread() {
	          public void run() {													//iterates through every coordinate in selected map
	        	  for(int i=0; i<coordinates3.length ; i++)
	        		  navigation.travelTo(coordinates3[i][0], coordinates3[i][1]);
	          }
	        }).start();
	      }
	    

	    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
	    System.exit(0);
	  }
	  
	  public static EV3LargeRegulatedMotor getLeftMotor() {			//creating public methods to allow for calling of motors and key values in other classes
		  return leftMotor;
	  }
	  public static EV3LargeRegulatedMotor getRightMotor() {
		  return rightMotor;
	  }
	  
	  public static double getRadius() {
		  return WHEEL_RAD;
	  }
	  
	  public static double getTrack() {
		  return TRACK;
	  }
	  
	  
	}

