package org.java.utils.remotesession;

import java.net.Socket;

public class LocalSender extends AbstractConnectionHandler {

	public LocalSender(Socket socket, String key) {
		this.socket = socket;
		this.key = key;
	}

}
