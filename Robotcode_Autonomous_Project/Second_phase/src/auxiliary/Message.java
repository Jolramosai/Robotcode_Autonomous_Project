package auxiliary;
import java.io.Serializable;

public class Message implements Serializable{
	
	public MessageType type;
	public double positionX;
	public double positionY;
	public double energy;
	public String robotName;
	
	public Message(MessageType type,double positionX,
			double positionY,double energy,String robotName) {
		this.type = type;
		this.positionX = positionX;
		this.positionY = positionY;
		this.robotName = robotName;
		this.energy = energy;
	}
	
	public Message(MessageType type,double positionX,double positionY) {
		
		this.type = type;
		this.positionX = positionX;
		this.positionY = positionY;
	}

}
