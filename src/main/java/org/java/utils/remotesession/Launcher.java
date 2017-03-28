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

public class Launcher {

	private static final String WAIT_TO = "Allow and wait for a remotesession";
	private static final String SEND_TO = "Send your local desktop to a remotesession";

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			UIManager.put("swing.boldMetal", Boolean.FALSE);
		}catch (Exception e1) {
			e1.printStackTrace();
		}
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
					ex.printStackTrace();
				}
				v.dispose();
			}
		});
		cc.gridx = 0;
		cc.gridy = 2;
		v.add(button,cc);
        v.pack();
        v.setVisible(true);
        v.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

}
