package bondesjakk.game;

import java.io.Serializable;
import java.util.ArrayList;

import bondesjakk.activities.GameActivity;
import android.util.Log;

public class Game implements Serializable {
	private final static String TAG = "GameClass";
	private static final long serialVersionUID = 1L;
	
	// ************** OBJECT VARIABLES
	
	public int[][] gamePanel;
	public int victory;
	public int gameMode;
	public ArrayList<Player> players = new ArrayList<Player>();
	private int currentPlayer = 1;
	private int uniqueIdCounter = 1;
	private int gameCount = 0;
	private int tagCount = 0;
	private AiEngine aie;
	private GameActivity activity;
	
	// ************** CONSTRUCTORS
	
	public Game(int width, int height, int victory, int gameMode, int playercount, int aiplayercount) {
		gamePanel = new int[height][width];
		this.victory = victory;
		this.gameMode = gameMode;
		for (int i=0; i<playercount; i++) {
			players.add(new Player("Player "+(i+1), 0));
		}
		for (int i=0; i<aiplayercount; i++) {
			players.add(new Player("AI "+(i+1), 0, true));
		}
		aie = new AiEngine(this);
	}

	public Game(int width, int height, int victory, int gameMode ) {
		gamePanel = new int[height][width];
		this.victory = victory;
		this.gameMode = gameMode;
	}
	
	// ************** GET METHODS
	
	public String getCurrentPlayerName() {
		return players.get(currentPlayer-1).name;
	}
	
	public int getCurrentPlayerId() {
		return currentPlayer;
	}
	
	public Player getCurrentPlayer() {
		return players.get(currentPlayer-1);
	}
	
	public Player getPlayerByNetId(int givenId) {
		for (Player p : players) {
			if (p.netId==givenId) return p;
		}
		return null;
	}
	
	public int getWidth() {
		return gamePanel[0].length;
	}
	
	public int getHeight() {
		return gamePanel.length;
	}
	
	// ************** SET METHODS
	
	public void setDisconnectedByNetId(int givenId, boolean givenDisc) {
		for (Player p : players) {
			if (p.netId==givenId) p.disconnected = givenDisc;
		}
	}
	
	public void setActivity(GameActivity activity) {
		this.activity = activity;
	}
	
	
	// ************** INTERFACE METHODS

    /** 
     * The current player tags one square of the game as his.
     * Takes row and col number and playerNumber
     */ 	
	public int[] tagSquare(int row, int col, int playerNumber) {
		if (gamePanel[row][col]==0) {
			tagCount++;
			if (gameMode==1) { // Normal
				gamePanel[row][col] = playerNumber;
				Log.i(TAG, playerNumber+" tagged ["+row+"]["+col+"]");
				Log.i(TAG, toString());
				return new int[]{row,col}; // The tag was accepted;				
			}
			else if (gameMode==2) { // Dropped (falls to the bottom)
				for (int i=getHeight()-1; i>=0; i--) {
					if (gamePanel[i][col]==0) {
						gamePanel[i][col] = playerNumber;
						Log.i(TAG, playerNumber+" tagged ["+i+"]["+col+"]");
						Log.i(TAG, toString());
						return new int[]{i,col};						
					}
				}
				Log.i(TAG,"nofoundtag: "+0+","+0);
				return new int[]{0,0};
			}
		}
		else {
			return new int[]{-1,-1}; // The tag did not go trough;
		}
		return null;
	}

    /** 
     * Adds a new player to the game;
     */ 	
	public void addPlayer(String name, int netId) {
		players.add(new Player(name,netId));
		Log.i(TAG, "** Player ("+netId+") joined game :"+name);
	}
	
	public void removePlayerByNetID(int netId) {
		for (Player p : players) {
			if (p.netId == netId) {
				players.remove(p);
				break;
			}
		}
	}
	
    /** 
     * Start a new round
     */ 	
	public void restart() {
		gameCount++;
		currentPlayer = (gameCount % players.size())+1;
		gamePanel = new int[getHeight()][getWidth()];
		tagCount = 0;
		start();
	}
	
    /** 
     * Start the game
     */ 	
	public void start() {
		if (getCurrentPlayer().isAI) {
			aiTurn();
		}
	}
	
	
	
