package org.java.utils.remotesession;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.codec.binary.Base64;
import org.java.utils.remotesession.panel.ImagePanel;
import org.java.utils.remotesession.utils.ImageUtils;
import org.json.JSONObject;

public class SessionHandeler extends Thread {

	private JFrame frame;
	private String cached = ""; //image cached
	public JFrame chatjframe;
	private static JTextArea display;
	private static boolean working;
	
	private LocalConnectionHandler imageHandler;
	public LocalConnectionHandler messagesHandler;
	private ImagePanel imagePanel;
	
	static{
		working = false;
	}

	public SessionHandeler(ServerSocket serverSocket,String key) {
		frame = new JFrame();
		try {
			Socket s = serverSocket.accept();
//			System.out.println("imageHandler :"+s.getPort());
			imageHandler = new LocalConnectionHandler(s,key);
			Socket s2 = serverSocket.accept();
//			System.out.println("messagesHandler :"+s2.getPort());
			messagesHandler = new LocalConnectionHandler(s2,key);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Launching a reset by exception...");
			new SessionHandeler(serverSocket,key);
		} 
	}
	
	public JPanel getChannelPanel() {
		JPanel chatPanel = new JPanel();
	    chatPanel.setLayout(new BoxLayout(chatPanel,BoxLayout.Y_AXIS));
	    display = new JTextArea(16,16);
	    display.setEditable(false); 
	    JScrollPane scroll = new JScrollPane(display);
	    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    chatPanel.add(scroll);
	    final JTextField chatTextField = new JTextField();
	    chatTextField.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar()=='\n'){
					String text = chatTextField.getText();
					messagesHandler.sendCommand("chatMessage", text.getBytes());
					chatTextField.setText("");
					display.setText(display.getText()+"\n"+text);
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyPressed(KeyEvent e) {}
		});
	    JScrollPane scroll2 = new JScrollPane(chatTextField);
	    scroll2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    chatPanel.add(scroll2);
	    return chatPanel;
	}
	

	public void run() {
		working = true;
		ImageIcon img = null;
		Image image = null;
		JSONObject jsonObject = null;
		try {
			jsonObject = imageHandler.receiveCommand();
			runJSONCommand(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			jsonObject = null;
		}
		image = (Image) ImageUtils.read(Base64.decodeBase64(cached));
//		image = (Image) ImageUtils.read(bytes);
		img = new ImageIcon(image);
		imagePanel = new ImagePanel(image);
		imagePanel.addMouseListener(new MouseAdapter(){
			
			public void mousePressed(MouseEvent e) {
				JSONObject json = new JSONObject();
				json.put("x",e.getPoint().getX());
				json.put("y",e.getPoint().getY());
				int mouseButtonPressed = getMouseButtonPressed(e);
				json.put("mousePress", mouseButtonPressed);
				try {
					messagesHandler.writeJSONToOutputStream(json);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			public void mouseReleased(MouseEvent ev) {
				JSONObject json = new JSONObject();
				json.put("x",ev.getPoint().getX());
				json.put("y",ev.getPoint().getY());
				int mouseButtonPressed = getMouseButtonPressed(ev);
				json.put("mouseRelease", mouseButtonPressed);
				try {
					messagesHandler.writeJSONToOutputStream(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
            public void mouseEntered(MouseEvent ev) {
//				JSONObject json = new JSONObject();
//				json.put("x",ev.getPoint().getX());
//				json.put("y",ev.getPoint().getY());
//				writeJSONToOutputStream(json);
            }
            public void mouseExited(MouseEvent ev) {
//            	JSONObject json = new JSONObject();
//				json.put("x",ev.getPoint().getX());
//				json.put("y",ev.getPoint().getY());
//				writeJSONToOutputStream(json);
            }

			private int getMouseButtonPressed(MouseEvent ev) {
            	int mouseButtonPressed = MouseEvent.BUTTON1;
 				if ((ev.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
 					mouseButtonPressed = MouseEvent.BUTTON1_MASK;
 				}else if ((ev.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
 					mouseButtonPressed = MouseEvent.BUTTON1_DOWN_MASK;
 				}else if ((ev.getModifiers() & MouseEvent.BUTTON2_MASK) != 0) {
 					mouseButtonPressed = MouseEvent.BUTTON2_MASK;
 				}else if ((ev.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
 					mouseButtonPressed = MouseEvent.BUTTON2_DOWN_MASK;
 				}else if ((ev.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
 					mouseButtonPressed = MouseEvent.BUTTON3_MASK;
 				}else if ((ev.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
 					mouseButtonPressed = MouseEvent.BUTTON3_DOWN_MASK;
 				}
 				return mouseButtonPressed;
			}
		});
		imagePanel.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent eve) {
				JSONObject json = new JSONObject();
				json.put("x",eve.getPoint().getX());
				json.put("y",eve.getPoint().getY());
				try {
					messagesHandler.writeJSONToOutputStream(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			public void mouseDragged(MouseEvent e) { }
		});
		frame.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				
			}
			public void keyReleased(KeyEvent e) {
				JSONObject json = new JSONObject();
				json.put("keyRelease",e.getKeyCode());
				try {
					messagesHandler.writeJSONToOutputStream(json);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			public void keyPressed(KeyEvent ev) {
				JSONObject json = new JSONObject();
				json.put("keyPress",ev.getKeyCode());
				try {
					messagesHandler.writeJSONToOutputStream(json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		frame.add(imagePanel, BorderLayout.CENTER);
		frame.setSize(img.getIconWidth(),img.getIconHeight());
		frame.setVisible(true);
		new Thread(){
			public void run() {
				Image image = null;
//				ImagePanel imagePanel = null;
				JSONObject jsonObject = null;
				Runtime runtime = Runtime.getRuntime(); //gc
				do{
					try {
						jsonObject = imageHandler.receiveCommand();
						runJSONCommand(jsonObject);
						image = (Image) ImageUtils.read(Base64.decodeBase64(cached));
						imagePanel.setImage(image);
						imagePanel.repaint();
					}catch (Exception ex) {
						ex.printStackTrace();
						working = false;
					}finally{
						image = null;
						jsonObject = null;
						runtime.gc();
					}
				}while(working);
			};
		}.start();
		
		new Thread(){
			public void run() {
				JSONObject jsonObject = null;
				Runtime runtime = Runtime.getRuntime(); //gc
				do{
					try {
						jsonObject = messagesHandler.receiveCommand();
						runJSONCommand(jsonObject);
					}catch (Exception ex) {
						ex.printStackTrace();
						working = false;
					}finally{
						jsonObject = null;
						runtime.gc();
					}
				}while(working);
			};
		}.start();
	}
	
	private void runJSONCommand(JSONObject jsonObject) {
		if(jsonObject.has("image")){
			cached = jsonObject.getString("image");
		}else if(jsonObject.has("chatMessage")){
			System.out.println("chatMessage");
			String content = display.getText()+"\n";
			try {
				String message = new String(Base64.decodeBase64(jsonObject.getString("chatMessage")),"UTF-8");
				System.out.println("Message is: "+message);
				content+=message;
			} catch (Exception e) {
				e.printStackTrace();
			} 
			if(chatjframe!=null){
				display.setText(content);
			}
		}else if(jsonObject.has("enableChat")){
			System.out.println("enableChat");
			if(chatjframe!=null){
				chatjframe.setEnabled(true);
				chatjframe.pack();
				chatjframe.setVisible(true);
			}
		}else if(jsonObject.has("disableChat")){
			System.out.println("disableChat");
			if(chatjframe!=null){
//				chatjframe.dispose();
				chatjframe.setVisible(false);
			}
		}else if(jsonObject.has("isTyping")){
			try {
				String message = new String(Base64.decodeBase64(jsonObject.getString("isTyping")),"UTF-8");
				message = message.equals("true")?"\nIs typing...":"";
				String oldText = display.getText();
				if(oldText.endsWith("\nIs typing...")){
					oldText = oldText.substring(0,oldText.lastIndexOf("\nIs typing..."));
				}
				display.setText(oldText+message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
//			System.out.println(jsonObject.toString());
		}
	}

	public static boolean isWorking(){
		return working;
	}
}
