package bondesjakk.socket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import bondesjakk.activities.HostGameActivity;
import android.util.Log;

public class ClientSender extends Thread{
	private final static String TAG = "ClientSender";
	private final static int refreshRate = 300; //ms
	private HostGameActivity activity;
	private Socket s;
	
	public ClientSender(Socket s, HostGameActivity activity) {
		this.activity = activity;
		this.s = s;
	}
	
	public void run() {
		PrintWriter out		= null;
		
		try{
            out = new PrintWriter(s.getOutputStream(), true);
            String settingsString = "SETTINGS";
            settingsString +=","+activity.game.getWidth();
            settingsString +=","+activity.game.getHeight();
            settingsString +=","+activity.game.victory;
            settingsString +=","+activity.game.gameMode;
            out.println(settingsString);
            Log.i(TAG,"*** Settings sendt to client: "+settingsString);
            int comCounter = 0;
            while (!isInterrupted()) {
            	if (comCounter<activity.outComStrings.size()) {
                	String nextComString = activity.outComStrings.get(comCounter);
                	out.println(nextComString);
                    Log.i(TAG,"*** Message sendt to client: " + nextComString);
                    comCounter++;           		
            	}
            	else {
            		pause(refreshRate);
            	}
            }
        } catch (IOException e) {
            e.printStackTrace();
            interrupt();
        }finally{ // Closing all sockets.
        	try{
        		out.close();
            	s.close();
        	}catch(Exception e){}	            	
        }
	}
	
	private void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}
}
