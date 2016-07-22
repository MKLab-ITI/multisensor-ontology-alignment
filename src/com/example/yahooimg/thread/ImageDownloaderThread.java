package com.example.yahooimg.thread;

public class ImageDownloaderThread extends Thread {

	private String imageUrl;
    private String file;
    private Lock lock;
 
    public ImageDownloaderThread(String imageUrl, String destinationPath, Lock lock) {
        this.imageUrl = imageUrl;
        this.file = destinationPath;
        this.lock = lock;
    }
 
    @Override
    public void run() {
        try {
            lock.addRunningThread();
            ImageDownloader imd = new ImageDownloader(imageUrl);
            imd.download();
            imd.saveImage(file);
            
            synchronized (lock) {
                lock.notify();
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	lock.removeRunningThread();
        }
    }

}
