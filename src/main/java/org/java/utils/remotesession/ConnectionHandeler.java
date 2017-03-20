package org.java.utils.remotesession;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
import org.java.utils.remotesession.utils.EncryptionUtils;
import org.java.utils.remotesession.utils.ImageUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class ConnectionHandeler extends Thread {

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private JFrame frame;
	private InputStream inputStream;
	private OutputStream outputStream;
	private String cached;
	public static JFrame chatjframe;
	private static JTextArea display;
	private static boolean working;
	private static String key;
	
	static{
		working = false;
		key = "orangeisnotblack";
	}

	public static void setKey(String key) {
		ConnectionHandeler.key = key;
	}
	
	public static String getKey(){
		return ConnectionHandeler.key;
	}

	public ConnectionHandeler(Socket s) {
		frame = new JFrame();
		try {
			inputStream = s.getInputStream();
			outputStream = s.getOutputStream();
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}
	
	private void writeJSONToOutputStream(JSONObject json) {
		try { 
			oos.reset();
			oos.writeObject(new String(EncryptionUtils.encrypt(ConnectionHandeler.key, null, json.toString().getBytes()),"UTF-8"));
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static JPanel getChannelPanel() {
		JPanel chatPanel = new JPanel();
	    chatPanel.setLayout(new BoxLayout(chatPanel,BoxLayout.Y_AXIS));
	    display = new JTextArea(16,16);
	    display.setEditable(false); 
	    JScrollPane scroll = new JScrollPane(display);
	    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    chatPanel.add(scroll);
	    JTextField chatTextField = new JTextField();
	    JScrollPane scroll2 = new JScrollPane(chatTextField);
	    scroll2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    chatPanel.add(scroll2);
	    return chatPanel;
	}
	

	public void run() {
		working = true;
		Runtime runtime = Runtime.getRuntime(); //gc
		ImageIcon img = null;
		Image image = null;
		ImagePanel imagePanel = null;
		JSONObject jsonObject = null;
		try {
			ois = new ObjectInputStream(inputStream);
			oos = new ObjectOutputStream(outputStream);
			byte[] bytes = (byte[])ois.readObject();
			byte[] decryptedImageBytes = EncryptionUtils.decrypt(ConnectionHandeler.key, null, bytes);
			jsonObject = new JSONObject(new String(decryptedImageBytes,"UTF-8"));
			runJSONCommand(jsonObject);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				writeJSONToOutputStream(json);
			}
			public void mouseReleased(MouseEvent ev) {
				JSONObject json = new JSONObject();
				json.put("x",ev.getPoint().getX());
				json.put("y",ev.getPoint().getY());
				int mouseButtonPressed = getMouseButtonPressed(ev);
				json.put("mouseRelease", mouseButtonPressed);
				writeJSONToOutputStream(json);
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
				writeJSONToOutputStream(json);
			}
			public void mouseDragged(MouseEvent e) { }
		});
		frame.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				
			}
			public void keyReleased(KeyEvent e) {
				JSONObject json = new JSONObject();
				json.put("keyRelease",e.getKeyCode());
				writeJSONToOutputStream(json);
			}
			public void keyPressed(KeyEvent ev) {
				JSONObject json = new JSONObject();
				json.put("keyPress",ev.getKeyCode());
				writeJSONToOutputStream(json);
			}
		});
		frame.add(imagePanel, BorderLayout.CENTER);
		frame.setSize(img.getIconWidth(),img.getIconHeight());
		frame.setVisible(true);
		do{
			try {
				ois = new ObjectInputStream(inputStream);
				byte[] bytes = (byte[])ois.readObject();
				byte[] decryptedImageBytes = EncryptionUtils.decrypt(ConnectionHandeler.key, null, bytes);
				jsonObject = new JSONObject(new String(decryptedImageBytes,"UTF-8"));
				runJSONCommand(jsonObject);
				image = (Image) ImageUtils.read(Base64.decodeBase64(cached));
				imagePanel.setImage(image);
				imagePanel.repaint();
			}catch(JSONException jex){
				jex.printStackTrace();
			}catch(java.io.StreamCorruptedException cex){
				cex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
				working = false;
			}finally{
				img = null;
				image = null;
				jsonObject = null;
				runtime.gc();
			}
		}while(working);
	}
	
	private void runJSONCommand(JSONObject jsonObject) {
		if(jsonObject.has("image")){
			cached = jsonObject.getString("image");
		}else if(jsonObject.has("enableChat")){
			System.out.println("enableChat");
			if(chatjframe!=null){
				chatjframe.pack();
				chatjframe.setVisible(true);
			}
		}else if(jsonObject.has("chatMessage")){
			System.out.println("chatMessage");
			String content = display.getText()+"\n";
			try {
				content+=new String(Base64.decodeBase64(jsonObject.getString("chatMessage")),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(chatjframe!=null)
				display.setText(content);
		}else if(jsonObject.has("disableChat")){
			System.out.println("disableChat");
			if(chatjframe!=null)
				chatjframe.dispose();
		}else if(jsonObject.has("isTyping")){
			try {
				display.setText(new String(Base64.decodeBase64(jsonObject.getString("isTyping")),"UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			System.out.println(jsonObject.toString());
		}
	}

	public static boolean isWorking(){
		return working;
	}
}
