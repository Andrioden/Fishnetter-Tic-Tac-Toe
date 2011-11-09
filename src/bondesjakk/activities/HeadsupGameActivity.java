package bondesjakk.activities;

import bondesjakk.game.Game;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class HeadsupGameActivity extends GameActivity {
	private final static String TAG = "HeadsupActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = (Game) getIntent().getSerializableExtra("Game");
        game.setActivity(this);
        super.startGame();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) { // Only need one dialog in this activity.
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Placeholder")
    	       .setCancelable(false)
    	       .setPositiveButton("Play again!", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   restart();
    	        	   dialog.cancel();
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
    
    @Override
    public void onPrepareDialog (int id, Dialog dialog) {
    	if (id==1) {
    		((AlertDialog) dialog).setMessage("The winner is "+lastWinner+"!");
    	}
    	else {
    		((AlertDialog) dialog).setMessage("Draw game!");
    	}
    
    }
}
