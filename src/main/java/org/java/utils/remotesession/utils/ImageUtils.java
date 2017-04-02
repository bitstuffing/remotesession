package org.java.utils.remotesession.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.log4j.Logger;

public class ImageUtils {
	
	private static Logger log = Logger.getLogger(Constants.LOG);
	
	public final static String JPG = "jpeg";
	public final static String PNG = "png";
	public final static String TIFF = "tiff";
	
	private static void write(BufferedImage image, float quality, ByteArrayOutputStream out) throws IOException {
		Iterator writers = ImageIO.getImageWritersBySuffix(JPG);
		if (!writers.hasNext()){
			throw new IllegalStateException("No writers found");
		}
		ImageWriter writer = (ImageWriter) writers.next();
		ImageOutputStream ios = ImageIO.createImageOutputStream(out);
		writer.setOutput(ios);
		ImageWriteParam param = writer.getDefaultWriteParam();
		if (quality >= 0) {
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
		}
		writer.write(null, new IIOImage(image, null, null), param);
	}

	public static BufferedImage read(byte[] bytes) {
		try {
			return ImageIO.read(new ByteArrayInputStream(bytes));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] getBytes(BufferedImage image, float quality) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream(50000);
			write(image, quality, out);
			return out.toByteArray();
		} catch (IOException e) {
			log.warn("Fail trying to write BufferedImage with quality: "+quality);
			throw new RuntimeException(e);
		}
	}

	public static BufferedImage compress(BufferedImage image, float quality) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			write(image, quality, out);
			return ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String imgToBase64String(BufferedImage img){
		return imgToBase64String(img,JPG);
	}

	public static String imgToBase64String(BufferedImage img, String formatName) {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, formatName, os);
			return Base64.getEncoder().encodeToString(os.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
