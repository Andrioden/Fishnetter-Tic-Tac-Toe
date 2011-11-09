package bondesjakk.activities;

import bonde.sjakk.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MenuActivity extends Activity {
	private final static String TAG = "MenuActivity";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    }
    
    // ************** UI ACTIONS 	
    public void startHeadsUpGame(View v) {
    	Intent i = new Intent(this, SettingsActivity.class);
    	i.putExtra("gametype", "HEADSUP");
    	startActivity(i);
    }
    
    public void startHostGame(View v) {
    	Intent i = new Intent(this, SettingsActivity.class);
    	i.putExtra("gametype", "HOST");
    	startActivity(i);
    }
    
    public void startJoinGame(View v) {
		Intent i = new Intent(this, JoinGameActivity.class);
		startActivity(i);
    }
}