    /** 
     * Checks if the game got a winner and return the winner Player object,
     * if there is no winner; returns null
     */ 	
	public Player checkForWinner(boolean internalCheck) {
		// Check if someone won horisontaly
		for (int i=0; i<gamePanel.length; i++) {
			int prevPN = 0;
			int chaining = 1;
			for (int y=0; y<gamePanel[i].length; y++) {
				int curPN = gamePanel[i][y]; // Sets current player number
				if ((curPN!=0)&&(curPN==prevPN)) { // Detected chaining
					chaining++; 
					if (chaining==victory) { // If number of chains equals victory condition
						Player winner = players.get(curPN-1);
						if (!internalCheck) winner.addWin();
						return winner;
					}
				}
				else { // Chain broken
					chaining = 1;
				}
				prevPN = curPN;
			}
		}
		// Check if someone won vertically
		for (int i=0; i<gamePanel[0].length; i++) {
			int prevPN = 0;
			int chaining = 1;
			for (int y=0; y<gamePanel.length; y++) {
				int curPN = gamePanel[y][i]; // Sets current player number
				if ((curPN!=0)&&(curPN==prevPN)) { // Detected chaining
					chaining++; 
					if (chaining==victory) { // If number of chains equals victory condition
						Player winner = players.get(curPN-1);
						if (!internalCheck) winner.addWin();
						return winner;
					}
				}
				else { // Chain broken
					chaining = 1;
				}
				prevPN = curPN;
			}
		}
		// Check if someone won across
		for (int i=0; i<gamePanel.length; i++) {
			for (int y=0; y<gamePanel[i].length; y++) {
				int prevPN = gamePanel[i][y]; 
				int chaining = 1;
				int c = 1;
				while (prevPN!=-1) { // Searching for crossing Down-Right
					int curPN = getLowerRight(i,y,c);
					if ((curPN!=0)&&(curPN==prevPN)) { // Detected chaining
						chaining++; 
						if (chaining==victory) { // If number of chains equals victory condition
							Player winner = players.get(curPN-1);
							if (!internalCheck) winner.addWin();
							return winner;
						}
					}
					else { // Chain broken
						chaining = 1;
					}
					c++;
					prevPN = curPN;
				}
				prevPN = gamePanel[i][y]; 
				chaining = 1;
				c = 1;
				while (prevPN!=-1) { // Searching for crossing Up-Right
					int curPN = getUpperRight(i,y,c);
					if ((curPN!=0)&&(curPN==prevPN)) { // Detected chaining
						chaining++; 
						if (chaining==victory) { // If number of chains equals victory condition
							Player winner = players.get(curPN-1);
							if (!internalCheck) winner.addWin();
							return winner;
						}
					}
					else { // Chain broken
						chaining = 1;
					}
					c++;
					prevPN = curPN;
				}
			}
		}		
		return null;
	}
	
	/*
	 * Checks if all the game is draw,
	 * return true if draw, false otherwise
	 */
	public boolean checkForDraw() {
		int squares = getWidth()*getHeight();
		if (tagCount==squares) return true;
		else return false;
	}
	
	/*
	 *  Initiates next step of game, including AI player turns
	 */
	public void next() {
		if ((!checkForDraw())&&checkForWinner(true)==null) {
			if (currentPlayer==players.size()) currentPlayer = 1;
			else currentPlayer++;
			Log.i(TAG,"currentPlayer: "+currentPlayer+" isAI: "+getCurrentPlayer().isAI);
			if (getCurrentPlayer().isAI) {
				aiTurn();
			}
		}
	}
	
	/*
	 * Returns the next unique player id
	 */
	public int getNextPlayerId() {
		uniqueIdCounter++;
		return uniqueIdCounter;
	}

    /** 
     * Print out the game panel values.
     */ 
	public String toString() {
		String returnString ="";
		for (int i=0; i<gamePanel.length; i++) {
			for (int y=0; y<gamePanel[i].length; y++) {
				int curPN = gamePanel[i][y]; // Sets current player number
				returnString += "["+curPN+"] ";
			}
			returnString += "\n";
		}
		return returnString;
	}
	
	// ************** INTERNAL METHODS
	
    /** 
     * Returns the lower right times the (offset) tagged 
     * number on the game panel relative to given row and col
     * Returns -1 if went out of bonds.
     */ 
	private int getLowerRight(int row, int col, int offset) {
		try {
			return gamePanel[row+offset][col+offset];
		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
	}

	
    /** 
     * Returns the upper right times the (offset) tagged 
     * number on the game panel relative to given row and col
     * Returns -1 if went out of bonds.
     */ 
	private int getUpperRight(int row, int col, int offset) {
		try {
			return gamePanel[row-offset][col+offset];
		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
	}
	
	private void aiTurn() {
		int[] aiTag = aie.getAiTag(gamePanel);
		int[] returTags = tagSquare(aiTag[0], aiTag[1], currentPlayer);
		activity.aiTagHandler(returTags[0], returTags[1], currentPlayer);
	}	
}