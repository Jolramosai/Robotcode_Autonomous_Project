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
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import java.awt.*;
import java.util.ArrayList;
import robocode.Condition;
import Bin.Position;
import robocode.RoundEndedEvent;
import robocode.BattleEndedEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import robocode.RobocodeFileOutputStream;
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
public class RobozitoMKcontadorepisodico extends AdvancedRobot 
{

    
    	
	int count = 0; //Ha quantas unidades estamos à procura do alvo // ele pode ter morrido entretanto ...
	double gunTurnAmt; // Amplitude de procura do scan
        
	String trackName; // Robot que estamos a fazer track atualmente
	int c = 0;
        boolean posInicial = false; // verifica se chega a posição inicial definida para  o inico da corrida
	boolean posDestino = false;
	int enemyX, enemyi; // Posição cartesiana do inimigo
	int contador; //define os passos
	ArrayList<String> listRobot = new ArrayList<String>(); //lista de robot
	private Position posEnemy;
	ArrayList<Position> listPos = new ArrayList<Position>();
	double distTotal = 0; //guarda a distancia total  
	double xi, yi; // guarda a posicao do robot
	double racio;
	String string_ler=""; 
	int numberEnemies;
	private Odometer odometer = new Odometer("isRacing",this);
    
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
		addCustomEvent(odometer);
                
        trackName = null; // Inicialmente não tem nenhum como alvo principal
		setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
		gunTurnAmt = 90; // Initialize gunTurn to 10
		enemyi=enemyX=18; //Vai por na posição inicial...
                
		numberEnemies = getOthers();
		contador = -1;
                
                

