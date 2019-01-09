package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.motor.EV3LargeRegulatedMotor;


/**This controller steers the robot away from the wall by polling the ultrasonic sensor and using the distance from the wall to proportionally alter the wheel speeds to turn right.
 * If the robot is at a safe distance from the wall it is running in a constant left turn until it approaches the wall again and the proportional right turn is triggered again. **/
public class PController implements UltrasonicController {

  /* Constants */
  private static final int MOTOR_SPEED = 160;                                                                                                                                                                  
  private static final int FILTER_OUT = 20;
  private final int extraSpeed = 165;
  private final int speedMultiple = 100;

  private final int bandCenter;
  private final int bandWidth;
  private int distance;
  private int filterControl;

  public PController(int bandCenter, int bandwidth) {
    this.bandCenter = bandCenter;
    this.bandWidth = bandwidth;
    this.filterControl = 0;

    WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED); // Initalize motor rolling forward
    WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance) {

    // rudimentary filter - toss out invalid samples corresponding to null
    // signal.
    // (n.b. this was not included in the Bang-bang controller, but easily
    // could have).
    //
    if (distance >= 255 && filterControl < FILTER_OUT) {
      // bad value, do not set the distance var, however do increment the
      // filter value
      filterControl++;
    } else if (distance >= 255) {
      // We have repeated large values, so there must actually be nothing
      // there: leave the distance alone
      this.distance = distance;
    } else {
      // distance went below 255: reset filter and leave
      // distance alone.
      filterControl = 0;
      this.distance = distance;
    }

    // TODO: process a movement based on the us distance passed in (P style)
    
    if(this.distance < bandCenter - bandWidth) {												// steers robot right at a speed dependent on robots distance from the wall
    		 WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED - speedMultiple*( bandCenter -this.distance )); 	//slow forward moving left wheel if very close to the wall 
    		 																					//because of proportionality provided by dynamic distance variable.
         WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED + speedMultiple*( bandCenter -this.distance));	//fast reverse turning right wheel if very close to the wall
         																					//becayse of proportionality provided by the always changing distance variable.
         WallFollowingLab.leftMotor.forward();		
         WallFollowingLab.rightMotor.backward();								
    	
    }
    
    else {
    		WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED); 						// natural state of robot when at safe distance, constant left turning.
         WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED + extraSpeed);					// values derived from pure trial and error.
         WallFollowingLab.leftMotor.forward();
         WallFollowingLab.rightMotor.forward();
     }
    	
    }
  
  


  @Override
  public int readUSDistance() {
    return this.distance;
  }

}
