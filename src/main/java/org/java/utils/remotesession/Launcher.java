package org.java.utils.remotesession;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java.utils.remotesession.utils.Constants;

public class Launcher {
	
	private static Logger log = LogManager.getLogger(Constants.LOG);

	private static final String WAIT_TO = "Allow and wait for a remotesession";
	private static final String SEND_TO = "Send your local desktop to a remotesession";

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
		log.info("Welcome!");
		final JFrame v = new JFrame("Welcome");
		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints cc = new GridBagConstraints();
		cc.gridx = 0;
		cc.gridy = 0;
//		v.setLayout(new BoxLayout(v,BoxLayout.Y_AXIS));
		v.setLayout(gridBagLayout);
		v.add(new Label("Please choose an option:"),cc);
		final JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addItem(SEND_TO);
		comboBox.addItem(WAIT_TO);
		cc.gridx = 0;
		cc.gridy = 1;
		v.add(comboBox,cc);
		JButton button = new JButton("Launch");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedItem = (String)comboBox.getSelectedItem();
				if(selectedItem.equals(SEND_TO)){
					new Thread(){
						public void run() { new RemoteDesktopSender(); };
					}.start();
				}else if(selectedItem.equals(WAIT_TO)){
					new Thread(){
						public void run() { new RemoteDesktopReceiver(); };
					}.start();
				}
				v.setVisible(false);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					log.warn(ex.getMessage());
				}
				v.dispose();
			}
		});
		cc.gridx = 0;
		cc.gridy = 2;
		v.add(button,cc);
        v.pack();
        v.setVisible(true);
        log.debug("Showing form...");
        v.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

}
