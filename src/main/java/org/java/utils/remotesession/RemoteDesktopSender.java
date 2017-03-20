package org.java.utils.remotesession;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
import org.java.utils.remotesession.utils.EncryptionUtils;
import org.java.utils.remotesession.utils.ImageUtils;
import org.json.JSONObject;

public class RemoteDesktopSender extends JFrame{
	
	private static final int REMOTE_PORT = 2009;
	private static Integer framesPerSecond;
	private Socket socket;
	private static Robot robot;
	private JSlider jslider;
	private JTextField chatTextField;
	private Sender sender;
	private static String key;
	private static float quality;
	private static boolean enableControl;
	private static boolean enableChat;
	
	static{
		key = "orangeisnotblack";
		quality = 0.7f;
		framesPerSecond = 5;
		enableControl = false;
		enableChat = false;
	}
	
	public static String getKey(){
		return RemoteDesktopSender.key;
	}
	
	public static void setKey(String key){
		RemoteDesktopSender.key = key;
	}

	public RemoteDesktopSender() {
		String response = null;
		try {
			robot = new Robot();
			String password = JOptionPane.showInputDialog("Write session password:");
			password = (password==null || password.isEmpty())?RemoteDesktopSender.getKey():password;
			for(;(password.length())%16!=0;){
				password+="p";
			}
			RemoteDesktopSender.setKey(password);
			final String response2 = JOptionPane.showInputDialog("Write remote address (needs to be waiting for):");
			if(response2!=null && !response2.isEmpty()){
				try{
					socket = new Socket(response2, REMOTE_PORT);
					buildLocalJFramePanel();
					sender = new Sender();
					new Receiver();
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
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			UIManager.put("swing.boldMetal", Boolean.FALSE);
		}catch (Exception e1) {
			e1.printStackTrace();
		}
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS));
		GridBagConstraints cc = new GridBagConstraints();
		GridBagLayout gridBagLayout = new GridBagLayout();
		JPanel panel = new JPanel();
		panel.setLayout(gridBagLayout);
		this.setSize(320, 400);
		Label qualityLabel = new Label("Quality");
		qualityLabel.setAlignment(FlowLayout.LEFT);
		cc.gridx = 0;
		cc.gridy = 0;
		panel.add(qualityLabel,cc);
		this.jslider = new JSlider(SwingConstants.HORIZONTAL, 0, 100,70);
		this.jslider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				quality = (float)((JSlider)e.getSource()).getValue()/100;
			}
		});
//		cc = new GridBagConstraints();
		cc.gridx = 1;
		panel.add(this.jslider,cc);
		Label connectedSession = new Label("Connected to: ");
		connectedSession.setAlignment(FlowLayout.LEFT);
		cc.gridx = 0;
		cc.gridy = 1;
		panel.add(connectedSession,cc);
		JTextField textField = new JTextField(socket.getRemoteSocketAddress().toString());
		textField.setPreferredSize(new Dimension(120,20));
		textField.setEditable(false);
		textField.setHorizontalAlignment(FlowLayout.LEFT);
		cc.gridx = 1;
		panel.add(textField,cc);
		Label framesLabel = new Label("Frames per second:");
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
	    Label controlCheckboxLabel = new Label("Enable control");
	    controlCheckboxLabel.setAlignment(FlowLayout.LEFT);
	    cc.gridx = 0;
	    cc.gridy = 3;
	    panel.add(controlCheckboxLabel,cc);
	    JCheckBox controlCheckbox = new JCheckBox();
	    controlCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableControl = ((JCheckBox)e.getSource()).isSelected();
			}
		});
	    controlCheckbox.setToolTipText("Capture input events from mouse and keyboard");
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
				System.out.println("click!");
				enableChat = !enableChat;
				chatTextField.setEditable(enableChat);
				chatTextField.setText("");
				try {
					Thread.sleep(250);
					sender.sendCommand(enableChat?"enableChat":"disableChat","true".getBytes());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	    controlCheckbox.setToolTipText("Enable remote chat with receiver");
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
	    final JTextArea display = new JTextArea(16,16);
	    display.setEditable(false); 
	    JScrollPane scroll = new JScrollPane(display);
	    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    chatPanel.add(scroll);
	    chatTextField = new JTextField();
	    chatTextField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					try{
						sender.sendCommand("chatMessage",chatTextField.getText().getBytes());
						display.setText(display.getText()+"\n"+chatTextField.getText());
						chatTextField.setText("");
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}else{
					try {
						sender.sendCommand("isTyping",chatTextField.getText().length()>0?"true".getBytes():"false".getBytes());
					} catch (Exception e1) {
						e1.printStackTrace();
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

	public static void main(String[] args) {
		new RemoteDesktopSender();
	}

	class Sender extends Thread {

		private boolean wait = false;

		public Sender() {
			start();
		}
		
		public void sendCommand(String key, byte[] bytes) throws Exception {
			if(!wait){ //TODO, change to while(wait) Thread.sleep(20); or a latence
				wait  = true;
				Runtime runtime = Runtime.getRuntime();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(key, Base64.encodeBase64String(bytes));
				byte[] bytesEncrypted = EncryptionUtils.encrypt(RemoteDesktopSender.key, null, jsonObject.toString().getBytes());
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				oos.reset();
				oos.writeObject(bytesEncrypted);
				oos.flush();
				oos = null;
				bytesEncrypted = null;
				runtime.gc();
				wait = false;
			}
		}

		public void run() {
			Runtime runtime = Runtime.getRuntime();
			BufferedImage screenShot = null;
			byte[] bytes = null;
			while (true) {
				try {
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					screenShot = ImageUtils.compress(robot.createScreenCapture(new Rectangle((screenSize))),quality); //compress quality to 70%
					screenShot.flush();
					bytes = ImageUtils.getBytes(screenShot, 1.0f); //convert to bytes with 100% quality (before was 70% compressed)
					sendCommand("image", bytes);
					Thread.sleep(1000/framesPerSecond);
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					screenShot = null;
					bytes = null;
					runtime.gc();
				}
			}
		}
	}
	
	class Receiver extends Thread {
		
		public Receiver() {
			start();
		}
		
		public void run() {
			Runtime runtime = Runtime.getRuntime();
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(socket.getInputStream());
				while (true) {
					try{
						Object object = ois.readObject();
						if((enableControl || enableChat) && object!=null && object instanceof String){
							JSONObject json = null;
							try{
								String content = new String(EncryptionUtils.decrypt(RemoteDesktopSender.key, null, ((String)object).getBytes()),"UTF-8");
								json = new JSONObject(content);
								if(enableControl){
									if(json.has("x") && json.has("y")){
										robot.mouseMove(json.getInt("x"), json.getInt("y"));
									}
									if(json.has("mousePress")){
										robot.mousePress(json.getInt("mousePress")); //InputEvent.BUTTONX_MASK
									}else if(json.has("mouseRelease")){
										robot.mouseRelease(json.getInt("mouseRelease"));
									}else if(json.has("keyPress")){
										robot.keyPress(json.getInt("keyPress")); //keycode
									}else if(json.has("keyRelease")){
										robot.keyRelease(json.getInt("keyPress"));
									}else if(json.has("mouseWheel")){
										robot.mouseWheel(json.getInt("mouseWheel")); //rotation
									}
								}
								if(enableChat){
									if(json.has("isTyping")){
										
									}
									if(json.has("textContent")){
										
									}
								}
//								System.out.println(json.toString());
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}catch(Exception exc){
						System.out.println("Exception trying to extract message!");
					}finally{
						runtime.gc();
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}