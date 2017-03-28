package org.java.utils.remotesession;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.Socket;

import org.java.utils.remotesession.utils.ImageUtils;

public class ImageSender extends AbstractConnectionHandler {

	private float quality = 0.7f;
	private Robot robot;
	private int framesPerSecond = 5;
	
	public void setQuality(float quality){
		this.quality = quality;
	}
	
	public void setFramesPerSecond(int frames){
		this.framesPerSecond = frames;
	}

	public ImageSender(Socket socket, Robot robot, String key) {
		this.socket = socket;
		this.robot = robot;
		this.key = key;
		try {
			this.inputStream = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.outputStream = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Sender :"+socket.getLocalPort()+" ::"+socket.getPort());
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