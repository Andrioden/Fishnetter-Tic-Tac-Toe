package bondesjakk.game;

import java.io.Serializable;

/*
 * CORD CLASS, simple class for storing coordinates
 */
class Cord implements Serializable {
	private static final long serialVersionUID = 1L;
	public int row;
	public int col;
	
	public Cord(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public String toString() {
		return "Row "+row+", Col "+col;
	}
}