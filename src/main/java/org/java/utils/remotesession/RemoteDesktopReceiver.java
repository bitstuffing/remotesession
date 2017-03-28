package org.java.utils.remotesession;

import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

public class RemoteDesktopReceiver extends JFrame{
	
	private static final int PORT = 2009;
	
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
					NotificationLauncher.showNotification("Waiting","Listening for a conection in port: "+PORT);
					SessionHandeler sessionHandler = new SessionHandeler(new ServerSocket(PORT),response);
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
				e.printStackTrace();
			}
			dispose();
		}
	}

	public static void main(String[] args) {
		new RemoteDesktopReceiver();
	}
}

