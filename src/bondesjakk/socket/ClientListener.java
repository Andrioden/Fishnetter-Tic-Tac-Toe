package bondesjakk.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import bondesjakk.activities.HostGameActivity;
import bondesjakk.game.GameComHandler;

import android.util.Log;

public class ClientListener extends Thread{
	private final static String TAG = "ClientListener";
	private HostGameActivity activity;
	private Socket s;
	private GameComHandler gch;
	
	public ClientListener(Socket s, HostGameActivity activity) {
		this.activity = activity;
		this.s = s;
		this.gch = new GameComHandler(activity);
	}
	
	@Override
	public void run() {
		BufferedReader in 	= null;
		try{
			in = new BufferedReader(new InputStreamReader(s.getInputStream())); 
            while (!isInterrupted()) {
                String res = in.readLine().trim();//receive text from client
                activity.outComStrings.add(res);
                Log.i(TAG,"*** Message from client: " + res);
                gch.gameMessageHandler(res);
            }   
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof InterruptedException) {
            	interrupt();
            }
        }finally{ // Closing all sockets.
        	try{
            	in.close();
            	s.close();
        	}catch(Exception e){}	            	
        }
	}
}
