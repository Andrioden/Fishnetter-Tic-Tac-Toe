package bondesjakk.activities;

import bonde.sjakk.R;
import bondesjakk.game.Game;
import bondesjakk.game.Player;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class GameActivity extends Activity {
	private final static String TAG = "GameActivity";
	public Game game;
	public String lastWinner = null;
	protected Handler handler = new Handler();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    // ************** UI ACTIONS
    
    @Override
    public boolean onCreateOptionsMenu(Menu meny){
    	super.onCreateOptionsMenu(meny);
    	meny.add(R.string.newGame);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	if (item.getTitle().equals(getResources().getString(R.string.newGame))){
    		restart();
    	}
    	return true;
    }
    
    // ************** INTERNAL METHODS
    
    protected void startGame() {
        setContentView(R.layout.game);
        initGameSquares();
        updateIngamelist();
        Log.i(TAG,"** Game ready for play **");
        Log.i(TAG,"Turn: "+game.getCurrentPlayerName());
        updateIngamelist();
        game.start();
    }
    
    /**
     * Next turn, checks if there is a winner first.
     */
    private void next() {
		Player winner = game.checkForWinner(false);
		if (winner==null) { // There is no winner
	    	boolean draw = game.checkForDraw();
	    	if (draw) {
	    		showDialog(2);
	    	}
		}
		else {
			Log.i(TAG,"** WINNER IS: "+winner.name);
			lastWinner = winner.name;
			showDialog(1);
		}
		game.next();
		updateIngamelist();
    }
    
    void restart() {
		game.restart();
		initGameSquares();
		updateIngamelist();
		lastWinner = null;
		Context context = getApplicationContext();
		Toast.makeText(context, game.getCurrentPlayerName()+" starts this round", Toast.LENGTH_SHORT).show();
    }
    
    private void initGameSquares() {
    	Log.i(TAG,"Initing game squares...");
    	LinearLayout gp = (LinearLayout) findViewById(R.id.gameLayout);
    	gp.removeAllViews();
    	int width = game.getWidth();
    	int height = game.getHeight();
    	for (int i=0; i<height; i++) {
    		LinearLayout row = new LinearLayout(this);
    		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    		lp.weight = 1.0f; // Makes sure the images scale
    		row.setLayoutParams(lp);
    		row.setOrientation(LinearLayout.HORIZONTAL);
    		for (int y=0; y<width; y++) {
    			ImageView square = new ImageView(this);
    			square.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f)); // Makes sure the images scale
    			square.setImageResource(R.drawable.square);
    			square.setOnClickListener(taqSquareListener(i, y));
    			row.addView(square);
    		}
    		gp.addView(row);
    	}
    }
    
    /*
     * This method updates the game info table
     */
    void updateIngamelist() {
        LinearLayout playerListLayout = (LinearLayout) findViewById(R.id.ingamelistLayout);
        playerListLayout.removeAllViews();
        for (int i=0; i<game.players.size(); i++) {
        	LinearLayout playerLayout = new LinearLayout(this);
        	// Nick
        	TextView nameText = new TextView(this);
        	nameText.setText(game.players.get(i).name);
        	nameText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD);
        	playerLayout.addView(nameText);
        	// Disconnected?
        	if (game.players.get(i).disconnected) {
            	TextView discText = new TextView(this);
            	discText.setText(" (disconnected)");
            	playerLayout.addView(discText);        		
        	}
        	// Adjusted image
        	ImageView iw = new ImageView(this);
        	iw.setMaxWidth(25);
        	iw.setMaxHeight(25);
        	iw.setAdjustViewBounds(true);
        	iw.setImageResource(getCurrentPlayerImageResource(i+1));
        	playerLayout.addView(iw);
        	// Score
        	TextView scoreText = new TextView(this);
        	scoreText.setText(" Score: "+game.players.get(i).points);
        	playerLayout.addView(scoreText);
        	// Adds arrow if its the players turn
        	if (game.getCurrentPlayerId()==(i+1)) {
            	TextView arrow = new TextView(this);
            	arrow.setText(" <---");
            	playerLayout.addView(arrow);
        	}
        	// Appends to table
        	playerListLayout.addView(playerLayout);
        }
    }
    
    private OnClickListener taqSquareListener(final int row, final int col) {
        return new OnClickListener() {
            public void onClick(View v) { 
            	tagSquareAttempt(row,col, game.getCurrentPlayerId());
            }
        };    	
    }
    
    protected void tagSquareAttempt(int row, int col, int playerNumber) {
    	int[] returTags = game.tagSquare(row, col, playerNumber);
    	if ((returTags[0]!=-1)&&(lastWinner==null)) {
    		tagIWFromCords(returTags[0],returTags[1], playerNumber);
    	}    	
    }
    
    private void tagIWFromCords(int row, int col, int playerNumber) {
    	LinearLayout gameLayout = (LinearLayout) findViewById(R.id.gameLayout);
    	LinearLayout rowLayout = (LinearLayout) gameLayout.getChildAt(row);
    	ImageView iw = (ImageView) rowLayout.getChildAt(col);
    	int imgResource = getCurrentPlayerImageResource(playerNumber);
    	iw.setImageResource(imgResource);
    	next();
    }
    
    private int getCurrentPlayerImageResource(int playerId) {
    	if (playerId==1) {
    		return R.drawable.square_p1;
    	}
    	else if (playerId==2) {
    		return R.drawable.square_p2;
    	}
    	else if  (playerId==3) {
    		return R.drawable.square_p3;
    	}
    	else if  (playerId==4) {
    		return R.drawable.square_p4;
    	}
    	else if  (playerId==5) {
    		return R.drawable.square_p5;
    	}
    	else if  (playerId==6) {
    		return R.drawable.square_p6;
    	}
    	else {
    		return R.drawable.square_err;
    	}
    }
    
    
    // ************** THREAD CALLBACK METHODS
    
    /**
     * Handler method for tagging a square.
     */
    public void tagHandler(final int row, final int col, final int playerNumber) {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    			Log.i(TAG,"handlertag: "+row+col+playerNumber);
    	    	int[] returTags = game.tagSquare(row, col, playerNumber);
    	    	if ((returTags[0]!=-1)&&(lastWinner==null)) {
    	    		tagIWFromCords(returTags[0],returTags[1], playerNumber);
    	    	}
    		}
    	});
    }
    
    /**
     * Handler method for tagging a square.
     */
    public void aiTagHandler(final int row, final int col, final int playerNumber) {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    			tagIWFromCords(row,col,playerNumber);
    		}
    	});
    }
}