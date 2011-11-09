package bondesjakk.activities;

import bonde.sjakk.R;
import bondesjakk.game.Game;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	private final static String TAG = "SettingsActivity";
	// These settings define the slider values.
	private final static int MIN_PLAYERS = 0;
	private final static int MAX_PLAYERS = 6;
	private final static int MIN_AI = 0;
	private final static int MIN_VIC_COND = 3;
	private final static int MIN_WIDTH = 3;
	private final static int MAX_WIDTH = 20;
	private final static int MIN_HEIGHT = 3;
	private final static int MAX_HEIGHT = 20;
	// Other object variables.
	private String gametype;
	private SeekBar pcSeekBar;
	private SeekBar aiSeekBar;
	private SeekBar vSeekBar;
	private SeekBar widthSeekBar;
	private SeekBar heightSeekBar;
	private boolean userChangedSizeSettings = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        gametype = (String) getIntent().getSerializableExtra("gametype");
        if (gametype.equals("HEADSUP")) {
        	LinearLayout pcLayout = (LinearLayout) findViewById(R.id.playercountLayout);
        	pcLayout.setVisibility(View.VISIBLE);
        	LinearLayout aiLayout = (LinearLayout) findViewById(R.id.aiplayerLayout);
        	aiLayout.setVisibility(View.VISIBLE);
        }
        else {
        	LinearLayout pcLayout = (LinearLayout) findViewById(R.id.playercountLayout);
        	pcLayout.setVisibility(View.GONE);
        	LinearLayout aiLayout = (LinearLayout) findViewById(R.id.aiplayerLayout);
        	aiLayout.setVisibility(View.GONE);
        }
        initSettingsElements();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    }
    
    // ************** UI ACTIONS
    
    public void startJoinGame(View v) {
		Intent i = new Intent(this, JoinGameActivity.class);
		startActivity(i);
    }
    
    public void startGame(View v) {
		if (gametype.equals("HEADSUP")) {
			startHeadsupGame();
		}
		else if (gametype.equals("HOST")) {
			startHostGame();
		}
    }

    // ************** INTERNAL METHODS
    
    private void initSettingsElements() {
    	// Init game PLAYER COUNT choice SeekBar
    	pcSeekBar = (SeekBar) findViewById(R.id.playercountSeekBar);
    	pcSeekBar.setMax(MAX_PLAYERS-MIN_PLAYERS);
    	pcSeekBar.setProgress(2);
    	pcSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    	// Init game AI COUNT choice SeekBar
    	aiSeekBar = (SeekBar) findViewById(R.id.aiplayerSeekBar);
    	aiSeekBar.setMax(MAX_PLAYERS-MIN_AI);
    	aiSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    	// Init game WIDTH choice SeekBar
    	widthSeekBar = (SeekBar) findViewById(R.id.widthtSeekBar);
    	widthSeekBar.setMax(MAX_WIDTH-MIN_WIDTH);
    	widthSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    	// Init game HEIGHT choice SeekBar
    	heightSeekBar = (SeekBar) findViewById(R.id.heightSeekBar);
    	heightSeekBar.setMax(MAX_HEIGHT-MIN_HEIGHT);
    	heightSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    	// Init game VICTORY CONDITION choice SeekBar
    	vSeekBar = (SeekBar) findViewById(R.id.victorySeekBar);
    	vSeekBar.setMax(biggestNumber(MIN_WIDTH,MIN_HEIGHT)-MIN_VIC_COND);
    	vSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }
    
    private void startHeadsupGame() {
		Intent i = new Intent(this, HeadsupGameActivity.class);
		int playerCount = MIN_PLAYERS+pcSeekBar.getProgress();
		int aiplayerCount = MIN_AI+aiSeekBar.getProgress();
		int victory = MIN_VIC_COND+vSeekBar.getProgress();
		int width = MIN_WIDTH+widthSeekBar.getProgress();
		int height = MIN_HEIGHT+heightSeekBar.getProgress();
		int gameMode = getGameModeChoice();
		Game game = new Game(width,height,victory,gameMode,playerCount, aiplayerCount);
		i.putExtra("Game", game);  
		startActivity(i);    	
    }
    
    private void startHostGame() {
		Intent i = new Intent(this, HostGameActivity.class);
		int victory = MIN_VIC_COND+vSeekBar.getProgress();
		int width = MIN_WIDTH+widthSeekBar.getProgress();
		int height = MIN_HEIGHT+heightSeekBar.getProgress();
		int gameMode = getGameModeChoice();
		Game game = new Game(width,height,victory,gameMode);
		i.putExtra("Game", game);  
		startActivity(i);
    }
  
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
    	@Override
    	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    		if (seekBar.getId()==R.id.playercountSeekBar) {
    			TextView tw = (TextView) findViewById(R.id.playercountNumberText);
    			int newPlayerCount = MIN_PLAYERS+progress;
    			tw.setText(" "+(newPlayerCount)+" ");
    			int aiCount = aiSeekBar.getProgress()+MIN_AI;
    			if ((newPlayerCount+aiCount)>MAX_PLAYERS) { // Human+AI players do not exceed max
    				aiCount = MAX_PLAYERS-newPlayerCount;
    				aiSeekBar.setProgress(aiCount);
    			}
    			if (!userChangedSizeSettings) {
        			widthSeekBar.setProgress(SuggestGameSizeByPC(newPlayerCount+aiCount)-MIN_WIDTH);
        			heightSeekBar.setProgress(SuggestGameSizeByPC(newPlayerCount+aiCount)-MIN_HEIGHT);    				
    			}
    		}
    		else if (seekBar.getId()==R.id.aiplayerSeekBar) {
    			TextView tw = (TextView) findViewById(R.id.aiplayerNumber);
    			int newAiCount = MIN_AI+progress;
    			tw.setText(" "+(newAiCount)+" ");
    			int playerCount = pcSeekBar.getProgress()+MIN_PLAYERS;
    			if ((newAiCount+playerCount)>MAX_PLAYERS) { // Human+AI players do not exceed max
    				playerCount = MAX_PLAYERS-newAiCount;
    				pcSeekBar.setProgress(playerCount);
    			}
    			if (!userChangedSizeSettings) {
        			widthSeekBar.setProgress(SuggestGameSizeByPC(newAiCount+playerCount)-MIN_WIDTH);
        			heightSeekBar.setProgress(SuggestGameSizeByPC(newAiCount+playerCount)-MIN_HEIGHT);    				
    			}
    		}    		
    		else if (seekBar.getId()==R.id.widthtSeekBar) {
    			if (fromUser) userChangedSizeSettings = true;
    			TextView tw = (TextView) findViewById(R.id.widthNumberText);
    			tw.setText(" "+(MIN_WIDTH+progress)+" ");
    			int newWidth = MIN_WIDTH+progress;
    			int height = MIN_HEIGHT+heightSeekBar.getProgress();
    			vSeekBar.setMax(biggestNumber(newWidth,height)-MIN_VIC_COND);
    		}
    		else if (seekBar.getId()==R.id.heightSeekBar) {
    			if (fromUser) userChangedSizeSettings = true;
    			TextView tw = (TextView) findViewById(R.id.heightNumberText);
    			tw.setText(" "+(MIN_HEIGHT+progress)+" ");
    			int newHeight = MIN_HEIGHT+progress;
    			int width = MIN_WIDTH+widthSeekBar.getProgress();
    			vSeekBar.setMax(biggestNumber(newHeight,width)-MIN_VIC_COND);
    		}
    		else if (seekBar.getId()==R.id.victorySeekBar) {
    			TextView tw = (TextView) findViewById(R.id.victoryNumberText);
    			tw.setText(" "+(MIN_VIC_COND+progress)+" ");    			
    		}
    	}
    	@Override // Kept synt error otherwise
    	public void onStartTrackingTouch(SeekBar seekBar) {}
    	@Override // Kept synt error otherwise
    	public void onStopTrackingTouch(SeekBar seekBar) {}
    };
    
    private int biggestNumber(int num1, int num2) {
    	if (num1>num2) return num1;
    	else return num2;
    }
    
    // Formula for suggestion what format the game table should be at in a NxN format.
    private int SuggestGameSizeByPC(int pc) {
    	return Math.round(pc*3/2);
    }
    
    private int getGameModeChoice() {
    	RadioButton rbOne = (RadioButton) findViewById(R.id.gameModeOne);
    	if (rbOne.isChecked()) return 1;
    	RadioButton rbTwo = (RadioButton) findViewById(R.id.gameModeTwo);
    	if (rbTwo.isChecked()) return 2;
    	return -1;
    }
}