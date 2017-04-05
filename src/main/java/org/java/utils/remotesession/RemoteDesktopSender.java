package org.java.utils.remotesession;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.java.utils.remotesession.utils.ConnectionUtils;
import org.java.utils.remotesession.utils.Constants;
import org.java.utils.remotesession.utils.EncryptionUtils;
import org.java.utils.remotesession.utils.TextsUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoteDesktopSender extends JFrame{
	
	private Logger log = Logger.getLogger(Constants.LOG);
	
	private static Integer framesPerSecond;
	private static Robot robot;
	private JSlider jslider;
	private JTextField chatTextField;
	private JTextArea display;
	
	private ImageSender imageSender;
	private CommandHandler commandHandler;
	private String remoteAddress;
	private String key = Constants.PASSWD;
	
	private static float quality;
	private static boolean enableControl;
	private static boolean enableChat;
	
	static{
		quality = 0.7f;
		framesPerSecond = 5;
		enableControl = false;
		enableChat = false;
	}
	
	public String getKey(){
		return this.key;
	}
	
	public void setKey(String key){
		this.key = key;
	}

	public RemoteDesktopSender(String id) {
		String response = null;
		try {
			robot = new Robot();
			JSONObject jsonResponse = null;
			try {
				String sResponse = ConnectionUtils.get(Constants.HASTEBIN_RAW_PROVIDER+id);
				byte[] decryptedBytes = EncryptionUtils.decrypt(key, null, sResponse.getBytes());
				jsonResponse = new JSONObject(new String(decryptedBytes));
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
			String response2 = jsonResponse.getString("ip"); //local ipaddress (LAN)
			String password = jsonResponse.getString("password");
			String externalIp = jsonResponse.getString("externalIp"); //remote ipaddress (WAN)
			String localExternalIp = ConnectionUtils.getInternetIpAddress();
			if(localExternalIp!=null && !localExternalIp.equals(externalIp)){
				response2 = externalIp;
			}
			if(response2!=null && !response2.isEmpty()){
				try{
					Socket socket = new Socket(response2, Constants.REMOTE_PORT);
					remoteAddress = socket.getRemoteSocketAddress().toString();
					buildLocalJFramePanel();
					imageSender = new ImageSender(socket,robot,password);
					imageSender.start();
					Socket socket2 = new Socket(response2, Constants.REMOTE_PORT);
					commandHandler = new CommandHandler(socket2,robot,password,display);
					commandHandler.start();
					setDefaultCloseOperation(EXIT_ON_CLOSE);
					setVisible(true);
				} catch (UnknownHostException ex) {
					NotificationLauncher.showNotification("UnKnown Host Exception",response2);
				} catch (IOException ex) {
					NotificationLauncher.showNotification("IO Exception after unknown",response2);
				} 
			}else{
				NotificationLauncher.showNotification("Bad remote address",response2);
				setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				dispose();
			}
		} catch (AWTException e) {
			NotificationLauncher.showNotification("Robot exception",response);
		}
	}

	private void buildLocalJFramePanel() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//			UIManager.put("swing.boldMetal", Boolean.FALSE);
		}catch (Exception e1) {
			log.warn(e1.getMessage());
		}
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS));
		GridBagConstraints cc = new GridBagConstraints();
		GridBagLayout gridBagLayout = new GridBagLayout();
		JPanel panel = new JPanel();
		panel.setLayout(gridBagLayout);
		this.setSize(320, 400);
		Label qualityLabel = new Label(TextsUtils.getText("label.quality"));
		qualityLabel.setAlignment(FlowLayout.LEFT);
		cc.gridx = 0;
		cc.gridy = 0;
		panel.add(qualityLabel,cc);
		this.jslider = new JSlider(SwingConstants.HORIZONTAL, 0, 100,70);
		this.jslider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				quality = (float)((JSlider)e.getSource()).getValue()/100;
				imageSender.setQuality(quality);
			}
		});
