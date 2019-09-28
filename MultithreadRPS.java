import java.util.Random;
import java.util.Scanner;

class testing{
	public static void main(String[] args){
		Scanner scan = new Scanner(System.in);
		System.out.println("How many people are playing?");

		int playerNum = scan.nextInt();	//receive input for # of players

		while (playerNum < 2 || playerNum > 10){	//bounds checking
			System.out.println("You must have at least 2 players, and no more than 10\n" +
								"How many people are playing?");
			playerNum = scan.nextInt();
		}

		scan.close();	//close system input

		Game g = new Game(1000,playerNum);	//create game object with # of rounds and # of players
		g.gameStart();	//call gamestart method
	}
}


//------------------------------------------
class Game {

	private Player[] p;		
	private int roundCount,
				playerCount, 
				rockWin = 0,
				paperWin = 0,
				scissorWin = 0,
				drawCount = 0,
				finishedCount = 0,
				ready = 0;

	Game(int x, int y){		//Game constructor

		roundCount = x;		//assign round count
		playerCount = y;	//assign player count

		p = new Player[playerCount];	//assign p a player array of size player count

		for (int i = 0; i < playerCount; i++){	
			p[i] = new Player(this,i+1,roundCount);	//assign each player a new player object with reference to this game & an id #
		}//end for

	}//end Game constructor

	public synchronized void gameStart(){

		for (int i = 0; i < playerCount; i++){	
			p[i].start();	//start each player object in the player array
		}//end for
		
	}//end gameStart

	public synchronized void waitHere(int roundNum){

		if (ready != playerCount-1) {	//if not all other players are ready
			ready++;	//inc ready
			try {		
				this.wait();
			}//end try
			catch (InterruptedException e) {}
		}//end if

		else {
			ready = 0;	//reset ready count for next round
			compare(roundNum);	//call compare for the given round
			this.notifyAll();	//release all threads to continue execution
		}//end else

	}//end waitHere

	public synchronized void compare(int round){
		boolean win = false;	//flag to check for a win

		for (int i = 0; i < playerCount; i++){	//loop to get a player to compare against other players
			int beat = 0;	//counter to check if player beat all other players

			for (int j = 0; j < playerCount && !win; j++){	//loop to get second player to compare against
				if (j != i){	//check to make sure you aren't comparing a player against themselves

					if ((p[i].getSymbol() == 1 && p[j].getSymbol() == 3) || 	// check for every case that the first
						(p[i].getSymbol() == 2 && p[j].getSymbol() == 1) ||		// player can win against the other player
						(p[i].getSymbol() == 3 && p[j].getSymbol() == 2)){

						beat++;	//if first player beat second, inc beat count

						if (beat == playerCount-1){	//if player beat all others, they won the round

							win = true;	//set win flag
							System.out.println("Round " + round + ": Player " + (i+1) + " wins\n");

							switch (p[i].getSymbol()){	//check for what the player drew, and inc the appropriate win count
								case 1: rockWin++;
										break;
								case 2: paperWin++;
										break;
								case 3: scissorWin++;

							}//end switch
						}//end double nested if	
					}//end nested if
				}//end if
			}//end nested for
		}//end for
		
		if (!win){	//if no one won, it is a draw. output that and inc draw count
			System.out.println("Round " + round + ": It's a draw\n");
			drawCount++;
		}

	}//end compare

	public void stats(){
		finishedCount++;
		if (finishedCount == playerCount){	//once all theads have called stat and finished execution output stats
			System.out.println("\n\n\n Statistics:\n" +
								"Number of draws: " + drawCount +
								"\nNumber of times scissors won: " + scissorWin +
								"\nNumber of times rock won: " + rockWin + 
								"\nNumber of times paper won: " + paperWin);		

		}//end if
	}//end stats
	
}//end game

//-----------------------------------------------
class Player extends Thread{

	private Game g;
	private Random gen = new Random();
	private int symbol,
				id,
				roundCount;


	Player(Game x, int y, int z){
		g = x;
		id = y;
		roundCount = z;
	}

	public void run(){
		System.out.println("Player thread " + id + " is running");

		for (int i = 0; i < roundCount; i++){
			newSymbol(i+1);
			g.waitHere(i+1);
		}

		System.out.println("Player thread " + id + " is finishing");
		g.stats();
	}

	public void newSymbol(int roundNum){
		symbol = gen.nextInt(3) + 1;	//random num generation between 1 and 3
		switch (symbol){
			case 1: System.out.println("Round " + roundNum + ": Player " + id + " selects rock");
					break;
			case 2: System.out.println("Round " + roundNum + ": Player " + id + " selects paper");
					break;
			case 3: System.out.println("Round " + roundNum + ": Player " + id + " selects scissors");
		}
	}

	public int getSymbol(){
		return symbol;
	}

}//end Player