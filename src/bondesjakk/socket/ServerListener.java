package bondesjakk.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import bondesjakk.activities.JoinGameActivity;
import bondesjakk.game.GameComHandler;

import android.util.Log;

public class ServerListener extends Thread{
	private final static String TAG = "ServerListener";
	private Socket s;
	private GameComHandler gch;
	
	public ServerListener(Socket s, JoinGameActivity activity) {
		this.s = s;
		this.gch = new GameComHandler(activity);
	}
	
	@Override
	public void run() {
		BufferedReader in 	= null;

		try{
			in = new BufferedReader(new InputStreamReader(s.getInputStream())); 
			String initialSettings = in.readLine().trim();//receive text from client
			Log.i(TAG,"*** Settings from server: " + initialSettings);
			gch.gameSettingsHandler(initialSettings);
            while (!isInterrupted()) {
                String res = in.readLine().trim();//receive text from client
                Log.i(TAG,"*** Message from server: " + res);
                gch.gameMessageHandler(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof InterruptedException) {
            	interrupt();
            }
        }finally{ // Closing all sockets.
        	try{
        		Log.i(TAG,"closing ServerListener....");
            	in.close();
            	s.close();
        	}catch(Exception e){}	            	
        }
	}
}
