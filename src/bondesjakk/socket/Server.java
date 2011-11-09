package bondesjakk.socket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;

import bondesjakk.activities.HostGameActivity;

import android.util.Log;

public class Server extends Thread{
	private final static String TAG = "Server";
	private final static int PORT = 12345;
	private final static int SERVER_TIMEOUT = 8000; //ms
	private final static int HOST_RETRY_TIMER = 3000; //ms
	private final static int MAX_HOST_ATTEMPTS = 5;
	private HostGameActivity activity;
	
	public Server(HostGameActivity activity) {
		this.activity = activity;
	}
	
	public void run() {
		ServerSocket ss 	= null;
		PrintWriter out 	= null;
		ArrayList<ClientListener> listeners = new ArrayList<ClientListener>();
		ArrayList<ClientSender> senders = new ArrayList<ClientSender>();
		
		try{
    		Log.i(TAG,"Attempting to start server....");
    		int hostAttempts = 1;
    		// Attempts to create the server several times
    		while ((hostAttempts<=MAX_HOST_ATTEMPTS)&&(ss==null)) {
	            try {
					ss = new ServerSocket(PORT);
				} catch (BindException e) {
					Log.i(TAG, "Host attempt failed("+hostAttempts+")");
					hostAttempts++;
					pause(HOST_RETRY_TIMER);
					e.printStackTrace();
				}
    		}
    		if (ss==null) {
    			Log.i(TAG, "Did not manage to host game.");
    			activity.hostingFail();
    		}
    		else {
    			activity.hostingSuccessful();
	            ss.setSoTimeout(SERVER_TIMEOUT); // Now the thread can be interrupted.
	            activity.setHostIP(getLocalIpAddress());
	            Log.i(TAG,"Serversocket created! Waiting for clients....");
	            while (!isInterrupted()) {
	            	Socket s = null;
	            	try {
	            		s = ss.accept();
	            	} catch (SocketTimeoutException e) {
	            		Log.i(TAG, "Accept timeout.. restarting for interrupt check...");
	            	}
	            	if ((!activity.isGameStarted)&&(s!=null)) {
		            	// To make sure the id hand outs are synchronized
	            		out = new PrintWriter(s.getOutputStream(), true);
		            	int clientId = activity.game.getNextPlayerId();
		            	out.println(clientId);
		            	Log.i(TAG, "client connected, given netID: "+clientId);
		            	listeners.add(new ClientListener(s,activity));
		            	senders.add(new ClientSender(s,activity));
		            	listeners.get(listeners.size()-1).start();
		            	senders.get(senders.size()-1).start();
	            	}
	            	else if (s!=null) {
	            		out = new PrintWriter(s.getOutputStream(), true);
	            		out.println("STARTED");
	            	}
	            }
    		}
        } catch (IOException e) {
            e.printStackTrace();
            interrupt();
        }finally{ // Closing all sockets.
        	activity.outComStrings.add("HOST_QUIT,1");
        	pause(1000); // Pausing the thread, giving the clientSenders time to send the coms
        	Log.i(TAG,"closing server....");
        	try{
        		for (ClientListener listener : listeners) {
        			listener.interrupt();
        		}
        		for (ClientSender sender : senders) {
        			sender.interrupt();
        		}
        		if (out!=null) {
        			out.close();
        		}
        		Log.i(TAG,"Freeing adress....");
        		ss.setReuseAddress(true);
        		ss.close();
        	}catch(Exception e){
        		e.printStackTrace();
        	}	            	
        }   
	}
	
	private void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}
	
	private String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
}
