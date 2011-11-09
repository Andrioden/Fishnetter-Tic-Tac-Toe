package bondesjakk.activities;


import java.util.ArrayList;

import bonde.sjakk.R;
import bondesjakk.game.Game;
import bondesjakk.game.Player;
import bondesjakk.socket.Server;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HostGameActivity extends GameActivity {
	public boolean isGameStarted = false;
	public ArrayList<String> outComStrings = new ArrayList<String>();
	public ArrayList<String> innComStrings = new ArrayList<String>();
	private int yourNetId = 1;
	private Server server;
	private ProgressDialog hostDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = (Game) getIntent().getSerializableExtra("Game");
        setContentView(R.layout.gamelobby);
        server = new Server(this);
        server.start();
        hostDialog = ProgressDialog.show(HostGameActivity.this, "" , "Hosting... ");
    }
    
    @Override
    protected Dialog onCreateDialog(int id) { // Only need one dialog in this activity.
    	if ((id==1)||(id==2)) { 
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("Placeholder")
	    	       .setCancelable(false)
	    	       .setPositiveButton("Play again!", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   restart();
	    	        	   dialog.cancel();
	    	        	   outComStrings.add("RESTART");
	    	           }
	    	       })
	    	       .setNegativeButton("Close dialog", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	        	   dialog.cancel();
	           		       Context context = getApplicationContext();
	           		       Toast.makeText(context, "Menu to start new game", Toast.LENGTH_LONG).show();
	    	           }
	    	       });
	    	return builder.create();
    	}
    	else {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	final EditText input = new EditText(this);
	    	builder.setMessage("Pick a name!")
				   .setView(input)
				   .setCancelable(false)
				   .setPositiveButton("Registrer", new DialogInterface.OnClickListener() {
				       public void onClick(DialogInterface dialog, int id) {
				    	   String inputName = input.getText().toString().trim();
				    	   game.addPlayer(inputName,yourNetId); // Host is id 1
				    	   outComStrings.add("JOIN,"+inputName+","+yourNetId);
				    	   updateHostLobbyView();
				       }
				   });
	    	return builder.create();    		
    	}
    }
    
    @Override
    public void onPrepareDialog (int id, Dialog dialog) {
    	if (id==1) {
    		((AlertDialog) dialog).setMessage("The winner is "+lastWinner+"!");
    	}
    	else if (id==2){
    		((AlertDialog) dialog).setMessage("Draw game!");
    	}
    
    }
    
    /**
     * Triggers a server interrupt when the player pushes back button.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            server.interrupt();
        }
        return super.onKeyDown(keyCode, event);
    }
    
    // ************** UI ACTIONS
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	if (item.getTitle().equals(getResources().getString(R.string.newGame))){
    		restart();
    		outComStrings.add("RESTART");
    	}
    	return true;
    }
    
    public void lobbyGameStart(View v) {
    	isGameStarted = true;
    	startGame();
    	outComStrings.add("START");
    }
    
    // ************** INTERNAL METHODS
    
    private void updateHostLobbyView() {
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
    	// Unique for host
    	if (game.players.size()>1) {
    		ImageButton startButton = (ImageButton) findViewById(R.id.starthostgameButton);
    		startButton.setVisibility(View.VISIBLE);
    	}
    }
    
    protected void tagSquareAttempt(int row, int col, int playerNumber) {
    	Log.i("tagSqyareAT", "currentplayer: "+game.getCurrentPlayer().netId);
    	if (game.getCurrentPlayer().netId==yourNetId)	{
    		outComStrings.add("TAGGED,"+row+","+col+","+playerNumber);
    		super.tagSquareAttempt(row, col, playerNumber);
    	}
    }
   
    // ************** THREAD CALLBACK METHODS
    
    /**
     * Updates the lobby view.
     * Callback method for other threads, using an handler.
     */
    public void newPlayerHandler(final String name) {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    			if (!isGameStarted) {
        		    Context context = getApplicationContext();
        		    Toast.makeText(context, name+" connected", Toast.LENGTH_LONG).show();
    				updateHostLobbyView();
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
    		    Context context = getApplicationContext();
    		    Toast.makeText(context, game.getPlayerByNetId(netId).name+" disconnected", Toast.LENGTH_LONG).show();
    		    if (isGameStarted) {
    		    	game.setDisconnectedByNetId(netId, true);
    		    	updateIngamelist();
    		    }
    		    else {
    		    	game.removePlayerByNetID(netId);
    		    	updateHostLobbyView();
    		    }
    		}
    	});
    }
    
    /**
     * Recieved socket com that a client has quit.
     */
    public void setHostIP(final String IP) {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    			TextView hostIPView = (TextView) findViewById(R.id.hostIP);
    			hostIPView.setText("Your IP: "+IP);
    		}
    	});
    }
    
    public void hostingSuccessful() {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    			hostDialog.dismiss();
    			showDialog(3);
    		}
    	});
    }
    
    public void hostingFail() {
    	handler.post(new Thread() {
    		@Override
    		public void run() {
    			hostDialog.dismiss();
    			finish();
    		}
    	});
    }
}
