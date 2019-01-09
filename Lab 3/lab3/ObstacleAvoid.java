package ca.mcgill.ecse211.lab3;

import ca.mcgill.ecse211.odometer.OdometerData;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class ObstacleAvoid implements UltrasonicController{

	private static final int FILTER_OUT = 20;
	private int filterControl;
	private int distance;
	private static Navigation navigation;
	private static final double Radius = Lab3.getRadius();
	private static final double track = Lab3.getTrack();
	private static final int blockLength = 28;
	private static final int blockWidth =48;
	private boolean check =false;
	private boolean block =false;

	private static EV3LargeRegulatedMotor leftMotor = Lab3.getLeftMotor();
	private static EV3LargeRegulatedMotor rightMotor = Lab3.getRightMotor();



	public ObstacleAvoid(Navigation navigation) {
		this.navigation = navigation;

	}
	/** continuously polls ultrasonic Sensor for distance, if distance threshold is met, avoid the obstacle through the hardcoded
	 * square turn around the block. Also decides whether to perform the turn on the left or right depending on readings from the 
	 * Ultrasonic sensor after the first 90 degree turn. **/
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
		if(block)				//if a block has already been avoided, do nothing.
			return;
		
		if(navigation.obstacle) {						//if you've already seen an obstacle
			if(this.distance < 35 ) {					//check if there is a wall in front, this indicates that a left turn around the block is the wrong path and will perform the right turn
				check = true;
				leftMotor.rotate(-convertAngle(Radius, track, 180.0),true);			//180 degree turn to begin the right sided path around the block.
				rightMotor.rotate(convertAngle(Radius, track, 180.0), false);
				
				leftMotor.rotate(convertDistance(Radius, blockLength),true);
				rightMotor.rotate(convertDistance(Radius, blockLength),false);

				leftMotor.rotate(-convertAngle(Radius,track,90.0),true);
				rightMotor.rotate(convertAngle(Radius, track, 90.0), false);

				leftMotor.rotate(convertDistance(Radius, blockWidth),true);
				rightMotor.rotate(convertDistance(Radius, blockWidth),false);

				leftMotor.rotate(-convertAngle(Radius,track,90.0),true);
				rightMotor.rotate(convertAngle(Radius, track, 90.0), false);

				leftMotor.rotate(convertDistance(Radius, blockLength),true);
				rightMotor.rotate(convertDistance(Radius, blockLength),false);

				leftMotor.rotate(convertAngle(Radius, track, 90.0),true);
				rightMotor.rotate(-convertAngle(Radius, track, 90.0), false);
				navigation.obstacle = false;
				block =true;
			}else {
				check=true;
				leftMotor.rotate(convertDistance(Radius, blockLength),true);		//left sided path around block
				rightMotor.rotate(convertDistance(Radius, blockLength),false);

				leftMotor.rotate(convertAngle(Radius,track,90.0),true);
				rightMotor.rotate(-convertAngle(Radius, track, 90.0), false);

				leftMotor.rotate(convertDistance(Radius, blockWidth),true);
				rightMotor.rotate(convertDistance(Radius, blockWidth),false);

				leftMotor.rotate(convertAngle(Radius,track,90.0),true);
				rightMotor.rotate(-convertAngle(Radius, track, 90.0), false);

				leftMotor.rotate(convertDistance(Radius, blockLength),true);
				rightMotor.rotate(convertDistance(Radius, blockLength),false);

				leftMotor.rotate(-convertAngle(Radius, track, 90.0),true);
				rightMotor.rotate(convertAngle(Radius, track, 90.0), false);
				navigation.obstacle = false;
				block =true;												//ensures it limits its block avoidance to a single block and is not swayed by sensing a wall in the future.
			}
		}
		
		if(distance<15 && navigation.isNavigating() && !navigation.obstacle && !check) { //if the distance too close, robot is navigating, no obstacle detected yet and no turn being performed
			navigation.obstacle=true;													//sets obstacle boolean to true and makes navigation class wait.
			leftMotor.setSpeed(1);														//slows down both motors before stopping to avoid turning error experienced during testing.
			rightMotor.setSpeed(1);							
			try {
				Thread.sleep(500);														//wait half second for motor slowdown.
			} catch (InterruptedException e1) {
				
			}
			rightMotor.stop();
			leftMotor.stop();
			leftMotor.setSpeed(150);
			rightMotor.setSpeed(150);
	
			leftMotor.rotate(-convertAngle(Radius, track, 90.0),true);					// turn the robot left
			rightMotor.rotate(convertAngle(Radius, track, 90.0), false);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {											//sleep that was found useful for error avoidance during testing.
			}
		}
		check =false;				

	}
	public int readUSDistance() {
		return this.distance;
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}


}
