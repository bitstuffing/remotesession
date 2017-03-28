package org.java.utils.remotesession;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.commons.codec.binary.Base64;
import org.java.utils.remotesession.utils.EncryptionUtils;
import org.json.JSONObject;

public abstract class AbstractConnectionHandler extends Thread {
	
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
			System.out.println("sending...");
			try {
				writeJSONToOutputStream(jsonObject);
			} catch (Exception e) {
				e.printStackTrace();
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
			System.out.println("Sending "+bytesEncrypted.length+" bytes to "+socket.getLocalPort()+" :: "+socket.getPort());
		}catch(Exception e){
			System.out.println("remote sender error");
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
				System.out.println("receiving command...");
				Object object = ois.readObject();
				if(object!=null && object instanceof byte[]){
					String content = new String(EncryptionUtils.decrypt(key, null, (byte[])object),"UTF-8");
					System.out.println("command length: "+content.length()+" bytes");
					json = new JSONObject(content);
				}else{
					System.out.println("rejected: "+object);
				}
			}catch(Exception e){
				System.out.println("Remote receiver fails!");
				e.printStackTrace();
			}finally{
				ois = null;
				wait2 = false;
			}
			
		}
		return json;
	}
}
