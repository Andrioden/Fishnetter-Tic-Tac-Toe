package bondesjakk.activities;

import java.util.ArrayList;
import bonde.sjakk.R;
import bondesjakk.game.Game;
import bondesjakk.game.Player;
import bondesjakk.socket.Client;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.content.Intent;

public class JoinGameActivity extends GameActivity {
	public boolean isGameStarted = false;
	public ArrayList<String> outComStrings = new ArrayList<String>();
	public int yourNetId = 0;
	private Client client;
	private ProgressDialog conDialog;
	private String hostIP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join);
        EditText ipET = (EditText) findViewById(R.id.ipEditText);
        String lastIP = loadPref("fishnetter_ip");
        if (lastIP.equals("")) lastIP = "10.10.10.10";
        ipET.setText(lastIP);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) { // Only need one dialog in this activity.
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Placeholder")
    	       .setCancelable(false)
    	       .setNegativeButton("Close dialog", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   dialog.cancel();
    	           }
    	       });
    	return builder.create();
    }
    
    @Override
    public void onPrepareDialog (int id, Dialog dialog) {
    	if (id==1) {
    		((AlertDialog) dialog).setMessage("The winner is "+lastWinner+"!");
    	}
    	else {
    		((AlertDialog) dialog).setMessage("Draw game!");
    	}
    
    }
    
    /**
     * Triggers a client interrupt when the player pushes back button.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	if (client != null) {
        		client.interrupt();
        	}
        }
        return super.onKeyDown(keyCode, event);
    }
    
    // ************** UI ACTIONS
    
    @Override
    public boolean onCreateOptionsMenu(Menu meny){
    	meny.add(R.string.backToMenu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	if (item.getTitle().equals(getResources().getString(R.string.backToMenu))){
    		client.interrupt();
    		startActivity(new Intent(this, MenuActivity.class));
    	}
    	return true;
    }
    
    public void joinGame(View v) {
    	EditText nameET = (EditText) findViewById(R.id.nameEditText);
    	EditText ipET = (EditText) findViewById(R.id.ipEditText);
    	String name = nameET.getText().toString().trim();
    	hostIP = ipET.getText().toString().trim();
    	savePref("fishnetter_ip", hostIP);
    	client = new Client(this, name, hostIP);
    	client.start();
    	setContentView(R.layout.gamelobby);
    	ImageButton startButton = (ImageButton) findViewById(R.id.starthostgameButton);
    	startButton.setVisibility(View.GONE);
    	// Hide the input keyboard helper, since it stucks when joining the game.
    	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    	mgr.hideSoftInputFromWindow(nameET.getWindowToken(), 0);
    	mgr.hideSoftInputFromWindow(ipET.getWindowToken(), 0);
    	conDialog = ProgressDialog.show(JoinGameActivity.this, "" , "Connecting... ");
    }
    
    // ************** INTERNAL METHODS
    
    protected void tagSquareAttempt(int row, int col, int playerNumber) {
    	if (game.getCurrentPlayer().netId==yourNetId)	{
    		outComStrings.add("TAGGED,"+row+","+col+","+playerNumber);
    	}
    }
    
    private void savePref(String tag, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spe = prefs.edit();
        spe.putString(tag, value);
        spe.commit();
    }
    
    private String loadPref(String tag) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String checkForQuestion = prefs.getString(tag , "");
        return checkForQuestion;
    }
    
    // ************** THREAD ACTIVITY METHODS
    
    public void initGameObject(Game game) {
    	this.game = game;
    }
    
    public void updateLobbyView() {
    	TextView currentplayersText = (TextView) findViewById(R.id.currentplayersText);
    	currentplayersText.setText("Current players ("+game.players.size()+"):");
    	LinearLayout playerListLayout = (LinearLayout) findViewById(R.id.playerlistLayout);
    	playerListLayout.removeAllViews();
    	for (Player p : game.players) {
    		TextView nameView = new TextView(this);
    		nameView.setText(p.name);
    		nameView.setTextSize(20);
    		nameView.setBackgroundResource(R.drawable.gamelobby_nameplank);
    		playerListLayout.addView(nameView);
    	}
    }
    
    // ************** THREAD CALLBACK METHODS
    
    /**
     * Updates the lobby view.
     * Callback method for other threads, with handler.
     */
    public void newPlayerHandler(final String name) {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    			if (!isGameStarted) {
    				updateLobbyView();
    			}
    		}
    	});
    }
    
    public void startHandler() {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    			startGame();
    			isGameStarted = true;
    		}
    	});    	
    }

    /**
     * Recieved socket com that the hosted has started a new round.
     */
    public void restartHandler() {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    			restart();
    		}
    	});
    }
    
    /**
     * Recieved socket com that the hosted has quit.
     */
    public void hostQuitHandler(final int gameId) {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    		    Context context = getApplicationContext();
    		    Toast.makeText(context, "Host disconnected", Toast.LENGTH_LONG).show();
    		    game.setDisconnectedByNetId(gameId, true);
    		    if (isGameStarted) {
    		    	updateIngamelist();
    		    }
    		    else {
    		    	client.interrupt();
    		    	finish();
    		    }
    		}
    	});
    }
  
    /**
     * Recieved socket com that a client has quit.
     */
    public void clientQuitHandler(final int netId) {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    		    if (isGameStarted) {
        		    Context context = getApplicationContext();
        		    Toast.makeText(context, game.getPlayerByNetId(netId).name+" disconnected", Toast.LENGTH_LONG).show();
    		    	game.setDisconnectedByNetId(netId, true);
    		    	updateIngamelist();
    		    }
    		    else {
    		    	game.removePlayerByNetID(netId);
    		    	updateLobbyView();
    		    }
    		}
    	});
    }
  
    /**
     * The game has started, messaged from client.thread.
     */
    public void gameStartedHandler() {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    			setContentView(R.layout.join);
    		    Context context = getApplicationContext();
    		    Toast.makeText(context, "Game allready started!", Toast.LENGTH_LONG).show();
    		}
    	});
    }
    
    public void timedOutHandler() {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    		    Context context = getApplicationContext();
    		    Toast.makeText(context, "Unable to connect to game.", Toast.LENGTH_LONG).show();
    	    	conDialog.dismiss();
    	    	finish();
    		}
    	});
    }
    
    public void connectedHandler() {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    		    conDialog.dismiss();
    			TextView hostIPView = (TextView) findViewById(R.id.hostIP);
    			hostIPView.setText("Host IP: "+hostIP);
    		}
    	});
    }
}
