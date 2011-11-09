package bondesjakk.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class AiEngine implements Serializable {
	private Game game;
	private Random rg = new Random();
	
	public AiEngine(Game game) {
		this.game = game;
	}
	
	// ************** INTERFACE METHODS
	
	public int[] getAiTag(int[][] gamePanel) {
		if (game.gameMode==1) {
			return getNormalTag(gamePanel);
		}
		else {
			return getDropTag(gamePanel);
		}
	}
	
	// ************** INTERNAL METHODS
	
	private int[] getNormalTag(int[][] gamePanel) {
		ArrayList<Cord> cords = getOpenCords(gamePanel);
		int rgInt = rg.nextInt(cords.size());
		Cord randomCord = cords.get(rgInt);
		return new int[]{randomCord.row, randomCord.col};
	}
	
	private int[] getDropTag(int[][] gamePanel) {
		ArrayList<Cord> cords = getOpenCords(gamePanel);
		int rgInt = rg.nextInt(cords.size());
		Cord randomCord = cords.get(rgInt);
		return new int[]{randomCord.row, randomCord.col};
	}
	
	private ArrayList<Cord> getOpenCords(int[][] gamePanel) {
		int rows = gamePanel.length;
		int cols = gamePanel[0].length;
		ArrayList<Cord> cords = new ArrayList<Cord>();
		for (int row=0; row<rows; row++) {
			for (int col=0; col<cols; col++) {
				if (gamePanel[row][col]==0) cords.add(new Cord(row,col));
			}
		}
		return cords;
	}
}