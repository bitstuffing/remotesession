package org.java.utils.remotesession.events;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java.utils.remotesession.RemoteDesktopReceiver;
import org.java.utils.remotesession.utils.Constants;
import org.java.utils.remotesession.utils.TextsUtils;

public class AllowActionListener implements ActionListener {
	
	protected static final int WAITING_REFRESH = 200;

	private static Logger log = LogManager.getLogger(Constants.LOG);

	private RemoteDesktopReceiver remoteDesktopReceiver = null;
	private Label statusLabel = null;
	private Label mainActionLabel = null;
	
	public AllowActionListener(Label statusLabel, Label mainActionLabel) {
		this.statusLabel = statusLabel;
		this.mainActionLabel = mainActionLabel;
	}

	public void actionPerformed(ActionEvent e) {
		((JButton)e.getSource()).setEnabled(false);
		String response = JOptionPane.showInputDialog(TextsUtils.getText("message.writeyoursessionpassword"));
		response = (response==null || response.isEmpty())?Constants.PASSWD:response;
		for(;(response.length())%16!=0;){
			response+=Constants.PASSWD_CHAR;
		}
		final String finalResponse = response;
		new Thread(){
			public void run() {
				statusLabel.setText(TextsUtils.getText("message.waitingforasession"));
				remoteDesktopReceiver = new RemoteDesktopReceiver(finalResponse);
			}
		}.start();
		
		new Thread(){
			public void run() {
				log.debug(TextsUtils.getText("message.checkingstatus"));
				while(remoteDesktopReceiver == null || remoteDesktopReceiver.getSessionId() == null || remoteDesktopReceiver.getSessionId().isEmpty()){
					log.debug(TextsUtils.getText("message.waitingforstatus"));
					try {
						Thread.sleep(WAITING_REFRESH);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				statusLabel.setText(TextsUtils.getText("message.yourlocalsessionidis")+remoteDesktopReceiver.getSessionId());
				mainActionLabel.setText(remoteDesktopReceiver.getSessionId()); //put id in the preview label button
				log.debug(TextsUtils.getText("message.yourlocalsessionidis")+remoteDesktopReceiver.getSessionId());
				statusLabel.repaint();
				mainActionLabel.repaint();
			};
		}.start();
		
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException ex) {
//			log.warn(ex.getMessage());
//		}
//		((JFrame)SwingUtilities.getWindowAncestor(button)).dispose();
	}

}
