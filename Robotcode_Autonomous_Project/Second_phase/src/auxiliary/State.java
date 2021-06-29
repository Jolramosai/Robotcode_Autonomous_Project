package auxiliary;
import java.util.HashMap;

public class State {
	// task the robot is currently doing
	public tasks task;
	//saves info from all the robots in the game
	public HashMap<String,InfoRobot> InfoRobots;
	//enemy currently being attacked possibly null
	public String enemy_being_attacked;
	//current leader
	public String leader;
	//saves the current number of enemies
	public int number_of_enemies;
	
	public Boolean hitWall;
	
	public String robot_being_protected;
	
	public Boolean first_swarm_move;
	
	public Boolean hits_wrong_target;
	
	public int hitsRobot;
	
	public State() {
		this.InfoRobots = new HashMap<String,InfoRobot>();
		this.leader = null;
		this.task = tasks.free;
		this.robot_being_protected = null;
		this.hitWall = false;
		this.first_swarm_move = false;
		this.hitsRobot = 0;
		this.hits_wrong_target = true;
	}
	
	public void updateInfoRobot(InfoRobot info){
		InfoRobots.put(info.name, info);
	}
	
	 public InfoRobot enemyInfo()
     {
		return InfoRobots.get(enemy_being_attacked);
	}

	public void removeInfoBot(String name)
     {
             InfoRobots.remove(name);
		return;
	}
	
}
