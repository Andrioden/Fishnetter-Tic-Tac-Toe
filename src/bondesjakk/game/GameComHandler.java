package bondesjakk.game;

import android.util.Log;
import bondesjakk.activities.HostGameActivity;
import bondesjakk.activities.JoinGameActivity;

public class GameComHandler {
	private JoinGameActivity JGA = null;
	private HostGameActivity HGA = null;
	
	public GameComHandler(JoinGameActivity JGA) {
		this.JGA = JGA;
	}
	
	public GameComHandler(HostGameActivity HGA) {
		this.HGA = HGA;
	}
	
	/**
	 * This method handles the initial game settings communication, only run at the start by clients.
	 * @param msg in string format [SETTINGS,weight,height,victor]".
	 */
	public void gameSettingsHandler(String msg) {
		String[] msgSplit = msg.split(",");
		String type = msgSplit[0];
		if (type.equals("SETTINGS")) {
			int gameWidth = Integer.parseInt(msgSplit[1]);
			int gameHeight = Integer.parseInt(msgSplit[2]);
			int gameVictory = Integer.parseInt(msgSplit[3]);
			int gameMode = Integer.parseInt(msgSplit[4]);
			if (JGA!=null) {
				JGA.initGameObject(new Game(gameWidth,gameHeight,gameVictory, gameMode));
			}
		}
	}
	
	/**
	 * This method handles the games communication strings.
	 * @param msg in string format [ACTION_TYPE,ATTRIBUTT].
	 */
	public void gameMessageHandler(String msg) {
		String[] msgSplit = msg.split(",");
		String type = msgSplit[0];
		if (type.equals("JOIN")) {
			String atr = msgSplit[1];
			int gameId = Integer.parseInt(msgSplit[2]);
			addPlayer(atr, gameId);
		}
		else if (type.equals("START")) {
			start();
		}
		else if (type.equals("TAGGED")) {
			String row = msgSplit[1];
			String col = msgSplit[2];
			String playerNumber = msgSplit[3];
			tag(row,col,playerNumber);
		}
		else if (type.equals("RESTART")) {
			restart();
		}
		else if (type.equals("HOST_QUIT")) {
			int gameId = Integer.parseInt(msgSplit[1]);
			hostQuit(gameId);
		}
		else if (type.equals("CLIENT_QUIT")) {
			int gameId = Integer.parseInt(msgSplit[1]);
			clientQuit(gameId);			
		}
	}
	
	// ************** INTERNAL METHODS

	private void addPlayer(String name, int gameId) {
		if (JGA!=null) {
			JGA.game.addPlayer(name,gameId);
			JGA.newPlayerHandler(name);
		}
		else {
			HGA.game.addPlayer(name,gameId);
			HGA.newPlayerHandler(name);
		}
	}
	
	private void start() {
		if (JGA!=null) {
			JGA.startHandler();
		}	
	}
	
	private void tag(String sRow, String sCol, String playerNumber) {
		int row = Integer.parseInt(sRow);
		int col = Integer.parseInt(sCol);
		int pn = Integer.parseInt(playerNumber);
		if (JGA!=null) {
			JGA.tagHandler(row, col, pn);
		}
		else {
			HGA.tagHandler(row, col, pn);
		}
	}
	
	private void restart() {
		if (JGA!=null) {
			JGA.restartHandler();
		}			
	}
	
	private void hostQuit(int gameId) {
		if (JGA!=null) {
			JGA.hostQuitHandler(gameId);
		}	
	}
	
	private void clientQuit(int gameId) {
		if (JGA!=null) {
			JGA.clientQuitHandler(gameId);
		}
		else {
			HGA.clientQuitHandler(gameId);
		}
	}
}
