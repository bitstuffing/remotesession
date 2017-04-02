package org.java.utils.remotesession.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class ConnectionUtils {
	
	private static Logger log = Logger.getLogger(Constants.LOG);
	
	public static String post(String httpUrl,String content) throws IOException{
		URL url = new URL(httpUrl);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("User-Agent", Constants.USER_AGENT);
		conn.setRequestProperty("Accept", "*");
		conn.setDoOutput(true);
		if(content!=null && !content.isEmpty()){
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(content);
			writer.flush();
			writer.close();
		}
		conn.connect();
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String response = "";
		while ((line = reader.readLine()) != null) {
			response += line;
		}
		reader.close();
		return response;
	}
	
	public static String get(String httpUrl) throws IOException{
		return post(httpUrl,null);
	}

	public static String getLocalIpAddress() {
		String local = null;
		try {
			local = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn(e.getMessage());
		}
		return local;
	}
	
	public static String getInternetIpAddress(){
		String remote = null;
		String[] providers = {Constants.CHECKIP_AMAZON_PROVIDER,
				Constants.CHECKIP_MYEXTERNALIP_PROVIDER,
				Constants.CHECKIP_IPECHO_PROVIDER,
				Constants.CHECKIP_TRACKIP_PROVIDER,
				Constants.CHECKIP_ICANHAZIP_PROVIDER};
		int i=0;
		do{
			try {
				remote = get(providers[i]);
			} catch (IOException e) {
				log.warn(e.getMessage());
			}
		}while(remote==null && ++i<providers.length);
		return remote;
	}
	
}
