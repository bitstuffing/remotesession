package org.java.utils.remotesession.panel;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private Image image;

	public ImagePanel(Image image) {
		this.image = image;
	}
	
	public void setImage(Image image){
		this.image = image;
	}

	public void paintComponent(Graphics g) {
		g.drawImage(this.image, 0, 0, this);
		this.image = null;
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
	}

}