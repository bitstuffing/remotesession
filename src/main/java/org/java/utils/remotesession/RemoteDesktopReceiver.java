package org.java.utils.remotesession;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.java.utils.remotesession.utils.ConnectionUtils;
import org.java.utils.remotesession.utils.Constants;
import org.java.utils.remotesession.utils.EncryptionUtils;
import org.java.utils.remotesession.utils.TextsUtils;
import org.json.JSONObject;

public class RemoteDesktopReceiver extends JFrame{
	
	private String sessionId = "";
	
	public String getSessionId(){
		return this.sessionId;
	}
	
	private Logger log = Logger.getLogger(Constants.LOG);
	
	public RemoteDesktopReceiver(final String response) {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		new Thread(){ //needs to be in a thread
			public void run() {
				try {
					do {
						if(!SessionHandeler.isWorking()){ //creates new one
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("password", response);
							jsonObject.put("ip", ConnectionUtils.getLocalIpAddress());
							jsonObject.put("externalIp",ConnectionUtils.getInternetIpAddress());
							String sResponse = null;
							NotificationLauncher.showNotification(TextsUtils.getText("message.creatingsession"),TextsUtils.getText("message.pleasewaitforasession"));
							try {
								byte[] content = EncryptionUtils.encrypt(Constants.PASSWD, null, jsonObject.toString().getBytes());
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
							NotificationLauncher.showNotification(TextsUtils.getText("message.waiting"),TextsUtils.getText("message.sessionis")+": "+sResponse+TextsUtils.getText("message.andhasbeencopiedtoclipboard")+" \n"+TextsUtils.getText("message.listeningforaconnectionatport")+": "+Constants.REMOTE_PORT);
							sessionId = sResponse;
							SessionHandeler sessionHandler = new SessionHandeler(new ServerSocket(Constants.REMOTE_PORT),response);
							sessionHandler.chatjframe = new JFrame();
							sessionHandler.chatjframe.add(sessionHandler.getChannelPanel());
							sessionHandler.chatjframe.pack();
							sessionHandler.chatjframe.setVisible(true);
							sessionHandler.start();
						}
						try {
							Thread.sleep(1000); //check time
						} catch (InterruptedException e) {
							NotificationLauncher.showNotification(TextsUtils.getText("message.connectionreset"),TextsUtils.getText("message.waitingforareconection"));
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
		}.start();
	}

}

