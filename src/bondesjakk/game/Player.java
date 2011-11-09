package bondesjakk.game;

import java.io.Serializable;

public class Player implements Serializable{
	private static final long serialVersionUID = 1L;
	public String name;
	public int netId; // Only used in a socket game to identify players.
	public int points;
	public boolean disconnected = false;
	public boolean isAI = false;
	
	public Player(String name, int netId) {
		this.name = name;
		this.netId = netId;
	}
	
	public Player(String name, int gameId, boolean isAI) {
		this.name = name;
		this.netId = gameId;
		this.isAI = isAI;
	}
	
	public void addWin() {
		points++;
	}
}
