package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {

  private final int bandCenter;
  private final int bandwidth;
  private final int motorLow;
  private final int motorHigh;
  private int distance;
  private final int tooClose=14;
  private final int largeBoost =70;
  private final int smallBoost=20;
  private final int emergencyLeft=100;
  private final int emergencyRight=350;
  
  /** This controller steers our robot away from the structure by calling upon pre determined wheel speeds when certain thresholds of distance are read on the scanner. 
   *  Four seperate cases were implemented in order of importance, an emergency reverse, a normal left turn when close, a normal right turn when far and a straight drive 
   *  when within acceptable range. Each case uses constant wheel speed and does not use the distance from the wall for anything other than triggering the cases. **/

  public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
    // Default Constructor
    this.bandCenter = bandCenter;
    this.bandwidth = bandwidth;
    this.motorLow = motorLow;
    this.motorHigh = motorHigh;
    WallFollowingLab.leftMotor.setSpeed(motorHigh); // Start robot moving forward
    WallFollowingLab.rightMotor.setSpeed(motorHigh);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {
    this.distance = distance;
    
    
    // TODO: process a movement based on the us distance passed in (BANG-BANG style)
    
    
    
    if(this.distance < tooClose) {					//emergencry reverse manoeuver when robot is dangerously close.
		WallFollowingLab.leftMotor.setSpeed(emergencyLeft);
		WallFollowingLab.rightMotor.setSpeed(emergencyRight);
		WallFollowingLab.leftMotor.backward();
		WallFollowingLab.rightMotor.backward();
    }
    
    else if (this.distance < bandCenter - bandwidth) {			//when robot is too close to the wall but not in danger zone, stear forward left.
		WallFollowingLab.leftMotor.setSpeed(motorHigh + largeBoost);
		WallFollowingLab.rightMotor.setSpeed(motorLow);
		WallFollowingLab.leftMotor.forward();
		WallFollowingLab.rightMotor.forward();
}
    
    else if(this.distance > bandCenter + bandwidth) {			//steer left when robot is too far from wall
    		WallFollowingLab.leftMotor.setSpeed(motorLow);
    		WallFollowingLab.rightMotor.setSpeed(motorHigh +smallBoost);
    		WallFollowingLab.leftMotor.forward();
 		WallFollowingLab.rightMotor.forward();
    }
    
    else {													//final else to allow robot to roll straight if its within error range.
    		WallFollowingLab.leftMotor.setSpeed(motorHigh);
		WallFollowingLab.rightMotor.setSpeed(motorHigh);
		WallFollowingLab.leftMotor.forward();
		WallFollowingLab.rightMotor.forward();
    }
	
}
    
    
  

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}
