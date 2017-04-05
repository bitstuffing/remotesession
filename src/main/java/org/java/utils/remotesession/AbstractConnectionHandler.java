package org.java.utils.remotesession;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.java.utils.remotesession.utils.Constants;
import org.java.utils.remotesession.utils.EncryptionUtils;
import org.java.utils.remotesession.utils.TextsUtils;
import org.json.JSONObject;

public abstract class AbstractConnectionHandler extends Thread {
	
	private Logger log = Logger.getLogger(Constants.LOG);
	
	private boolean wait = false;
	protected Socket socket;
	protected String key;
	protected InputStream inputStream;
	protected OutputStream outputStream;
	
	public void sendCommand(String key, byte[] bytes){
		if(!wait){ //TODO, change to while(wait) Thread.sleep(20); or a latency
			wait  = true;
			Runtime runtime = Runtime.getRuntime();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(key, Base64.encodeBase64String(bytes));
			log.debug(TextsUtils.getText("message.sending"));
			try {
				writeJSONToOutputStream(jsonObject);
			} catch (Exception e) {
				log.warn(e.getMessage());
			} finally{
				runtime.gc();
				wait = false;
			}
		}
	}

	public void writeJSONToOutputStream(JSONObject jsonObject) throws Exception {
		ObjectOutputStream oos = null;
		byte[] bytesEncrypted = null;
		try{
			bytesEncrypted = EncryptionUtils.encrypt(this.key, null, jsonObject.toString().getBytes());
			oos = new ObjectOutputStream(outputStream);
			oos.reset();
			oos.writeObject(bytesEncrypted);
			oos.flush();
			log.debug(TextsUtils.getText("message.sendingstart")+" "+bytesEncrypted.length+" bytes to "+socket.getLocalPort()+" :: "+socket.getPort());
		}catch(Exception e){
			log.warn(TextsUtils.getText("error.remoteservererror"));
			throw e;
		}finally {
			oos = null;
			bytesEncrypted = null;
		}
	}
	
	private boolean wait2 = false;
	
	protected JSONObject receiveCommand(){
		JSONObject json = null;
		if(!wait2){
			wait2 = true;
			ObjectInputStream ois = null;
			try{
				ois = new ObjectInputStream(inputStream);
				log.debug(TextsUtils.getText("message.receivingcommand"));
				Object object = ois.readObject();
				if(object!=null && object instanceof byte[]){
					String content = new String(EncryptionUtils.decrypt(key, null, (byte[])object),"UTF-8");
					log.debug(TextsUtils.getText("message.commandlength")+" "+content.length()+" bytes");
					json = new JSONObject(content);
				}else{
					log.info(TextsUtils.getText("message.rejected")+": "+object);
				}
			}catch(Exception e){
				log.warn(TextsUtils.getText("error.remoteserverfails")+": "+e.getMessage());
			}finally{
				ois = null;
				wait2 = false;
			}
			
		}
		return json;
	}
}
