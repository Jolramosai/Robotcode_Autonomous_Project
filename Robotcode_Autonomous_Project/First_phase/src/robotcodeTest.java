import standardOdometer.*;
import myodometer.myOdometer;
import robocode.*;

import robocode.*;

public class robotcodeTest extends AdvancedRobot {
    private Odometer odometer = new Odometer("isRacing",this);
    private myOdometer myodometer = new myOdometer(this);
    
    public void run() {
        addCustomEvent(odometer);
    	while (true) {
            ahead(100);
            turnGunRight(360);
    		this.myodometer.increment_distance();
            back(100);
            turnGunRight(360);
            this.myodometer.increment_distance();
    	}
    }
 
    public void RoundEndedEvent(Event ev) {
    	this.myodometer.printTotalDistance();
    	this.myodometer.reset_distance();
    }
    
    public void onCustomEvent(CustomEvent ev) {
    	Condition cd = ev.getCondition();
    	if(cd.getName().equals("isRacing"))
    		this.odometer.getRaceDistance();
    }
    
    
    
    public void onScannedRobot(ScannedRobotEvent e) {
        //fire(1);
    }
}