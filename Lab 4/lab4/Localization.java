package ca.mcgill.ecse211.lab4;

import ca.mcgill.ecse211.odometer.OdometerData;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class Localization extends Thread{
	public static Navigation navigation = Lab4.navigation;
	public static boolean reached=false;
	public static boolean fallingEdge;
	public static final double distancethr=35;
	public static double alpha;
	public static double beta;
	private static boolean stopped =false; 
	private static EV3LargeRegulatedMotor leftMotor = Lab4.getLeftMotor();
	private static EV3LargeRegulatedMotor rightMotor = Lab4.getRightMotor();
	private static OdometerData odo;
	boolean nextStep=false;
	private static final EV3ColorSensor colorSensor=new EV3ColorSensor(LocalEV3.get().getPort("S2"));
	SampleProvider usSensor=colorSensor.getMode("Red");
	float [] usData=new float [usSensor.sampleSize()];
	private double currentPosition[];
	private double radius = Lab4.getRadius();
	private double track = Lab4.getTrack();
	private double array[] = new double[4];
	
	
	public Localization(boolean fallingEdge){
		this.fallingEdge=fallingEdge; //  records if the user inputed falling or rising edge detection
	}
	
	/**This method combines calculations from the odometry and navigation class with readings from an ultrasonic sensor and light sensor in order to correctly
	 * position itself at the relative grid origin no matter the start position. It first uses the US to orient itself facing 0 degrees and next moves forward 
	 * and performs a 360 degree turn while detecting four instances of grid lines and storing the current theta when detected. Those stored angles are then 
	 * used to accurately locate the current position of the robot and move to the origin.**/

	public void run(){
		int count =0;
		angleLocalization();
		while(!nextStep) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

		leftMotor.rotate(300,true);
		rightMotor.rotate(300,false);
		
		
		navigation.turn360(true);
		while(nextStep) {
			usSensor.fetchSample(usData, 0);
//			if(count==1) {
//				leftMotor.rotate(convertAngle(radius, track, 90), true);
//			    rightMotor.rotate(-convertAngle(radius, track, 90), false);	
//				leftMotor.forward();
//				rightMotor.forward();
//				count++;
//			}

			if(usData[0]<0.25) {				//set the threshold for the line detection 
				Sound.beep();
				if(count !=4)
					array[count++]=Lab4.odometryDisplay.getXYT()[2];	//storing the angle it was detected at.
			}
			
			if(count == 4) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				double deltaTx=array[1]-array[3];				//delta angles at second and fourth detected lines
				double d= 14.3;
				double y = -d*Math.cos(deltaTx*Math.PI/360);	//calculation of y position
				double deltaTy=array[0]-array[2];				//delta angles at first and third detected lines
				double x = -d*Math.cos(deltaTy*Math.PI/360);	//calculation of x position
				double theta = Math.atan(x/y)*180/Math.PI;		
				double distance = Math.sqrt(x*x+y*y);
				double correctionAngle = array[0]-(deltaTy/2)-270;		
//				System.out.println("x= "+x+" y= "+y+" correction= "+(correctionAngle));
				Sound.beep();
				leftMotor.rotate(-convertAngle(radius, track , theta+correctionAngle),true);		//turns to the origin point	
				rightMotor.rotate(convertAngle(radius, track , theta+correctionAngle),false);
				leftMotor.rotate(convertDistance(radius, distance),true);							//moves to the point
				rightMotor.rotate(convertDistance(radius, distance),false);	
				navigation.turnTo(0);																//turn back forward
				Lab4.odometryDisplay.odo.setXYT(0, 0, 0);											//display origin position when reached

				count++;
				
			}
						
		}
	}
	/**This method reads distance values from the ultrasonic sensor and detects rapid changes in the distances during movement. Depending
	 * on the direction of the change in value, it is considered a rising or falling edge. Two edges are detected during the sequence
	 * and the robots angle at the moment of each detection is used to calculate the angle needed to accurately orient the robot at 
	 * 0 degrees.**/
	
	private void angleLocalization() {
		navigation.turn360(true);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
		}
		while(!stopped)
			if(fallingEdge) {
				if(Lab4.obstacleDetect.lastDistance-Lab4.obstacleDetect.distance>distancethr && Lab4.obstacleDetect.distance<60) {	//triggered if falling edge is detected
					if(!reached) {			//if its the first falling edge detected
						reached =true;	
						alpha=Lab4.odometryDisplay.getXYT()[2];		//store the first angle
						leftMotor.setSpeed(1);						//slow the motors to reduce error when stopping
						rightMotor.setSpeed(1);
						leftMotor.stop();						
						rightMotor.stop();
						Sound.beep();
						navigation.turn360(false);
						try {
							Thread.sleep(1000);						//one second pause to stop sensor from returning false positive.
						} catch (InterruptedException e) {
						}
					}else {
						beta=Lab4.odometryDisplay.getXYT()[2];		//if its not the first falling edge detected store the angle
						leftMotor.setSpeed(1);
						rightMotor.setSpeed(1);
						leftMotor.stop();
						rightMotor.stop();
						stopped=true;								//set boolean to true to ensure to it doesnt run this sequence again.
						Sound.beep();
					}
				}
			}else {
				if(Lab4.obstacleDetect.distance-Lab4.obstacleDetect.lastDistance>distancethr) {			//same logic as above but for rising edge trigger.
					if(!reached) {
						reached =true;
						alpha=Lab4.odometryDisplay.getXYT()[2];
						leftMotor.setSpeed(1);
						rightMotor.setSpeed(1);
						leftMotor.stop();
						rightMotor.stop();
						navigation.turn360(false);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}else {
						beta=Lab4.odometryDisplay.getXYT()[2];
						leftMotor.setSpeed(1);
						rightMotor.setSpeed(1);
						leftMotor.stop();
						rightMotor.stop();
						stopped=true;
					}
				}
			}
		if(alpha>beta) {
			double current[] =Lab4.odometryDisplay.getXYT();
			Lab4.odometryDisplay.odo.setTheta(45-(alpha+beta)/2+current[2]);	//sets the new corrected theta.

		}else {
			double current[] =Lab4.odometryDisplay.getXYT();
			Lab4.odometryDisplay.odo.setTheta(225-(alpha+beta)/2+current[2]);	//also sets the new corrected if the first angle measured is less than the second.
		}

		navigation.turnTo(0);													//turn back to 0 degrees/

		currentPosition=Lab4.odometryDisplay.getXYT();
	}
	private static int convertDistance(double radius, double distance) {	//converts distance to wheel rotations
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	private static int convertAngle(double radius, double width, double angle) {	//converts angle to radians for degree rotation
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