//		cc = new GridBagConstraints();
		cc.gridx = 1;
		panel.add(this.jslider,cc);
		Label connectedSession = new Label(TextsUtils.getText("label.connectedto")+": ");
		connectedSession.setAlignment(FlowLayout.LEFT);
		cc.gridx = 0;
		cc.gridy = 1;
		panel.add(connectedSession,cc);
		JTextField textField = new JTextField(remoteAddress);
		textField.setPreferredSize(new Dimension(120,20));
		textField.setEditable(false);
		textField.setHorizontalAlignment(FlowLayout.LEFT);
		cc.gridx = 1;
		panel.add(textField,cc);
		Label framesLabel = new Label(TextsUtils.getText("label.fps"));
		framesLabel.setAlignment(FlowLayout.LEFT);
		cc.gridx = 0;
		cc.gridy = 2;
		panel.add(framesLabel,cc);
	    final JTextField jFormattedTextField = new JTextField(framesPerSecond.toString());
	    jFormattedTextField.addKeyListener(new KeyListener() {
	    	public void keyTyped(KeyEvent e) {
				if(jFormattedTextField.getText().length() < 2 && e.getKeyChar() >='0' && e.getKeyChar() <= '9'){
					try{
						Integer fps = Integer.parseInt((jFormattedTextField.getText()+e.getKeyChar()));
						framesPerSecond = fps; //update value
						imageSender.setFramesPerSecond(framesPerSecond);
					}catch(Exception ex){} finally {
						jFormattedTextField.setText(framesPerSecond.toString());
					}
				}else{
					e.consume();
				}
			}
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});
	    jFormattedTextField.setPreferredSize(new Dimension(60,20));
	    jFormattedTextField.setHorizontalAlignment(FlowLayout.LEFT);
	    cc.gridx = 1;
	    panel.add(jFormattedTextField,cc);
	    Label controlCheckboxLabel = new Label(TextsUtils.getText("label.enablecontrol"));
	    controlCheckboxLabel.setAlignment(FlowLayout.LEFT);
	    cc.gridx = 0;
	    cc.gridy = 3;
	    panel.add(controlCheckboxLabel,cc);
	    JCheckBox controlCheckbox = new JCheckBox();
	    controlCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableControl = ((JCheckBox)e.getSource()).isSelected();
				commandHandler.setEnableControl(enableControl);
			}
		});
	    controlCheckbox.setToolTipText(TextsUtils.getText("label.captureinputeventsfrom"));
	    controlCheckbox.setHorizontalAlignment(FlowLayout.LEFT);
	    cc.gridx = 1;
	    panel.add(controlCheckbox,cc);
	    Label chatCheckboxLabel = new Label("Enable chat");
	    chatCheckboxLabel.setAlignment(FlowLayout.LEFT);
	    cc.gridx = 0;
	    cc.gridy = 4;
	    panel.add(chatCheckboxLabel,cc);
	    JCheckBox chatCheckbox = new JCheckBox();
	    chatCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.info(TextsUtils.getText("message.click"));
				enableChat = !enableChat;
				chatTextField.setEditable(enableChat);
				chatTextField.setText("");
				try {
					Thread.sleep(250);
					commandHandler.sendCommand(enableChat?"enableChat":"disableChat","true".getBytes());
				} catch (Exception e1) {
					log.warn(e1.getMessage());
				}
				commandHandler.setEnableChat(enableChat);
			}
		});
	    controlCheckbox.setToolTipText(TextsUtils.getText("message.enableremotechat"));
	    controlCheckbox.setHorizontalAlignment(FlowLayout.LEFT);
	    cc.gridx = 1;
	    cc.gridy = 4;
	    panel.add(chatCheckbox,cc);
	    this.getContentPane().add(panel);
	    JPanel chatPanel = getChannelPanel();
	    this.getContentPane().add(chatPanel);
	    this.pack();
	}

	private JPanel getChannelPanel() {
		JPanel chatPanel = new JPanel();
	    chatPanel.setLayout(new BoxLayout(chatPanel,BoxLayout.Y_AXIS));
	    display = new JTextArea(16,16);
	    display.setEditable(false); 
	    JScrollPane scroll = new JScrollPane(display);
	    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    chatPanel.add(scroll);
	    chatTextField = new JTextField();
	    chatTextField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == '\n') {
					try{
						commandHandler.sendCommand("chatMessage",chatTextField.getText().getBytes());
						String displayedText = display.getText();
						if(displayedText.lastIndexOf("\n"+TextsUtils.getText("message.istyping"))==
								displayedText.length()-(("\n"+TextsUtils.getText("message.istyping")).length()+1)){
							displayedText = displayedText.substring(0,displayedText.lastIndexOf("\n"+TextsUtils.getText("message.istyping")));
						}
						display.setText(displayedText+"\n"+chatTextField.getText());
						chatTextField.setText("");
					}catch(Exception ex){
						log.warn(ex.getMessage());
					}
				}else{
					try {
						commandHandler.sendCommand("isTyping",chatTextField.getText().length()>0?"true".getBytes():"false".getBytes());
					} catch (Exception e1) {
						log.warn(e1.getMessage());
					}
				}
			}
			public void keyReleased(KeyEvent e) { }
			public void keyPressed(KeyEvent e) { }
		});
	    JScrollPane scroll2 = new JScrollPane(chatTextField);
	    scroll2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    chatPanel.add(scroll2);
	    return chatPanel;
	}

}