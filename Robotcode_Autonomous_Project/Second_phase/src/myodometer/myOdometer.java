package myodometer;

import robocode.*;

public class myOdometer {
	public Robot robot;
    public double pastX=-1,pastY=-1,distance = 0;

	public myOdometer(Robot robot) {
		this.robot = robot;
		this.distance = 0;		
	}
	
	public void reset_distance() {
		this.pastX=-1;
		this.pastY=-1;
		this.distance = 0;
	}
	
	public void increment_distance() {
		
		if (this.pastX==-1 && this.pastY==-1) {
			this.pastX =  robot.getX();
			this.pastY =  robot.getY();
			return ;
		}
		
		double X = robot.getX();
    	double Y = robot.getY();
    	
    	double distanceX = (pastX - X);
    	double distanceY = (pastY - Y);
    	
    	this.distance += Math.sqrt(distanceX*distanceX  + distanceY*distanceY);
    	
    	this.pastX = X;
    	this.pastY = Y;	
	}
	
	public double getTotalDistance() {
		return this.distance;
	}
	
	public void printTotalDistance() {
		System.out.println(this.distance);
	}
	
}
