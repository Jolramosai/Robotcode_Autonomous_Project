package auxiliary;
import java.io.Serializable;

public class InfoRobot  {
	public RobotType type;
	public String name;
	public Position position;
	public double gun_angle;
	public double energy;
	
	public InfoRobot(RobotType type,String name,Position position,double energy) {
		this.type = type;
		this.name = name;
		this.position = position;
		this.energy = energy;
	}
	
}
