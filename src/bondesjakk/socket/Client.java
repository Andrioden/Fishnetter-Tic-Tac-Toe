package bondesjakk.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;  
import java.net.Socket; 
import java.net.SocketException;

import bondesjakk.activities.JoinGameActivity;
import android.util.Log;   
  
public class Client extends Thread {   
	private final static String TAG = "Client";
	private final static int PORT = 12345;
	private final static int refreshRate = 300; //ms
	private String IP = "10.14.27.4";
	private JoinGameActivity activity;
	private String name;
	
	
	public Client(JoinGameActivity activity, String name, String ip) {
		this.activity = activity;
		this.name = name;
		this.IP = ip;
	}
    
	public void run() {
    	Socket s 			= null;
    	PrintWriter out		= null;
    	BufferedReader in 	= null;
    	ServerListener listener = null;
    	
        try {
        	s = new Socket(IP, PORT);
        	activity.connectedHandler();
            Log.i(TAG, "C: Connected to server " + s.toString());
            out = new PrintWriter(s.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String readFirstCom = in.readLine().trim();
            if (readFirstCom.equals("STARTED")) { 
            	// Game started, stop join sequence.
            	activity.gameStartedHandler();
            }
            else {
            	// Continue join sequence.
                Log.i(TAG, "Recieved netID: "+readFirstCom);
                activity.yourNetId = Integer.parseInt(readFirstCom);
                out.println("JOIN,"+name+","+readFirstCom);
                listener = new ServerListener(s,activity);
                listener.start();
            }
            // Prints out messages to the host
            while (!isInterrupted()) {
            	if (activity.outComStrings.size()>0) {
                	String nextComString = activity.outComStrings.get(0);
                	out.println(nextComString);
                	Log.i(TAG, "Message sendt to server: "+nextComString);
            		activity.outComStrings.remove(0);
            	}
            	pause(refreshRate);
            }
        } catch (SocketException e) {
        	Log.i(TAG,"SocketException, unable to connect to host.");
        	e.printStackTrace();
    		activity.timedOutHandler();
    	} catch (IOException e) {
            e.printStackTrace();
		} finally{ // Closing all sockets.
			if (activity.yourNetId != 0) {
				out.println("CLIENT_QUIT,"+activity.yourNetId);
				Log.i(TAG, "Message sendt to server: CLIENT_QUIT,"+activity.yourNetId);
			}
			Log.i(TAG,"closing client....");
        	try{
        		in.close();
	        	out.close();
	        	s.close();
        		listener.interrupt();
        	} catch(Exception e){}
        }
    }
	
	private void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			interrupt();
		}
	}
}