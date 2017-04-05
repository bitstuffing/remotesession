package org.java.utils.remotesession;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.java.utils.remotesession.utils.Constants;
import org.java.utils.remotesession.utils.TextsUtils;

public class NotificationLauncher{
	
	private static Logger log = Logger.getLogger(Constants.LOG);
	
	private static int width;
	private static int height;
	
	private static ImageIcon headingIcon;
	
	private final static long DISPLAY_TIME = 3000;
	
	static{
		width = 300;
		height = 80;
		String path = "";
		try{
			URL url = Thread.currentThread().getContextClassLoader().getResource("mail.jpg");
			path = new File(url.toURI()).getAbsolutePath();
		}catch(Exception e){ log.warn(e.getMessage()); }
		headingIcon = new ImageIcon(path);
	}
	
	public static void showNotification(String title,String body){
		showNotification(title, body, DISPLAY_TIME);
	}
	
	public static void showNotification(String title,String body,long time) {
		log.debug(TextsUtils.getText("message.showingnotification")+": "+title+", "+body);
		final JFrame frame = new JFrame();
		frame.setSize(width,height);
		frame.setIconImage(headingIcon.getImage());
		buildFrame(frame,title,body);
		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();// size of the screen
		Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());// height of the task bar
		frame.setLocation(scrSize.width - frame.getWidth(), scrSize.height - toolHeight.bottom - frame.getHeight());
		new Thread(){
			@Override
		    public void run() {
				try {
					Thread.sleep(DISPLAY_TIME); // time after pop up will be disappeared.
					frame.dispose();
				} catch (InterruptedException e) {
					log.warn(e.getMessage());
			    }
		    };
		}.start();
	}

	private static void buildFrame(final JFrame frame,String messageTitle, String messageBody){
		String message = messageBody;
		String header = messageTitle;
		frame.setUndecorated(true);
		frame.setLayout(new GridBagLayout());
		final double random = Math.random();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0f;
		constraints.weighty = 1.0f;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.BOTH;
		JLabel headingLabel = new JLabel(header);
		headingLabel.setIcon(headingIcon); // --- use image icon you want to be as heading image.
		headingLabel.setOpaque(false);
		frame.add(headingLabel, constraints);
		constraints.gridx++;
		constraints.weightx = 0f;
		constraints.weighty = 0f;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.NORTH;
		JButton cloesButton = new JButton(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		cloesButton.setText("X");
		cloesButton.setMargin(new Insets(1, 4, 1, 4));
		cloesButton.setFocusable(false);
		frame.add(cloesButton, constraints);
		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weightx = 1.0f;
		constraints.weighty = 1.0f;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.BOTH;
		JLabel messageLabel = new JLabel("<HtMl>"+message);
		frame.add(messageLabel, constraints);
		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();// size of the screen
		Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());// height of the task bar
		frame.setLocation(scrSize.width - frame.getWidth(), scrSize.height - toolHeight.bottom - frame.getHeight());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
