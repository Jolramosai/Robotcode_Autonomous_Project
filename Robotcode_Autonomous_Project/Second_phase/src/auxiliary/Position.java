package auxiliary;

public class Position {
	private double x;
	private double y;


	public Position(double scannedX, double scannedY) {
		this.x = scannedX;
		this.y = scannedY;
	}
	
	public Position(Position p) {
		this.x = p.getX();
		this.y = p.getY();
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}
}
