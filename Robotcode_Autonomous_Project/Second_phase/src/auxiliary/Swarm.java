package auxiliary;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Swarm {
	
	
	
	public String find_weakest_enemy(State state,double posX,double posY) {
			
			double distance_weight = 0.3;
			double energy_weight   = 0.7;
			
			
			
			String weakest_enemy = null;
			Double weakest_energy = Double.POSITIVE_INFINITY;
			
			for(String s: state.InfoRobots.keySet()) {
				InfoRobot infoRobot = state.InfoRobots.get(s);
				
				if(infoRobot.type == RobotType.enemy) {
				System.out.println("enemy");
				
				double energy = distance_weight * calcDist(posX,posY,infoRobot.position.getX(),infoRobot.position.getY()) + 
						energy_weight*infoRobot.energy;
				
				if(energy < weakest_energy) {
					weakest_enemy = s;
					weakest_energy = energy;
				}
				
				}
				
			}
			
			return weakest_enemy;
			
			}
			
			
	
		
	
	
	
	

	public String find_weakest_enemy(State state) {
			
			double distance_weight = 0.3;
			double energy_weight   = 0.7;
			
			ArrayList<Position> allies = new ArrayList<Position>();
			
			for(String s:state.InfoRobots.keySet())
				allies.add(state.InfoRobots.get(s).position);
			
			String weakest_enemy = null;
			Double weakest_energy = Double.POSITIVE_INFINITY;
			
			for(String s: state.InfoRobots.keySet()) {
				InfoRobot infoRobot = state.InfoRobots.get(s);
				
				if(infoRobot.type == RobotType.enemy) {
				System.out.println("enemy");
				
				double energy = distance_weight * get_cumulative_distance(infoRobot.position.getX(),infoRobot.position.getY(),allies) + 
						energy_weight*infoRobot.energy;
				
				if(energy < weakest_energy) {
					weakest_enemy = s;
					weakest_energy = energy;
				}
				
				}
				
			}
			
			
			return weakest_enemy;
			
			}
		
	double get_cumulative_distance(double pointX,double pointY,ArrayList<Position> positions) {
		
		double distance = 0;
		
		for(Position p:positions) {
			distance += calcDist(pointX,pointY,p.getX(),p.getY()); 
		}
		return distance;
	}
	
	//currently finds the weakest enemy
	
		public void check_for_weak_enemies(State state) {
				
			String weakest_enemy = find_weakest_enemy(state);
					
			if(weakest_enemy == null) return;
				
			state.task = tasks.attack;
					
			state.enemy_being_attacked = weakest_enemy;
				
		}
		
	//currently finds the weakest enemy
	
	public void check_for_weak_enemies(State state,double posX,double posY) {
			
		String weakest_enemy = find_weakest_enemy(state,posX,posY);
				
		if(weakest_enemy == null) return;
			
		state.task = tasks.attack;
				
		state.enemy_being_attacked = weakest_enemy;
			
	}
	

	
	public Position find_non_hitting_position(int repetitions,State state,double Height,double Width,
			double current_x,double current_y,String[] Teammates,double gunHeading) {
			
			
			double new_x=0;
			double new_y=0;
			
			double distance,lowest_distance = Double.POSITIVE_INFINITY;
			
			double estimated_radius = 100;
			
			for(int i = 0;i < repetitions;i++) {
				double x = Math.random()*(Width - 2*estimated_radius) + estimated_radius;
				double y = Math.random()*(Height - 2*estimated_radius) + estimated_radius;
				
				if(!hits_teammates(state,x,y,Teammates,gunHeading) && far_enough_from_others(x,y,state)) {
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
	
	
	public Boolean hits_teammates(State state,double myX,double myY,String[] Teammates,double gunHeading){
		
		for(String s:Teammates) {
			InfoRobot info = state.InfoRobots.get(s);
			if(info != null && hits_robot(myX,myY,info.position.getX(),
					info.position.getY(),gunHeading)) 
				
				return true;
					
		}
		
		return false;
	}
	
	public Boolean hits_robot(double myX,double myY,double robotX,
			double robotY,double gun_angle) {
		
		double abs_angle = absoluteBearing(myX,myY,robotX,robotY);
		double distance = calcDist(myX,myY,robotX,robotY);
		double estimated_size = 25;
		
		double non_hitting_angle = Math.toDegrees(Math.atan(estimated_size/distance));
		
		if(normalizeBearing(gun_angle) <= normalizeBearing(abs_angle + non_hitting_angle)
				&& normalizeBearing(gun_angle) >= normalizeBearing(abs_angle - non_hitting_angle))
			return true;
		
		return false;
		
	}
	
	 public double calcDist (double xi, double yi, double xf, double yf) 
     { //calcular a distancia 
         return Math.sqrt(Math.pow((xf-xi), 2) + Math.pow((yf-yi), 2));
     }
	
	//calculate the absolute bearing between two points
		
		public double absoluteBearing(double x1, double y1, double x2, double y2) {
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
		
		
		public Boolean far_enough_from_others(double pointX,double pointY,State state) {
			
			double estimated_robot_radius = 50;
			
			
			for(String s:state.InfoRobots.keySet()) {
				Position position = state.InfoRobots.get(s).position;
				if(calcDist(pointX,pointY,position.getX(),position.getY()) < estimated_robot_radius)
						return false;
			}
			
			return true;
		}
		

		//normalizes the bearing value
		
		public double normalizeBearing(double angle) {
			while (angle >  180) angle -= 360;
			while (angle < -180) angle += 360;
			return angle;
		}

		
}
