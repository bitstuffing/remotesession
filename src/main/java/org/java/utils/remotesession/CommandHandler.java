package org.java.utils.remotesession;

import java.awt.Robot;
import java.io.IOException;
import java.net.Socket;

import org.json.JSONObject;

public class CommandHandler extends AbstractConnectionHandler {
	
	private Robot robot;
	private boolean enableControl;
	private boolean enableChat;

	/**
	 * Receive robot commands
	 */
	public CommandHandler(Socket socket,Robot robot,String key) {
		this.socket = socket;
		this.robot = robot;
		this.key = key;
		try {
			this.inputStream = socket.getInputStream();
			this.outputStream = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Receiver :"+socket.getLocalPort()+" ::"+socket.getPort());
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
						System.out.println(json.toString());
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
//						System.out.println(json.toString());
					}else{
//						System.out.println("json is null - Receiver");
						Thread.sleep(200);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}catch(Exception exc){
				System.out.println("Exception trying to extract message!");
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