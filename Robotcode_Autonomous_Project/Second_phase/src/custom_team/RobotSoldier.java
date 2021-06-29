/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author joseramos
 */
package custom_team;
import robocode.CustomEvent;
import robocode.Droid;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.control.BattlefieldSpecification;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import auxiliary.InfoRobot;
import auxiliary.Message;
import auxiliary.MessageType;
import auxiliary.Position;
import auxiliary.RobotType;
import auxiliary.State;
import auxiliary.Swarm;
import auxiliary.tasks;
import robocode.Condition;
import robocode.RoundEndedEvent;
import robocode.Rules;
import robocode.BattleEndedEvent;
import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import robocode.RobocodeFileOutputStream;
import robocode.RobotDeathEvent;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

import standardOdometer.*;





/**
 * robozitoMKcontador - a sample robot.
 * <p>
 * Vai para uma posição inicial definida. E depois contorna tres robos estacionarios no menor percurso possivel
 *
 * @author
 * 
 */
public class RobotSoldier extends TeamRobot implements Droid
{

    
    	
	double gunTurnAmt; // Amplitude de procura do scan
	int misses = 0;
	int max_misses = 3;
	State state;
    public Boolean stopRandomMove = false;
    public Swarm swarm = new Swarm();
	/**
	 * Run method
         * Primeiro vai para a posição 18
	 */
	public void run() 
        {
		// Set colors
        setBodyColor(new Color(150, 75, 0)); // marron
		setGunColor(new Color(199, 234, 70)); // lime
		setRadarColor(new Color(200, 200, 70));
		setScanColor(Color.white);
		setBulletColor(Color.blue);
                
		setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
		gunTurnAmt = 90; // Initialize gunTurn to 10
               
                
                
		state = new State();
		
		share_position(getX(),getY(),getEnergy(),getName());

		//state.task = tasks.attack;

		
		if(state.hits_wrong_target) {
			
			double Height = getBattleFieldHeight();
			double Width  = getBattleFieldWidth();
			
			stopHittingWrongTarget(state,getX(),getY(),
					Height,Width);
			
			state.hits_wrong_target = false;
		}
		
		// Life Cycle
		while (true) 
                {
					switch(state.task) {
						
						case free:
							
							swarm.check_for_weak_enemies(state,getX(),getY());
							random_move();
						break;
						
						case protect:
							
						break;
						
						case searchingEnemy:
							turnGunRight(gunTurnAmt);
						break;
						
						case attack:
							//prepare_attack("custom_team.RobotLeader*");
							prepare_attack(state.enemy_being_attacked);
							fire(Rules.MAX_BULLET_POWER);
						break;
						
						case swarm:
							
							if(state.first_swarm_move) {
								System.out.println("attacking" + state.enemy_being_attacked);
								System.out.println("moving");
								Position EnemyPosition = state.InfoRobots.get(state.enemy_being_attacked).position;
								
								Position new_position = find_nearby_position(100,getBattleFieldHeight(),
									getBattleFieldWidth(),EnemyPosition.getX(),EnemyPosition.getY(),state);
							
								if(new_position != null) {
									System.out.println("MOVE TO " + new_position.getX() + " " + new_position.getY() );
									moveTo(new_position.getX(),new_position.getY());
								}
								state.first_swarm_move = false;
							}
							
							prepare_attack(state.enemy_being_attacked);

							fire(Rules.MAX_BULLET_POWER);
						default:
						
					
					}
			
                }
        }

	
	
	void random_move() {
		double moving_distance = 80;
		double nextX = getX() + Math.random()*moving_distance - moving_distance/2; 
		double nextY = getY() + Math.random()*moving_distance - moving_distance/2;
		
		double Height = getBattleFieldHeight();
		double Width  = getBattleFieldWidth();
		
		if(nextX > Width)
			nextX = Width;
		
		if(nextY > Height)
			nextY = Height;
			

		int max_itters = 20;
		int it = 0;
		
		while(!swarm.far_enough_from_others(nextX,nextY,state) && it < max_itters) {
			nextX = getX() + Math.random()*moving_distance - moving_distance/2; 
			nextY = getY() + Math.random()*moving_distance - moving_distance/2;
			if(nextX > Width)
				nextX = Width;
			
			if(nextY > Height)
				nextY = Height;
				

		}
		
		
		
		state.hitWall = false;
		stopRandomMove = false;
		
		goTo(nextX,nextY);
		execute();
		
		
		share_position(getX(),getY(),getEnergy(),getName());

	}

