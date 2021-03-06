package ca.mcgill.ecse211.lab4;

import ca.mcgill.ecse211.odometer.OdometerData;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/** This class is responsible for the calculations of distance and angle to reach desired coordinate by taking in current position information and running set calculations
 * on the info to solve for needed variables.**/

public class Navigation {

	private static final int FORWARD_SPEED =250;
	private static final int ROTATE_SPEED = 100;
	private static final double TILE_SIZE = 30.48;
	public OdometerData odometerData;
	private static final double radius = Lab4.getRadius();
	private static final double track = Lab4.getTrack();
	
	private static double deltaX;
	private static double deltaY;
	public static double current[];
	boolean stat =false;	
	boolean obstacle =false; 							//boolean that tells navigation there is an obstacle ahead and pauses the travelTo() method.
	private static EV3LargeRegulatedMotor leftMotor = Lab4.getLeftMotor();
	private static EV3LargeRegulatedMotor rightMotor = Lab4.getRightMotor();
	boolean finished360 =false;


	/** takes input of destination coordinates and calculates angle between current position and 
	 * destination. Also calculates distance needed to travel and commands robot to travel the distance. **/
	void travelTo(double x, double y) {					
														
		
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {		//initializes right and left motor.
		      motor.stop();
		      motor.setAcceleration(3000);
		   }
		
		current = Lab4.odometryDisplay.getXYT();		//gets current X Y and Theta values
		deltaX = x*TILE_SIZE- current[0];				//deltaX or deltaY is the difference between where you want to go and where you are currently.
		deltaY = y*TILE_SIZE- current[1];				

		double newTheta;
		if(deltaY==0) {									//series of checks to avoid division by 0 if destination coordinate is on same axis
			if(deltaX<0)
				newTheta=-Math.PI/2;
			else
				newTheta=Math.PI/2;
		}else {

			newTheta = Math.atan(deltaX/deltaY);

			if(deltaY<0) {
					newTheta+= Math.PI;
			}

		}
		turnTo(newTheta);				
		stat = true;					//boolean to say travelTo class is in action and moving forward. Very important for the obstacleAvoid.
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		Sound.beep();					
		leftMotor.rotate(convertDistance(radius, Math.sqrt(deltaX*deltaX + deltaY*deltaY)),true);	//drives to coordinate
		rightMotor.rotate(convertDistance(radius, Math.sqrt(deltaX*deltaX + deltaY*deltaY)),false);
		Sound.beep();
		if(obstacle == true) {			//if there is an obstacle, sleep repeatedly for half a second until the obstacle has been passed.
			while(obstacle)
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			travelTo(x,y);				//after the obstacle has been passed, restart the travelTo function recursively to resume desired path.
		}
		stat=false;						//set to false to indicate that we've reached the current coordinate and the robot is stopped.
	}

	/** adjusts calculated angle, adds or subtracts 2pi to achieve the smallest possible angle when turning to
	 *  coordinate and then turns towards calculated angle relative to the board**/
	
	void turnTo(double theta){	
		current= Lab4.odometryDisplay.getXYT();
		double deltaT= theta- Math.toRadians(current[2]%360);
		deltaT %= 2*Math.PI;								
		if(deltaT>Math.PI)
			deltaT-= 2*Math.PI;
		else if(deltaT<-Math.PI)
			deltaT += 2*Math.PI;

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		deltaT%= Math.PI*2;									//ensures robot doesn't over turn.
		leftMotor.rotate(convertAngle(radius, track, deltaT*180/Math.PI), true);
	    rightMotor.rotate(-convertAngle(radius, track, deltaT*180/Math.PI), false);	

	    
	 }
	/** instructs the robot to sping 360 degrees in a direction specified by the boolean argument. A boolean true argument would
	 * result in clockwise 360 degree turn.**/
	
	void turn360(boolean clockWise){
		double angle =360;
		if(!clockWise)			
			angle= -angle;
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(convertAngle(radius, track, angle), true);
	    rightMotor.rotate(-convertAngle(radius, track, angle), true);	
	}
	boolean isNavigating() {								//boolean created to indicate that travelTo() is currently running.
		return stat;
	}
	
	private static int convertDistance(double radius, double distance) {	//converts distance to wheel rotations
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {	//converts angle to radians for degree rotation
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}

