/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
  private static final long CORRECTION_PERIOD = 10;
  private Odometer odometer;
  private static final double TILE_SIZE = 30.48;

  
  //setting up the color sensor with the ev3 machine
  private static final EV3ColorSensor colorSensor=new EV3ColorSensor(LocalEV3.get().getPort("S1"));
  SampleProvider usSensor=colorSensor.getMode("Red");
  float [] usData=new float [usSensor.sampleSize()];

  /**
   * This is the default class constructor. An existing instance of the odometer is used. This is to
   * ensure thread safety.
   * 
   * @throws OdometerExceptions
   */
  public OdometryCorrection() throws OdometerExceptions {
	  

    this.odometer = Odometer.getOdometer();

  }

  /**
   * Here is where the odometer correction code should be run.
   * 
   * @throws OdometerExceptions
   */
  // run method (required for Thread)
  /** Following method acts as a correction system for the square driving path. A counter keeps track of the number of black lines passed
   *  by interacting with the color sensor. Through our knowledge of each square width and the value of the counter, the displayed values are 
   *  corrected as the robot runs.*/
  public void run() {
    long correctionStart, correctionEnd;
    int count = 0;
    int horizontalOffset = 4;
    int verticalOffset = 7;
    while (true) {
      correctionStart = System.currentTimeMillis();

      // TODO Trigger correction (When do I have information to correct?)
      usSensor.fetchSample(usData, 0);
      if(usData[0]<0.24) {								//most accurate tested threshold for detecting black lines, when detected adds to counter and beeps
  	  	Sound.beep();
  	  	count++;
  	  	
      	if(count<=3) {
    	  		odometer.setY((count-1)*TILE_SIZE);			//each edge of the driven square is treated seperately to update distance
      	}
      	if(3<count && count<=6) {
    	  		odometer.setX(((count-1)%3)*TILE_SIZE);		// the % is used to reset the count at every vertice of the square without having to reset the value
      	}
      	if(6<count && count<=9) {
    	  		odometer.setY((2-((count-1)%6)) *TILE_SIZE+verticalOffset); //added an offset to center our values 
      	}
      	if(9<count && count<=12) {
    	  		odometer.setX((2-((count-1)%9)) *TILE_SIZE+horizontalOffset); //added an offset to center our values
      	}
      }
      
      
      
//      if(usData[0]<0.20) {
//    	  	Sound.beep();
//    	  	count++;
//    	  	if(count==1)
//    	  		odometer.setXYT(0, 0, 0);
//    	  	
//      }
    	  	
      
      // TODO Calculate new (accurate) robot position

      // TODO Update odometer with new calculated (and more accurate) vales


      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here
        }
      }
    }
  }
}
