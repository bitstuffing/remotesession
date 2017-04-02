package org.java.utils.remotesession;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.java.utils.remotesession.utils.Constants;

public class LocalConnectionHandler extends AbstractConnectionHandler {
	
	private Logger log = Logger.getLogger(Constants.LOG);

	public LocalConnectionHandler(Socket socket,String key) {
		this.socket = socket;
		this.key = key;
		try {
			this.inputStream = this.socket.getInputStream();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		try {
			this.outputStream = this.socket.getOutputStream();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
}
