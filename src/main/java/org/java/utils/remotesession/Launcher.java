package org.java.utils.remotesession;

import javax.swing.UIManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java.utils.remotesession.utils.Constants;
import org.java.utils.remotesession.utils.TextsUtils;
import org.java.utils.remotesession.view.MainJFrame;

public class Launcher {
	
	private static Logger log = LogManager.getLogger(Constants.LOG);

	public static void main(String[] args) {
		try {
			//configure logs
//			PropertyConfigurator.configure(Thread.currentThread().getContextClassLoader().getResource("log4j.properties"));
			//choose swing lock and feel 
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			UIManager.put("swing.boldMetal", Boolean.FALSE);
		}catch (Exception e1) {
			log.warn(e1.getMessage());
		}
		log.info(TextsUtils.getText("message.welcome"));
		MainJFrame frame = new MainJFrame();
        frame.pack();
        frame.setVisible(true);
	}

	

}