	public void onRobotDeath(RobotDeathEvent event) {
		
		state.InfoRobots.remove(event.getName());

		if(state.task == tasks.attack || state.task == tasks.swarm) {
			
			if(!event.getName()
				.equals(state.enemy_being_attacked))
				return ;
			
			if(state.robot_being_protected != null) 
				state.task = tasks.protect;
			
			else 
				state.task = tasks.free;
			
		}
		
		if(state.task == tasks.protect)
			if(event.getName().equals(state.robot_being_protected))
				state.task = tasks.free;
		
		System.out.println("ROBOT DEAD: " +  event.getName());
		
	}
	
	
	public void onHitWall(HitWallEvent even) {
		state.hitWall = true;
	}
	
	
	//currently finds the weakest enemy
	
	Position find_nearby_position(int repetitions,double Height,
			double Width,double enemyX,double enemyY,State state) {

		double estimated_size = 100;
		
		Position new_position = null;
		double lower_distance = Double.POSITIVE_INFINITY;
		
		for(int i = 0;  i < repetitions; i++) {
			
			
			
			Position position = new Position(Math.random()*(Width - 2*estimated_size) + estimated_size
					,Math.random() * (Height - 2*estimated_size) + estimated_size);
			
			
			
			
			if(swarm.far_enough_from_others(position.getX(),position.getY(),state) && 
					!swarm.hits_teammates(state,getX(),getY(),getTeammates(),getGunHeading())){
				
				double distance = swarm.calcDist(position.getX(),position.getY(),enemyX,enemyY);
				
				if(distance < lower_distance) {
					lower_distance = distance;
					new_position = position;
				}
			}
		}
		
		
		return new_position;
	}
	
	
	
	
	//adjusts_gun to point to a specific position
	
	
	public void adjust_gun(double x,double y) {
		
		double abs_angle = swarm.absoluteBearing(getX(),getY(),x,y);
		double new_bearing = 2;

		while(new_bearing > 1) {
			new_bearing = swarm.normalizeBearing(abs_angle - getGunHeading());
			setTurnGunRight(new_bearing);
			execute();
		}
		
	}
	
	//prepares gun to attack robot
	
	public void prepare_attack(String robot) {
		
		InfoRobot enemyInfo = state.InfoRobots.get(robot);
		
		if(enemyInfo == null)
			return ;
		
		
		
		Double x = enemyInfo.position.getX();
		Double y = enemyInfo.position.getY();
		
		
		if(swarm.hits_teammates(state,getX(),getY(),getTeammates(),getGunHeading())){
			
			double Height = getBattleFieldHeight();
			double Width  = getBattleFieldWidth();
			
			Position next_position = swarm.find_non_hitting_position(100,state,Height,Width,
					getX(),getY(),getTeammates(),getGunHeading());
			
			if(next_position != null) {
				moveTo(next_position.getX(),next_position.getY());
			}
		}
				
		adjust_gun(x,y);
		
	}
	
	
	public void onMessageReceived(MessageEvent event) {
		Message message = (Message)event.getMessage();		
		
		InfoRobot info;
		
		switch(message.type) {
			case Protect:
				info = new InfoRobot(isTeammate(message.robotName) ?  RobotType.teammate:RobotType.enemy ,
						message.robotName,new Position(message.positionX,message.positionY),message.energy); 
				goTo(message.positionX,message.positionY);
				state.robot_being_protected = message.robotName;
				state.task = tasks.protect;
				state.updateInfoRobot(info);

			break;
			
			case Swarm:
				info = new InfoRobot(isTeammate(message.robotName) ?  RobotType.teammate:RobotType.enemy ,
						message.robotName,new Position(message.positionX,message.positionY),message.energy); 
				state.enemy_being_attacked = message.robotName;
				prepare_attack(state.enemy_being_attacked);				
				state.task = tasks.swarm;
				state.updateInfoRobot(info);
				
				
				state.first_swarm_move = true;
				

			break;
			
			case Inform:
				info = new InfoRobot(isTeammate(message.robotName) ?  RobotType.teammate:RobotType.enemy ,
						message.robotName,new Position(message.positionX,message.positionY),message.energy); 
				state.updateInfoRobot(info);
			break;
			

			
			default:
				
		}
	}
	
