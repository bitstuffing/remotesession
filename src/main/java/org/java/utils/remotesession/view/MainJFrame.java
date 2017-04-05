package org.java.utils.remotesession.view;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java.utils.remotesession.RemoteDesktopSender;
import org.java.utils.remotesession.events.AllowActionListener;
import org.java.utils.remotesession.utils.Constants;
import org.java.utils.remotesession.utils.TextsUtils;

public class MainJFrame extends JFrame{
	
	private static Logger log = LogManager.getLogger(Constants.LOG);
	
	private static Label statusLabel; //used to manage status message in frame
	private static Label mainActionLabel; //used to show local session messages
	
	public MainJFrame(){ 
		build();
	}
	
	private static JPanel getSecundaryActionPanel() {
		final JPanel v = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints cc = new GridBagConstraints();
		cc.anchor = GridBagConstraints.NORTHWEST;
		cc.fill = GridBagConstraints.HORIZONTAL;
		cc.weightx = 1;
		cc.weighty = 1;
		cc.gridx = 0;
		cc.gridy = 0;
		v.setLayout(layout);
		JLabel jlabel = new JLabel(TextsUtils.getText("label.remoteid"));
		v.add(jlabel,cc);
		final JTextField jTextField = new JTextField();
		jTextField.setPreferredSize(new Dimension(60,20));
		jTextField.setHorizontalAlignment(FlowLayout.LEFT);
		cc.gridx = 1;
		cc.gridy = 0;
		v.add(jTextField,cc);
		Border titleBorder = new TitledBorder(new LineBorder(Color.GRAY), TextsUtils.getText("label.remoteconnection"));
//		v.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
		v.setBorder(titleBorder);
		final JButton button = new JButton(TextsUtils.getText("label.connect"));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(){
					public void run() { new RemoteDesktopSender(jTextField.getText()); };
				}.start();
			}
		});
		cc.gridx = 0;
		cc.gridy = 1;
		v.add(button, cc);
		return v;
	}

	private static JPanel getMainActionPanel() {
		final JPanel v = new JPanel();
		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints cc = new GridBagConstraints();
//		cc.fill = GridBagConstraints.BOTH;
		cc.anchor = GridBagConstraints.NORTHWEST;
		cc.weightx = 1;
		cc.weighty = 1;
		cc.gridx = 0;
		cc.gridy = 0;
		v.setLayout(gridBagLayout);
		mainActionLabel = new Label(TextsUtils.getText("label.createyourlocalconnection"));
		v.add(mainActionLabel,cc);
		final JButton button = new JButton(TextsUtils.getText("label.allow"));
		button.addActionListener(new AllowActionListener(statusLabel,mainActionLabel));
		cc.gridx = 0;
		cc.gridy = 2;
		v.add(button,cc);
//		v.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
		Border titleBorder = new TitledBorder(new LineBorder(Color.GRAY), TextsUtils.getText("label.localconnection"));
		v.setBorder(titleBorder);
        return v;
	}

	private static JMenuBar createMenu() {
		final JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu(TextsUtils.getText("menu.file"));
		JMenuItem exitItem = new JMenuItem(TextsUtils.getText("menu.file.quit"));
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(menuBar);
				topFrame.dispose();
				log.info(TextsUtils.getText("message.bye"));
			}
		});
		file.add(exitItem);
		menuBar.add(file);
		JMenu configuration = new JMenu(TextsUtils.getText("menu.configuration"));
		JMenuItem propertiesItem = new JMenuItem(TextsUtils.getText("menu.configuration.properties"));
		configuration.add(propertiesItem);
		menuBar.add(configuration);
		JMenu about = new JMenu(TextsUtils.getText("menu.about"));
		JMenuItem aboutItem = new JMenuItem(TextsUtils.getText("menu.about.about"));
		about.add(aboutItem);
		menuBar.add(about);
		return menuBar;
	}
	
	public void build(){
		setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		JMenuBar jmenu = createMenu();
		jmenu.setBackground(Color.getHSBColor(0f, 0f, 0.90f));
		setJMenuBar(jmenu);
		setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		//at this moment needs to be instanced, because it's called in action button instance
		statusLabel = new Label(TextsUtils.getText("label.status"));
        statusLabel.setBackground(Color.getHSBColor(0f, 0f, 0.85f));
		JPanel mainPanel = getMainActionPanel();
		add(mainPanel);
		mainPanel.setBackground(Color.getHSBColor(0f, 0f, 0.95f));
		JPanel secundaryPanel = getSecundaryActionPanel();
		secundaryPanel.setBackground(Color.getHSBColor(0f, 0f, 0.95f));
		add(secundaryPanel);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(statusLabel); //status label instanced before
	}
}
