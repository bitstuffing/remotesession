package org.java.utils.remotesession;

import java.awt.Robot;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JTextArea;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.java.utils.remotesession.utils.Constants;
import org.java.utils.remotesession.utils.EncryptionUtils;
import org.java.utils.remotesession.utils.TextsUtils;
import org.json.JSONObject;

public class CommandHandler extends AbstractConnectionHandler {
	
	private Logger log = Logger.getLogger(Constants.LOG);
	
	private Robot robot;
	private boolean enableControl;
	private boolean enableChat;

	private JTextArea display;

	/**
	 * Receive robot commands
	 */
	public CommandHandler(Socket socket,Robot robot,String key, JTextArea display) {
		this.socket = socket;
		this.robot = robot;
		this.key = key;
		this.display = display;
		try {
			this.inputStream = socket.getInputStream();
			this.outputStream = socket.getOutputStream();
		} catch (IOException e) {
			log.warn(e.getMessage());
		}
		log.debug("CommandHandler :"+socket.getLocalPort()+" ::"+socket.getPort());
		this.enableChat = false;
		this.enableControl = false;
	}
	
	public void run() {
		Runtime runtime = Runtime.getRuntime();
		while (true) {
			JSONObject json = null;
			try{
				json = receiveCommand();
				try{
					if(json!=null){
						log.info(json.toString());
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
								log.error(json.get("isTyping"));
							}
							if(json.has("chatMessage")){
								log.error(json.get("chatMessage"));
								String content = new String(Base64.decodeBase64(json.getString("chatMessage")),"UTF-8");
								log.error(content);
								display.setText(display.getText()+"\n"+content);
							}
						}
						log.debug(json.toString());
					}else{
						log.debug(TextsUtils.getText("error.jsonisnullreceiver"));
						Thread.sleep(200);
					}
				}catch(Exception e){
					log.warn(e.getMessage());
				}
			}catch(Exception exc){
				log.error(TextsUtils.getText("error.exceptiontryingtoextractmessage"));
			}finally{
				runtime.gc();
			}
		}
	}

	public void setEnableControl(boolean enableControl) {
		this.enableControl = enableControl;
	}

	public void setEnableChat(boolean enableChat) {
		this.enableChat = enableChat;
	}
}