	/***
	when the robot misses a shot it recalibrates the gun whith the enemy coordinates present in 
	his state after missing more than 3 times the robots scans the area to find the enemy
	***/
	
	public void onBulletMissed(BulletMissedEvent bullet) {
		
		
		if(state.task == tasks.attack) {
			prepare_attack(state.enemy_being_attacked);	
		}
		
		misses++;
		
		
	}
	
	public void stopHittingWrongTarget(State state,double x,double y,
			double Height,double Width){
		
		Position new_position = swarm.find_non_hitting_position(100,state,Height,Width,
				getX(),getY(),getTeammates(),getGunHeading());
		double new_x = new_position.getX();
		double new_y = new_position.getY();
		
		moveTo(new_x,new_y);
		
	}
	
	public void onBulletHit(BulletHitEvent Bullet) {
		

		
		if(isTeammate(Bullet.getName()) && 
				swarm.hits_teammates(state,getX(),getY(),getTeammates(),getGunHeading())){
			
			state.hits_wrong_target = true;
			
		}
		
		
		if(Bullet.getName().equals(state.enemy_being_attacked))
			misses = 0;
		
		
	}
	
	/**
	 * onScannedRobot:  O que acontece quando apanhamos um robo no scan
	 */
	public void onScannedRobot(ScannedRobotEvent e) 
        {
			
        	double angleToEnemy = e.getBearing();

        	double angle = Math.toRadians((getHeading() + angleToEnemy % 360));
        	
			double scannedX = (int)(getX() + Math.sin(angle) * e.getDistance());
			double scannedY = (int)(getY() + Math.cos(angle) * e.getDistance());
            
			InfoRobot info = new InfoRobot(isTeammate(e.getName()) ?  RobotType.teammate:RobotType.enemy ,
					e.getName(),new Position(scannedX,scannedY),e.getEnergy());
			
			state.updateInfoRobot(info);
			
			if(e.getName().equals(state.enemy_being_attacked))
				state.task = tasks.attack;
			
        }
           


		void share_position(double x,double y,double energy,String name) {
		
		Message message = new Message(MessageType.Inform,x,y,
				energy,name);
		
		try {
			broadcastMessage(message);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		}
	/**
	 * onHitRobot:  If it's our fault, we'll stop turning and moving,
	 * so we need to turn again to keep spinning.  NORMAL;
	 */
		public void onHitRobot(HitRobotEvent e)
        {
			state.hitsRobot++;
			random_move();
        }
        
       
		public void onHitByBullet(HitByBulletEvent event) {
			random_move();
		}
        
		 void moveTo(double x,double y) {
	        	state.hitsRobot = 0;
	        	state.hitWall  = false;
	        	int max_hits = 5;
	        	
	        	while(goTo(x,y) && state.hitsRobot < max_hits && !state.hitWall) {
	        		double old_X = getX();
	        		double old_Y = getY();
	        		
	        		execute();
	        		
	        		if(Math.abs(old_X - getX()) < 1 && Math.abs(old_Y - getY()) < 1)
	        			break;
	        		
	        		share_position(getX(),getY(),getEnergy(),getName());
	        	}
	        	
	        	
	        }
        
        public Boolean goTo (double x, double y)  //vai calcular e direcionar o tanque reorrendo a funçoes trigonometricas
        {
    	double a;
    	setTurnRightRadians(Math.tan( a = Math.atan2(x -=  getX(), y -=  getY()) - getHeadingRadians()));
    	double ahead = Math.hypot(x, y) * Math.cos(a);
    	setAhead(ahead);
    	
    	System.out.println(ahead);
    	
    	if(Math.abs(ahead) < 1)
    		return false;
    	
    	return true;
        
        }
        
      
              
       
}



