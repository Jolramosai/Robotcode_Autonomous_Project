


package custom_team;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author joseramos
 */

import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.TeamRobot;
import robocode.Condition;
import robocode.CustomEvent;
import robocode.BattleEndedEvent;
import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RobotDeathEvent;
import robocode.RoundEndedEvent;
import robocode.util.Utils;
import robocode.ScannedRobotEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.*;
import java.util.ArrayList;

import auxiliary.InfoRobot;
import auxiliary.Message;
import auxiliary.MessageType;
import auxiliary.Position;
import auxiliary.RobotType;
import auxiliary.State;
import auxiliary.tasks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;


import java.io.IOException;



import standardOdometer.*;



public class MeleeMk02  extends TeamRobot
{
     
    
    // Constants
    
    // Scanning direction, where the radar turns to the right with positive values, and turns
	// to the left with negative values.
	double scanDir = 1;
    final double FIREPOWER = 5;//  Max. power of shot
	int misses = 0;
	int max_misses = 7;//  Max. number of shots missed
	State state; // Data containing the robot state, includes a variety of important information
	long lastDirectionShift; // Last time when the robot shifted its direction
	int direction = 1; // Current direction, where 1 means ahead (forward) and -1 means back
        String traked = null;
        double enemyBearing;
        double enemyDistance;
        double gunTurnAmt;
        
        /**
	 * Run method
         * Main method that is called by the game when the robot engage in the next round of a battle.
	 */
        @Override
	public void run() 
        {
		// Inicialization process
                setBodyColor(new Color(150, 75, 0)); // color marron
		setGunColor(new Color(199, 234, 70)); // colour lime
		setRadarColor(new Color(200, 200, 70));
		setScanColor(Color.white);
		setBulletColor(Color.blue);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true); 
                int look_arround_turns = 20;   
		state = new State();
               int  gunTurnAmt;
		

