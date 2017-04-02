package org.java.utils.remotesession;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.java.utils.remotesession.utils.ConnectionUtils;
import org.java.utils.remotesession.utils.Constants;
import org.java.utils.remotesession.utils.EncryptionUtils;
import org.json.JSONObject;

public class RemoteDesktopReceiver extends JFrame{
	
	private Logger log = Logger.getLogger(Constants.LOG);
	
	public RemoteDesktopReceiver() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		try {
			do {
				if(!SessionHandeler.isWorking()){ //creates new one
					String response = JOptionPane.showInputDialog("Write session password:");
					response = (response==null || response.isEmpty())?"orangeisnotblack":response;
					for(;(response.length())%16!=0;){
						response+="p";
					}
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("password", response);
					jsonObject.put("ip", ConnectionUtils.getLocalIpAddress());
					jsonObject.put("externalIp",ConnectionUtils.getInternetIpAddress());
					String sResponse = null;
					NotificationLauncher.showNotification("Creating session","Please wait for a session...");
					try {
						byte[] content = EncryptionUtils.encrypt("orangeisnotblack", null, jsonObject.toString().getBytes());
						sResponse = ConnectionUtils.post(Constants.HASTEBIN_PROVIDER_SUBMIT,new String(content));
						JSONObject responseJSON = new JSONObject(sResponse);
						sResponse = responseJSON.getString("key");
					} catch (Exception e) {
						log.warn(e.getMessage());
					}
					if(sResponse!=null){
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						StringSelection data = new StringSelection(sResponse);
						clipboard.setContents(data, data);
					}
					NotificationLauncher.showNotification("Waiting","Session is: "+sResponse+" and has been copied to clipboard.\nListening for a conection at default port: "+Constants.REMOTE_PORT);
					SessionHandeler sessionHandler = new SessionHandeler(new ServerSocket(Constants.REMOTE_PORT),response);
					sessionHandler.chatjframe = new JFrame();
//					ConnectionHandeler.chatjframe.setLayout(new BoxLayout(ConnectionHandeler.chatjframe,BoxLayout.Y_AXIS));
					sessionHandler.chatjframe.add(sessionHandler.getChannelPanel());
					sessionHandler.chatjframe.pack();
					sessionHandler.chatjframe.setVisible(true);
					sessionHandler.start();
				}
				try {
					Thread.sleep(1000); //check time
				} catch (InterruptedException e) {
					NotificationLauncher.showNotification("Connection reset","Waiting to a reconection...");
				}
			}while (true);
		} catch (IOException ex) {
			NotificationLauncher.showNotification("IOException",ex.getMessage());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				log.warn(e.getMessage());
			}
			dispose();
		}
	}

	public static void main(String[] args) {
		new RemoteDesktopReceiver();
	}
}

