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
import auxiliary.tasks;
import robocode.Condition;
import robocode.RoundEndedEvent;
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
public class RobotLeaderCopy extends TeamRobot
{

    
    	
	double gunTurnAmt; // Amplitude de procura do scan
	int misses = 0;
	int max_misses = 3;
	State state;
   
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
                
        int look_arround_turns = 20;   
                
		state = new State();
		
		share_position(getX(),getY(),getEnergy(),getName());
		// Life Cycle
		while (true) 
                {
					switch(state.task) {
						
						case free:
							turnGunRight(gunTurnAmt);
							
							if(getTime() > look_arround_turns && getTeammates() != null) {
								prepare_swarm(state);
							}
							
							
							
						break;
						
						case searchingEnemy:
							turnGunRight(gunTurnAmt);
						break;
						
						case attack:
							prepare_attack(state.enemy_being_attacked);
							fire(3);
							
						break;
						
						case swarm:
							System.out.println("attacking" + state.enemy_being_attacked);
							prepare_attack(state.enemy_being_attacked);
							fire(3);
							
						break;
							
					
						default:
						
					
					}
			
                }
        }

	
	/*
	Position find_safe_position(int tries,double myX,double myY) {
		
		double x=myX,y=myY;
		double min_distance;
		
		for(int i = 0; i < tries;i++) {
			
		}
		 
		return new Position(x,y);
	}
	*/
	
	/**
	HashMap<String,Position> 
	**/
	
	Position find_non_hitting_position(int repetitions,State state) {
		
		double Height = getBattleFieldHeight();
		double Width  = getBattleFieldWidth();
		
		double new_x=0,current_x = getX();
		double new_y=0,current_y = getY();
		
		double distance,lowest_distance = Double.POSITIVE_INFINITY;
		
		for(int i = 0;i < repetitions;i++) {
			double x = Math.random()*Width;
			double y = Math.random()*Height;
			
			if(!hits_teammates(state,x,y)) {
				distance = calcDist(current_x,current_y,x,y);
				if(distance < lowest_distance) {
					lowest_distance = distance;
					new_x = x;
					new_y = y;
				}
			}
			
		}
		
		if(lowest_distance < Double.POSITIVE_INFINITY)
			return new Position(new_x,new_y);
		
		return null;
	}
	
	/*Boolean hits_robot(double myX,double myY,double robotX,
			double robotY,double gun_angle) {
		
		double estimated_radius = 25;
		double c = myY - gun_angle*myX;
		
		double distance = (gun_angle*robotX - robotY + c)/
				Math.sqrt(Math.pow(gun_angle,2) + 1);
		
		if(distance <= estimated_radius)
			return true;
		
		return false;
	}*/
	
	Boolean hits_robot(double myX,double myY,double robotX,
			double robotY,double gun_angle) {
		
		double abs_angle = absoluteBearing(myX,myY,robotX,robotY);
		double distance = calcDist(myX,myY,robotX,robotY);
		double estimated_size = 25;
		
		double non_hitting_angle = Math.toDegrees(Math.atan(estimated_size/distance));
		
		System.out.println("normalize bearing + " + normalizeBearing(abs_angle + non_hitting_angle));
		System.out.println("normalize bearing - " + normalizeBearing(abs_angle - non_hitting_angle));
		System.out.println("gun heading " + normalizeBearing(getGunHeading()));
		
		if(normalizeBearing(getGunHeading()) <= normalizeBearing(abs_angle + non_hitting_angle)
				&& normalizeBearing(getGunHeading()) >= normalizeBearing(abs_angle - non_hitting_angle))
			return true;
		
		return false;
		
	}
	
	
	Boolean hits_teammates(State state,double myX,double myY) {
	
		for(String s:getTeammates()) {
			InfoRobot info = state.InfoRobots.get(s);
			if(info != null && hits_robot(myX,myY,info.position.getX(),
					info.position.getY(),getGunHeading())) 
				
				return true;
					
		}
		
		return false;
	}
	
	
	ArrayList<Position>  getEnemyPositions(State state) {
		ArrayList<Position> enemy_positions = new ArrayList<Position>();
		
		for(String robot: state.InfoRobots.keySet())
			enemy_positions.add(state.InfoRobots.get(robot).position);
		
		return enemy_positions;
	
	}
	
	Position[] generateRandomPositions(int number_positions,int repetitions,double enemyX,double enemyY,State state) {
		
		Position[] positions = new Position[number_positions];
		
		System.out.println(number_positions);
		
		ArrayList<Position> occupied_positions = getEnemyPositions(state);
		
		double Height = getBattleFieldHeight();
		double Width  = getBattleFieldWidth();
	
	
		for(int i = 0; i < number_positions;i++) {
	
			
			Position position_i = new Position(Math.random()*Height,Math.random() * Width);
			double lowest_distance = calcDist(enemyX,enemyY,position_i.getX(),position_i.getY()) ;

			for(int rep = 0; rep < repetitions; rep++) {
				Position position = new Position(Math.random()*Height,Math.random() * Width);
				
				if(far_enough_from_others(position.getX(),position.getY(),occupied_positions)) {
					double distance = calcDist(enemyX,enemyY,position.getX(),position.getY());
					if(distance < lowest_distance) {
						lowest_distance = distance;
						position_i = position;
					}
				}
			}
				positions[i] = position_i;
		}	
		
			return positions;
	}
	
	
	String[] allocate_positions(Position[] positions,int repetitions,State state,String[] teammates) {
		
		String[] allocations = teammates.clone();
		
		ArrayList<String> current_allocation = new ArrayList<String>();
		Collections.addAll(current_allocation, teammates);
		
		double shortest_distance = Double.POSITIVE_INFINITY;
		
		/*
		for(String s:current_allocation)
			System.out.println(s);
			
		for(Position p:positions)
			System.out.println(p);
		*/
		
		for(int i = 0; i < repetitions; i++) {
			Collections.shuffle(current_allocation);
			double distance = 0;
			int position_nr = 0;
			
			for(String s:current_allocation) {
				Position position = state.InfoRobots.get(s).position;
	
								
				distance += calcDist(position.getX(),position.getY(),
						positions[position_nr].getX(),positions[position_nr].getY());
				position_nr++;
			}
			
			if(distance < shortest_distance) {
				//System.out.println(shortest_distance);
				allocations = current_allocation.toArray(allocations);
				shortest_distance = distance;
			}
			
		}
		
		
		return allocations;
	}
	
	double get_cumulative_distance(double pointX,double pointY,Position[] positions) {
		
		double distance = 0;
		
		for(Position p:positions) {
			distance += calcDist(pointX,pointY,p.getX(),p.getY()); 
		}
		return distance;
	}
	
	
	Boolean far_enough_from_others(double pointX,double pointY,ArrayList<Position> occupied_positions) {
		
		double estimated_robot_radius = 100;
		
		for(Position position:occupied_positions)
			if(calcDist(pointX,pointY,position.getX(),position.getY()) < estimated_robot_radius)
					return false;
		
		return true;
	}
	
	
	
	String[] getTeam() {
		
		if(getTeammates() == null) {
			String[] team = new String[1];
			team[0] = getName();
			return team;
		}
		
		else {
			String[] teammates = getTeammates();
			String[] team = new String[getTeammates().length + 1];
			for(int i = 0; i < teammates.length;i++)
				team[i] = teammates[i];
			team[teammates.length] = getName();
			return team;
		}
			
	}
	
	void prepare_swarm(State state) {
		System.out.println("swarm");
		String weak_enemy = find_weakest_enemy(state);
		
		if(weak_enemy == null)
			return ;
		
		System.out.println("enemy exists");
		
		InfoRobot info_weak_enemy = state.InfoRobots.get(weak_enemy);
		
		double x = info_weak_enemy.position.getX();
		double y = info_weak_enemy.position.getY();
		
		double energy = info_weak_enemy.energy;
		
		Message message;
		
		state.enemy_being_attacked = weak_enemy;
		state.task =  tasks.swarm;
		
		InfoRobot myInfo = new InfoRobot(RobotType.teammate,getName(),new Position(getX(),getY()),getEnergy());
		
		state.updateInfoRobot(myInfo);
		
		String[] team = getTeam();
		
		Position[] random_positions = generateRandomPositions(getTeam().length,100,x,y,state);
		String[] allocations = allocate_positions(random_positions,100,state,getTeam());
		
		
		for(int i = 0; i < allocations.length;i++ ) {
			message = new Message(MessageType.Move,random_positions[i].getX(),random_positions[i].getY());
			System.out.println("allocation " + allocations[i]);
			System.out.println(random_positions[i].getX() + " : " + random_positions[i].getY());
			try {
				sendMessage(allocations[i],message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		message = new Message(MessageType.Swarm,x,y,energy,weak_enemy);
		
		try {
			broadcastMessage(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
	
	String find_weakest_enemy(State state) {
		
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
	
	
	public void onRobotDeath(RobotDeathEvent event) {
		
		if(state.task == tasks.attack || state.task == tasks.swarm 
				|| state.task == tasks.searchingEnemy) {
			
			if(!event.getName()
				.equals(state.enemy_being_attacked))
				return ;
			
				
				state.task = tasks.free;
				state.enemy_being_attacked = null;
				
			
		}
		
		System.out.println("ROBOT DEAD: " +  event.getName());
		state.InfoRobots.remove(event.getName());
		
	}
	//currently finds the weakest enemy
	
	void check_for_weak_enemies(State state) {
		
		String weakest_enemy = find_weakest_enemy(state);
			
		if(weakest_enemy == null) return;
		
		state.task = tasks.attack;
			
		state.enemy_being_attacked = weakest_enemy;
		
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
		
		System.out.println("message received");
		
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
		
		Double x = enemyInfo.position.getX();
		Double y = enemyInfo.position.getY();
		
		System.out.println("Enemy being attacked position: " +  x + "," + y);
		adjust_gun(x,y);
		
	}
	
	/***
	when the robot misses a shot it recalibrates the gun whith the enemy coordinates present in 
	his state after missing more than 3 times the robots scans the area to find the enemy
	***/
	
	public void onBulletMissed(BulletMissedEvent bullet) {
		
		System.out.println("Bullet missed");
		
		if(misses > max_misses) {
			
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

		}
		
		if(state.task == tasks.attack) {
			prepare_attack(state.enemy_being_attacked);	
		}
		
		misses++;
		
		
	}
	
	
	public void onBulletHit(BulletHitEvent Bullet) {
		
		misses = 0;
	}
	/**
	 * onScannedRobot:  O que acontece quando apanhamos um robo no scan
	 */
	public void onScannedRobot(ScannedRobotEvent e) 
        {
			
			System.out.println("robot scanned");
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
	
        
        public void goTo (int x, int y)  //vai calcular e direcionar o tanque reorrendo a funçoes trigonometricas
        {
    	double a;
    	setTurnRightRadians(Math.tan( a = Math.atan2(x -= (int) getX(), y -= (int) getY()) - getHeadingRadians()));
    	setAhead(Math.hypot(x, y) * Math.cos(a));
        }
        
      
              
       
}