		// Life Cycle
		while (true) 
                {
                    if(!posDestino) // verifica se ja chegou á posição pretendida
			{ 
				if((int) getX() == enemyX  && (int) getY() == enemyi )
                                {	//Se chegar na posição pretendida
                                    
                                    if ((int) getX() == 18  && (int) getY() == 18 )  // tava com stress em esperar, por falta de melhor recorri a isto
                                    {
                                        for (int a = 0 ; a < 20 ; a++ )
                                        {
                                            doNothing();                                        }
                                    }
                                    posDestino = true; //permite que seja feito o scan()
                                    //Atualiza a posição do robot
                                    xi = getX();
                                    yi = getY();
                                   // out.println("Posição Atual: x:" + xi + " e y:" + yi);
                                    
                                    if (listRobot.size() == 0) ; // falta me meter aqui qualquer coisa ams para esta apresentação nao e preciso!!!!!!!
                                    // passar o perimetro para fora desta comdição pomos no fim a mostra lo guardamos as posições dos bot e calculamos no fim e apresentamos... ppdemos poir o bot a morrer no fim ----
                                    if((int) getX() == 18  && (int) getY() == 18 && listRobot.size() > 0)
                                    {
						                    out.println("ODOMETRO : " + distTotal); //odometro
											racio = calcPerim(listPos)/distTotal;
											//out.println("PerimObstaculos: " + calcPerim(listPos));
											//out.println("Rátio: " + racio);
											posDestino = false;							
											break;
                                    }
				}
				else
                                {
					goTo(enemyX,enemyi); //dirige-se para a posição inicial
					execute();
				}
			}
                        // turn the Gun (looks for enemy)
			turnGunRight(gunTurnAmt);
			// Keep track of how long we've been looking
			count++;
			if (count > 2) // Se não virmos o alvo depois 2 turnos mudamos os parametros
                        {
                            gunTurnAmt = 5;
			}
			
			if (count > 15) // Se passam 15 turnos e o alvo não é encontrado damos como desaparecido
                        {
				trackName = null;
			}
		}
	}

	
	/**
	 * onScannedRobot:  O que acontece quando apanhamos um robo no scan
	 */
	public void onScannedRobot(ScannedRobotEvent e) 
        {
		//out.println("numero: " + e.getRobots());
		if(posDestino) // se ja temos uma posiçao de destino 
                {
			// Se não é o target que queriamos entao passamos a frente para podermos evr os resultados de mais scans
                        if (trackName != null && !e.getName().equals(trackName)) 
                        {
				return;
			}

			
			if (trackName == null) // Se não existe target este fica como o primeiro alvo
                        {
				trackName = e.getName();
				//out.println("Tracking " + trackName);
			}
			//out.println("Numeros lista: "+ listRobot.size()+ " ,n inimigos "+ numberEnemies);
                        
			if(listRobot.size() == numberEnemies)
                        {// vai direcionar o robo para a meta
				enemyX = enemyi = 18;
			}	
			else
                        {
				if(!listRobot.contains(trackName))
                                { // se nao contiver o nome
                                    contador = -1;  
                                    listRobot.add(trackName);//adiciona o nome
                                    // Passa a ser o novo target e resetamos o contador
                                    count = 0;	
                                    double angleToEnemy = e.getBearing();
                                    // Calculate the angle to the scanned robot
                                    double angle = Math.toRadians((getHeading() + angleToEnemy % 360));

                                    // calcula as coordenadas do robo inimigo
                                    enemyX = (int)(getX() + Math.sin(angle) * e.getDistance());
                                    enemyi = (int)(getY() + Math.cos(angle) * e.getDistance());
                                    posEnemy = new Position (enemyX, enemyi);
                                    listPos.add (posEnemy);
                                   // out.println("Robot: " + trackName + " " + enemyX + " " + enemyi);
					

					// define a posição para que vai tendo em conta uma estimativa do comprimento do tanque para nao bater
					out.println(xi +" " + yi);
					
					if(xi <= enemyX && yi <= enemyi ){	
						enemyX -= 55;
						enemyi += 55;
						contador = 0;
					}
					else if(xi <= enemyX && yi >= enemyi){
						enemyX += 80;
						enemyi += 55;
						contador = 1;
					}
					else if(xi >= enemyX && yi <= enemyi){
						enemyX -= 55;
						enemyi -= 55;
						contador = 2;
					}
					else{
						enemyX += 55;
						enemyi -= 55;
						contador = 3;
					}
				}
				else { // movimenta-se de forma a obter um novo enemy
					if (contador == 0){						
						enemyX += 0;
					}
					else if (contador == 1){
						enemyi -= 0;
					}
					else if (contador == 2){
						enemyi  += 0;
					}
					else if (contador == 3){	
						enemyX -= 0;
					}	
				}
			}
			posDestino = false;
			distTotal += calcDist(getX(), getY(), enemyX, enemyi);
		//	out.println("c: " +contador);
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
	
	public double calcPerim (ArrayList<Position> lista) 
        {
            double perimTotal = 0;	
            double perimTriangular = 0;// poide interessar para alguma coisa
            for (int i = 0; i<lista.size()-1; i++) 
            {
                perimTotal = calcDist (lista.get(i).getX(), lista.get(i).getY(), lista.get(i+1).getX(), lista.get(i+1).getY());
                perimTotal++;
            }
            perimTriangular = perimTotal + calcDist (lista.get(lista.size()-1).getX(), lista.get(lista.size()-1).getY(), lista.get(0).getX(), lista.get(0).getY());
                
            perimTotal += calcDist (lista.get(lista.size()-1).getX(), lista.get(lista.size()-1).getY(), 18, 18) + calcDist (18, 18, lista.get(0).getX(), lista.get(0).getY());
		
            return perimTotal;		
	}
        
        public void goTo (int x, int y)  //vai calcular e direcionar o tanque reorrendo a funçoes trigonometricas
        {
    	double a;
    	setTurnRightRadians(Math.tan( a = Math.atan2(x -= (int) getX(), y -= (int) getY()) - getHeadingRadians()));
    	setAhead(Math.hypot(x, y) * Math.cos(a));
	}
        
        public void onBattleEnded(BattleEndedEvent event)
        {
		recolheEstado();
		double total=0;
		String [] valores = string_ler.split(",");
		for(String d: valores){
			total+= Double.parseDouble(d);
		}
		out.println("A distância total percorrida na batalha foi "+ total+ " px");
		guardaEstado(true);
	}
        
        public void onRoundEnded(RoundEndedEvent event)
        {
            
            recolheEstado();
            guardaEstado(false);
        }
        
        
         public void onCustomEvent(CustomEvent ev) {
	    	Condition cd = ev.getCondition();
	    	if(cd.getName().equals("isRacing"))
	    		this.odometer.getRaceDistance();
	    }
        //Read saved distance to add new distance (round) and, later, read all distances to get sum for battle
	public void recolheEstado () 
        {
	
            try 
            {
                BufferedReader reader = null;
		try 
                {
                    // Read file "count.dat" which contains 2 lines, distance for each round, and total distance for the battle
                    reader = new BufferedReader(new FileReader(getDataFile("count.dat")));
                    string_ler = reader.readLine();
                } 
		finally 
                {
                    if (reader != null) 
                    {
                        reader.close();
                    }
		}
            } 
            catch (IOException e) 
            {
		// Something went wrong reading the file, reset to 0.
            } 
            catch (NumberFormatException e) 
            {// Something went wrong converting to ints, reset to 0

            }
	}
        
        public void guardaEstado(boolean reset)
	{ //reset (erase the contents of the file) once the battle ends
		PrintStream w = null;
		try 
                {
                    w = new PrintStream(new RobocodeFileOutputStream(getDataFile("count.dat")));
                    if(string_ler == null)
                    {
			string_ler="";			
                    }
                    string_ler += distTotal +",";
                    if(reset)
                    {
			string_ler="";
			w.println(string_ler);
                    }
                    else			
                    w.println(string_ler);

                    if (w.checkError()) 
                    {
			out.println("I could not write the count!");
                    }
		} 
                catch (IOException e) 
                {
                    out.println("IOException trying to write: ");
                    e.printStackTrace(out);
		} 
                finally 
                {
                    if (w != null) 
                    {
                    	w.close();
                    }
		}
        }
        
        
}



