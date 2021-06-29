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
import robocode.CustomEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import standardOdometer.*;





/**
 * robozitoMKcontador - a sample robot.
 * <p>
 * Vai para uma posição inicial definida. E depois contorna tres robos estacionarios no menor percurso possivel
 *
 * @author
 * 
 */
public class RobotLeader extends TeamRobot
{

    
    	
	double gunTurnAmt; // Amplitude de procura do scan
	int misses = 0;
	int max_misses = 1;
	State state;
	Swarm swarm = new Swarm();
	int max_hits = 3;
	int max_bullet_hits = 2;
	int nr_hits = 0;
	Boolean in_danger = false;
	int initial_scan;
    int look_arround_turns = 20;   

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
		// Life Cycle
		while (true) {
			
			
				if(state.hits_wrong_target) {
					
					double Height = getBattleFieldHeight();
					double Width  = getBattleFieldWidth();
				
					stopHittingWrongTarget(state,getX(),getY(),
						Height,Width);
				
					state.hits_wrong_target = false;
				}
			
				if(in_danger){
					
					in_danger = false;
					nr_hits = 0;
					
					Position new_position = find_safe_position(100,state);
					if(new_position != null)
						moveTo(new_position.getX(),new_position.getY());
				}
                
				
					switch(state.task) {
						
						case free:
							turnGunRight(gunTurnAmt);
							
							if(getTime() > look_arround_turns && getTeammates() != null) {
								prepare_swarm(state);
							}
							
							else if(getTeammates() == null) {
								String weakest_enemy = swarm.find_weakest_enemy(state);
								if(weakest_enemy != null) {
									state.task = tasks.attack;
									state.enemy_being_attacked = weakest_enemy;
								}
							}
							
							
						break;
						
						case searchingEnemy:
							turnGunRight(gunTurnAmt);
							if(getTime() - initial_scan >  look_arround_turns ) {
								state.task = tasks.free;
							}
						break;
						
						case attack:
							prepare_attack(state.enemy_being_attacked);
							fire(Rules.MAX_BULLET_POWER);
							
						break;
						
						case swarm:
							prepare_attack(state.enemy_being_attacked);
							fire(Rules.MAX_BULLET_POWER);
							
						break;
							
					
						default:
						
					
					}
			
                }
	}

	
	
	public void stopHittingWrongTarget(State state,double x,double y,
			double Height,double Width){
		
		Position new_position = swarm.find_non_hitting_position(100,state,Height,Width,
				getX(),getY(),getTeammates(),getGunHeading());
		double new_x = new_position.getX();
		double new_y = new_position.getY();
		
		moveTo(new_x,new_y);
		
	}
	
	ArrayList<Position>  getEnemyPositions(State state) {
		ArrayList<Position> enemy_positions = new ArrayList<Position>();
		
		for(String robot: state.InfoRobots.keySet())
			enemy_positions.add(state.InfoRobots.get(robot).position);
		
		return enemy_positions;
	
	}
		
	
	
	double get_cumulative_distance(double pointX,double pointY,ArrayList<Position> positions) {
		
		double distance = 0;
		
		for(Position p:positions) {
			distance += calcDist(pointX,pointY,p.getX(),p.getY()); 
		}
		return distance;
	}
	
	
	
	void prepare_swarm(State state) {
		System.out.println("swarm");
		String weak_enemy = swarm.find_weakest_enemy(state);
		
		if(weak_enemy == null)
			return ;
				
		InfoRobot info_weak_enemy = state.InfoRobots.get(weak_enemy);
		
		double x = info_weak_enemy.position.getX();
		double y = info_weak_enemy.position.getY();
		
		double energy = info_weak_enemy.energy;
		
		Message message;
		
		state.enemy_being_attacked = weak_enemy;
		state.task =  tasks.swarm;
		
		System.out.println("attacking" + state.enemy_being_attacked);

		message = new Message(MessageType.Swarm,x,y,energy,weak_enemy);
		
		try {
			broadcastMessage(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
	
	
	Position find_safe_position(int repetitions,State state) {
		
		double Height = getBattleFieldHeight();
		double Width  = getBattleFieldWidth();
		
		double estimated_size = 100;
		
		Position new_position = null;
		double max_distance = 0;
		
		ArrayList<Position> enemy_positions = new ArrayList<Position>();
		
		for(String s:state.InfoRobots.keySet())
			enemy_positions.add(state.InfoRobots.get(s).position);
		
		for(int i = 0; i < repetitions;i++) {
			
			Position position = new Position(Math.random()*(Width - 2*estimated_size) + estimated_size,
					Math.random()*(Height - 2*estimated_size) + estimated_size);
			
			if(swarm.far_enough_from_others(position.getX(), position.getY(), state)) {
				double distance = get_cumulative_distance(position.getX(),position.getY(),enemy_positions);
				if(distance > max_distance) {
					new_position = position;
					max_distance = distance;
				}
			}
		}
		
		
		return new_position;
	}
	
	
	
	public void onRobotDeath(RobotDeathEvent event) {
		
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
	
	
	public void onMessageReceived(MessageEvent event) {
		Message message = (Message)event.getMessage();
				
		InfoRobot info = new InfoRobot(isTeammate(message.robotName) ?  RobotType.teammate:RobotType.enemy ,
				message.robotName,new Position(message.positionX,message.positionY),message.energy); 
		
		state.updateInfoRobot(info);

		
	}
	//calculate the absolute bearing between two points
	
	double absoluteBearing(double x1, double y1, double x2, double y2) {
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
	
	public double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	
	//adjusts_gun to point to a specific position
	
	public void adjust_gun(double x,double y) {
		
		double abs_angle = absoluteBearing(getX(),getY(),x,y);
		double new_bearing = 2;

		while(new_bearing > 1) {
			new_bearing = normalizeBearing(abs_angle - getGunHeading());
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
	
	
	/***
	when the robot misses a shot it recalibrates the gun whith the enemy coordinates present in 
	his state after missing more than 3 times the robots scans the area to find the enemy
	***/
	
	public void onBulletMissed(BulletMissedEvent bullet) {
		
		
		if(misses > max_misses) {
			
			state.task = tasks.searchingEnemy;
			state.InfoRobots.remove(state.enemy_being_attacked);
			misses = 0;
			

		}
		
		if(state.task == tasks.attack) {
			prepare_attack(state.enemy_being_attacked);	
		}
		
		misses++;
		
		
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
			
			
			
			share_position(scannedX,scannedY,e.getEnergy(),e.getName());
			
        }
           
	
		public void onHitByBullet(HitByBulletEvent event) {
			random_move();
			nr_hits++;
			if(nr_hits > max_bullet_hits)
				in_danger = true;
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
	
		}
	/**
	 * onHitRobot:  If it's our fault, we'll stop turning and moving,
	 * so we need to turn again to keep spinning.  NORMAL;
	 */
		public void onHitRobot(HitRobotEvent e)
        {
		if (e.getBearing() > -10 && e.getBearing() < 10) {
			fire(3);
		}
		if (e.isMyFault()) {
			turnRight(10);
		}
        }
        
        public double calcDist (double xi, double yi, double xf, double yf) 
        { //calcular a distancia 
            return Math.sqrt(Math.pow((xf-xi), 2) + Math.pow((yf-yi), 2));
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
    	
    	
    	if(Math.abs(ahead) < 1)
    		return false;
    	
    	return true;
        
        }
        
      
              
       
}



