package org.java.utils.remotesession.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class EncryptionUtils {
	
	private final static String alg = "AES";
	private final static String cI = "AES/CBC/PKCS5Padding";
	
	public static byte[] encrypt(String key, String iv, byte[] bytes) throws Exception {
		Cipher cipher = Cipher.getInstance(cI);
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), alg);
		if(iv!=null){
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
		}else{
			IvParameterSpec ivParameterSpec = new IvParameterSpec(key.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
		}
		byte[] encrypted = cipher.doFinal(bytes);
		return Base64.encodeBase64(encrypted);
	}

	public static byte[] decrypt(String key, String iv, byte[] bytes) throws Exception {
		Cipher cipher = Cipher.getInstance(cI);
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), alg);
		byte[] enc = Base64.decodeBase64(bytes);
		if(iv!=null){
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
		}else{
			IvParameterSpec ivParameterSpec = new IvParameterSpec(key.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
		}
		byte[] decrypted = cipher.doFinal(enc);
		return decrypted;
	}
}
