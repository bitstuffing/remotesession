package org.java.utils.remotesession;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

public class RemoteDesktopReceiver extends JFrame{
	
	private static final int PORT = 2009;
	
	public RemoteDesktopReceiver() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		try {
			do {
				if(!ConnectionHandeler.isWorking()){ //creates new one
					String response = JOptionPane.showInputDialog("Write session password:");
					response = (response==null || response.isEmpty())?ConnectionHandeler.getKey():response;
					for(;(response.length())%16!=0;){
						response+="p";
					}
					ConnectionHandeler.setKey(response);
					NotificationLauncher.showNotification("Waiting","Listening for a conection in port: "+PORT);
					ConnectionHandeler.chatjframe = new JFrame();
//					ConnectionHandeler.chatjframe.setLayout(new BoxLayout(ConnectionHandeler.chatjframe,BoxLayout.Y_AXIS));
					ConnectionHandeler.chatjframe.add(ConnectionHandeler.getChannelPanel());
					ConnectionHandeler.chatjframe.pack();
					ConnectionHandeler.chatjframe.setVisible(true);
					Socket socket = new ServerSocket(PORT).accept();
					new ConnectionHandeler(socket).start();
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