		// Life Cycle
		while (true) 
                {
			// Handle the radar that scans enemy robots
			handleRadar();
			// Handle the gun by turning it and fire at our target
			handleGun(state);
			// Move the robot around on the battlefield
			moveRobot(state);
			// Scan for other robots.
			scan();
		  }

        }
	
        
	
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////      Movement Stuff              ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  /////////////// 
        void moveRobot(State state) // moves huging the wall until atacking 
        {	
// The  strategy is to get as close to our target robot as possible hugging the walls and evading  the death zone
           
         int newDirection = direction;
         InfoRobot infoEnemy;
         infoEnemy = state.enemyInfo();
         // Get closer to our target f we have a target robot
         if (state.enemy_being_attacked != null) 
                {
         
                        
			// Calculate the range from the walls/borders, our robot should keep within.
			int borderRange = 40;

			// The horizontal and vertical flags are for determine bot move.
			boolean horizontal = false;
			boolean vertical = false;

			// Initialize the new heading of the robot to the current heading of the robot.
			double newHeading = getHeadingRadians();

			// Check if bot is at the up or down border. If so it should move horizontally
                        
			if (getY() < borderRange || getY() > getBattleFieldHeight() - borderRange) 
                        {
				horizontal = true;
			}
                        
			// Check if our robot is at the left or right border. If so should move vertically
			if (getX() < borderRange || getX() > getBattleFieldWidth() - borderRange) 
                        {
				vertical = true;
			}
                        
			// If we are in one of the corners  we could move both horizontally or vertically.
			//We need to choose.
	
			// Adjust the heading of our robot with 90 degrees, if it must move horizontally. Otherwise the calculated heading is towards moving vertically.
			if (horizontal) 
                        {
				newHeading -= Math.PI / 2;
			}
			// Set the robot to turn left the amount of radians we have just calculated
			setTurnLeftRadians(Utils.normalRelativeAngle(newHeading));

			// Check if our robot has finished turning, i.e. has less than 1 degrees left to turn
                        if (enemyDistance > 150) {
			gunTurnAmt = normalRelativeAngleDegrees(enemyBearing + (getHeading() - getRadarHeading()));

			turnGunRight(gunTurnAmt); // Try changing these to setTurnGunRight,
			turnRight(enemyBearing); // and see how much Tracker improves...
			// (you'll have to make Tracker an AdvancedRobot)
			ahead(enemyDistance - 140);
			return;
                }
         // Set ahead 100 units forward or backward depending on the direction
		
                
         

	}}
	
       

	///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////       OnScanned Robot Stuff      ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  /////////////// 

	@Override
	public void onScannedRobot(ScannedRobotEvent e) 
        {System.out.println("scan");
		Position scanned = getScannedBotInfo(e);	
                //se acabamos de scanear o robo e ja estamos perto dele
                
                 Message message = new Message(MessageType.Inform,scanned.getX(),scanned.getY(),
					e.getEnergy(),e.getName());
           if(e.getName() == state.enemy_being_attacked){
                enemyBearing= e.getBearing();
                enemyDistance=e.getDistance();}
            try 
            {
             broadcastMessage(message);
            } 
            catch (IOException e1) 
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
			
        }
           
        public Position getScannedBotInfo(ScannedRobotEvent e) 
        {
		System.out.println("robot scanned");
        	double angleToEnemy = e.getBearing();
        	double angle = Math.toRadians((getHeading() + angleToEnemy % 360));
        	
		double scannedX = (int)(getX() + Math.sin(angle) * e.getDistance());
		double scannedY = (int)(getY() + Math.cos(angle) * e.getDistance());
            
		InfoRobot info = new InfoRobot(isTeammate(e.getName()) ?  RobotType.teammate:RobotType.enemy ,
					e.getName(),new Position(scannedX,scannedY),e.getEnergy());
			
		state.updateInfoRobot(info);
                
                
			
		return info.position;	
			
        }
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////       Radar Stuff                ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  /////////////// 
        
	private void handleRadar() {
		// Set the radar to turn infinitely to the right if the scan direction is positive;
		// otherwise the radar is moved to the left, if the scan direction is negative.
		// Notice that onScannedRobot(ScannedRobotEvent) is responsible for determining the scan
		// direction.
		setTurnRadarRightRadians(scanDir * Double.POSITIVE_INFINITY);
	}
        
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////       Gun Stuff                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  /////////////// 
           private void handleGun(State state) 
           {
		// Update our target robot to fire at
		check_for_weak_enemies( state);
		// Update the gun direction
		updateGunDirection(state);
		// Fires the gun, when it is ready
		fireGunWhenReady(state);
            }
           
           private void updateGunDirection(State state) 
           {
		// Only update the gun direction, if we have a current target
		if (state.enemy_being_attacked != null) {
			// Calculate the bearing between the gun and the target, which can be positive or
			// negative
                       InfoRobot enemy = state.enemyInfo();
			double targetBearing = bearingTo(getGunHeadingRadians(), enemy.position.getX(), enemy.position.getY());
			// Set the gun to turn right the amount of radians defined by the bearing to the target
			setTurnGunRightRadians(targetBearing); // positive => turn right, negative => turn left
		}
	}

           private void fireGunWhenReady(State state)
           {
		// We only fire the fun, when we have a target robot
		if (state.enemy_being_attacked != null) {
			// Only fire when the angle of the gun is pointing at our (virtual) target robot
			 InfoRobot enemy = state.enemyInfo();
			
			
			// Calculate the distance between between our robot and the target robot
			double x = enemy.position.getX();
			double y =  enemy.position.getY();
			double dist = distanceTo(x, y);
			// Angle that "covers" the the target robot from its center to its edge
			double angle = Math.atan(18 / dist);

			// Check if the remaining angle (turn) to move the gun is less than our calculated cover
			// angle
			if (Math.abs(getGunTurnRemaining()) < angle) {
				// If so, our gun should be pointing at our target so we can hit it => fire!!
				if((distanceTo(x, y) <= 200) && 
					(getEnergy()/2 > FIREPOWER)) {
						System.out.println("Close fire.");
						setFire(FIREPOWER);
					}
				else
				if ((distanceTo(x, y) <= 400) && 
					(getEnergy()/2 > FIREPOWER/2)) {
						System.out.println("Medium fire.");
						setFire(FIREPOWER/2);
					}
				else 
				if (getEnergy()/2 > FIREPOWER/3) {
					System.out.println("Long fire.");
					setFire(FIREPOWER/3);
				}
			}
		}
	}
           void check_for_weak_enemies(State state) 
            {
		
		String weakest_enemy = find_weakest_enemy(state);
		
		
		if(weakest_enemy == null) {return;}
		
       
			
		state.enemy_being_attacked = weakest_enemy;
                
		

		
	}
           String find_weakest_enemy(State state) 
           {   
		
		String weakest_enemy = null;
		Double weakest_energy = 10000.0;
		
		for(String s: state.InfoRobots.keySet()) {
			InfoRobot infoRobot = state.InfoRobots.get(s);
			
			if(infoRobot.type == RobotType.enemy) {
			System.out.println("enemy");
			if(infoRobot.energy < weakest_energy) {
				weakest_enemy = s;
				weakest_energy = state.InfoRobots.get(s).energy;
			}
			
			}
			
		}
		
		return weakest_enemy;
		

            }
           public void adjust_gun(double x,double y) 
            {
		
		double abs_angle = absoluteBearing(getX(),getY(),x,y);
		double new_bearing = 2;

		while(new_bearing > 1) {
			new_bearing = normalizeBearing(abs_angle - getGunHeading());
			setTurnGunRight(new_bearing);
			execute();
		}
		
		
	}
      
      
      
      
      
      
      
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////       MIscelaneous events        ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  /////////////// 
        public void onHitRobot(HitRobotEvent e)
        {
		if (e.getBearing() > -10 && e.getBearing() < 10) {
			fire(3);
		}
		if (e.isMyFault()) {
			turnRight(10);
		}
        }
        public void onBulletHitEvent(BulletHitEvent Bullet) 
        {		
            misses = 0;
        }
        @Override
        public void onBulletMissed(BulletMissedEvent bullet) 
        {
		misses++;
		System.out.println("Bullet missed");
	
		if(misses > max_misses) 
                {
			
			state.task = tasks.searchingEnemy;
			/*Position attacking_position = state.InfoRobots.get(state.enemy_being_attacked).position;
			double old_x = attacking_position.getX();
			double old_y = attacking_position.getY();
			
			System.out.println("searching");
			for(int i = 0; i < 360; i++) {
				turnGunRight(gunTurnAmt);
				scan();
				attacking_position = state.InfoRobots.get(state.enemy_being_attacked).position;
				if(attacking_position.getX() != old_x || attacking_position.getY() != old_y)
					break;
			}
			*/
			
			
			misses = 0;
                        return;

		}
		
		if(state.task == tasks.attack) {
			prepare_attack(state.enemy_being_attacked);	
		}
		
		
		
		
	}
        
      //saves the info optained from teammates
        @Override
        public void onMessageReceived(MessageEvent event) {
        	Message message = (Message)event.getMessage();
        		     		
        		
        	InfoRobot info  = new InfoRobot(isTeammate(message.robotName) ?  RobotType.teammate:RobotType.enemy ,
   						message.robotName,new Position(message.positionX,message.positionY),message.energy); 
        		
   		state.updateInfoRobot(info);

        		
        }
    
         @Override
	public void onRobotDeath(RobotDeathEvent event) 
        {
		
       state.InfoRobots.remove(event.getName());
       
       if(state.task == tasks.attack || state.task == tasks.swarm 
				|| state.task == tasks.searchingEnemy) {
			
			if(!event.getName()
				.equals(state.enemy_being_attacked))
				return ;
			
				
				state.task = tasks.free;
				state.enemy_being_attacked = null;
				
			
		}
		
		System.out.println("ROBOT DEAD: " +  event.getName());
		
	}
	//currently finds the weakest enemy
	
	
      
      ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////       Tracek Stuff             ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  /////////////// 
      
      
        private double angleTo(double x, double y) 
        {
		return Math.atan2(x - getX(), y - getY());
	}

        private double distanceTo(double x, double y) 
        {
		return Math.hypot(x - getX(), y - getY());
	}

        public void goTo (double x, double y)  //vai calcular e direcionar o tanque reorrendo a fun�oes trigonometricas
        {
    	double a;
    	setTurnRightRadians(Math.tan( a = Math.atan2(x -= (double) getX(), y -= (double) getY()) - getHeadingRadians()));
    	setAhead(Math.hypot(x, y) * Math.cos(a));
        }
        
        private double bearingTo(double heading, double x, double y) 
        {
		return Utils.normalRelativeAngle(angleTo(x, y) - heading);
	}


        public double calcDist (double xi, double yi, double xf, double yf) 
        { //calcular a distancia 
            return Math.sqrt(Math.pow((xf-xi), 2) + Math.pow((yf-yi), 2));
        }
        double absoluteBearing(double x1, double y1, double x2, double y2) 
        {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;

		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}

		return bearing;
	}
	
	//normalizes the bearing value
	
	public double normalizeBearing(double angle) 
        {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}









        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////        Not Nedded RN             ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  ///////////////         
        ///////////////                                  /////////////// 




        void prepare_swarm(State state) 
        {
		String weak_enemy = find_weakest_enemy(state);
		
		if(weak_enemy == null)
			return ;
		
		InfoRobot info_weak_enemy = state.InfoRobots.get(weak_enemy);
		
		double x = info_weak_enemy.position.getX();
		double y = info_weak_enemy.position.getY();
		
		double energy = info_weak_enemy.energy;
		
		Message message = new Message(MessageType.Swarm,x,y,energy,weak_enemy);
		
		state.enemy_being_attacked = weak_enemy;
		state.task =  tasks.swarm;
		
		try 
                {
			broadcastMessage(message);
		} 
                catch (IOException e) 
                {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
        public void prepare_attack(String robot) 
        {
		
		InfoRobot enemyInfo = state.InfoRobots.get(robot);
		
		Double x = enemyInfo.position.getX();
		Double y = enemyInfo.position.getY();
		
		System.out.println("Enemy being attacked position: " +  x + "," + y);
                // aqui falta dar broadcast para o swarm &&&&&&&
		adjust_gun(x,y);
		
	}
        
     
}



