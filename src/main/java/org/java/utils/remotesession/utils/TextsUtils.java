package org.java.utils.remotesession.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class TextsUtils {
	
	private static ResourceBundle bundle;
	private static Locale selectedLocale;
	
	static{
		selectedLocale = new Locale("en");
		bundle = ResourceBundle.getBundle("texts", selectedLocale);
	}

	public static String getText(String key){
		String text = key;
		try{
			text = bundle.getString(key);
		}catch(Exception e){
			e.printStackTrace();
		}
		return text;
	}
}
