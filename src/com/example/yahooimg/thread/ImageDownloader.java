package com.example.yahooimg.thread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.apache.http.client.methods.HttpGet;

public class ImageDownloader {
	
	URL url;
	//URLConnection connection;
	HttpURLConnection connection;
	HttpGet connection2;
	BufferedImage image;
	
	public ImageDownloader(URL url) {
		this.url = url;
		
		try {
			connection = (HttpURLConnection)this.url.openConnection();
			setConnectionParams();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ImageDownloader(String url) {
		try {
			this.url = new URL(url);
			connection = (HttpURLConnection)this.url.openConnection();
			setConnectionParams();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setConnectionParams() {
		if (connection != null) {
			try {
				HttpURLConnection.setFollowRedirects(false);
				connection.setConnectTimeout(3000);
				connection.setReadTimeout(3000);
				connection.setRequestMethod("GET");
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
				//connection.connect();
			} catch (Exception e) {
				System.out.println("Something else has occured");
			}
		}
	}
	
	public void download() {
		InputStream is = null;
		try {
			//image = ImageIO.read(connection.getURL());
			is = connection.getInputStream();
			image = ImageIO.read(is);
		} catch (SocketTimeoutException e) {
			System.out.println("Socket timeout, image "+url.toString()+" could not be downloaded");
		} catch (IOException e) {
			System.out.println("Image "+url.toString()+" could not be downloaded");
			//e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("Image "+url.toString()+" is of wrong type");
		} catch (Exception e) {
			System.out.println("Unknown error in image download");
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (connection != null)
				connection.disconnect();
		}
	}
	
	public void saveImage(String file) {
		try {
			if (image != null)
				ImageIO.write(image, "jpg", new File(file));
		} catch (IOException e) {
			System.out.println("Image "+url.toString()+" couldn't be saved");
			//e.printStackTrace();
		}
	}
	
	public void saveImage(String dir, String file) {
		
	}
}
