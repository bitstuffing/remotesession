package org.java.utils.remotesession;

import java.io.IOException;
import java.net.Socket;

public class LocalConnectionHandler extends AbstractConnectionHandler {

	public LocalConnectionHandler(Socket socket,String key) {
		this.socket = socket;
		this.key = key;
		try {
			this.inputStream = this.socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.outputStream = this.socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
