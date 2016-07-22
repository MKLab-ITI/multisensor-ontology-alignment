package com.example.yahooimg;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.example.yahooimg.thread.ImageDownloaderThread;
import com.example.yahooimg.thread.Lock;
import com.google.gson.Gson;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import gr.iti.mklab.visual.examples.FolderIndexingMT; 

public class ImgService {

	public StHttpRequest authorize() {
		StHttpRequest httpsRequest = new StHttpRequest();

		String params = Params.ImagesService;

		OAuthConsumer consumer = new DefaultOAuthConsumer(Params.consumer_key, Params.consumer_secret);

		httpsRequest.setOAuthConsumer(consumer);
		
		return httpsRequest;
	}
	
	public void getImagesForQueries(StHttpRequest httpsRequest, String[] queries, String mainFolder){
		try {
			for (String qText : queries) {
				getImagesForQuery(httpsRequest, qText, mainFolder);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getImagesForQuery(StHttpRequest httpsRequest, String qText, String mainFolder) throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, IOException {
		//String qText = "car";

		String params = Params.ImagesService;

		params = params.concat("?q=");
		params = params.concat(URLEncoder.encode(tokensToWord(tokenize(qText)), "UTF-8").replace("+", "%20"));
		String url = Params.yahooServer + params;

		int responseCode = httpsRequest.sendGetRequest(url);

		String response = httpsRequest.getResponseBody();
		System.out.println("response " + response);
		Gson gson = new Gson();

		JsImgResponse jsImgResponse = gson.fromJson(response, JsImgResponse.class);
		if (jsImgResponse != null && jsImgResponse.bossRsp != null && jsImgResponse.bossRsp.wResponse != null && jsImgResponse.bossRsp.wResponse.results != null
				&& jsImgResponse.bossRsp.wResponse.results.size() != 0) {

			File dirFile = new File(mainFolder+"/"+qText);
			if (!dirFile.exists())
				dirFile.mkdirs();
			
			Lock lock = new Lock();
			for (int i = 0; i < jsImgResponse.bossRsp.wResponse.results.size(); i++) {
				String imgUrl = "";
				try {
					imgUrl = jsImgResponse.bossRsp.wResponse.results.get(i).imgUrl;
					ImageDownloaderThread idt = new ImageDownloaderThread(imgUrl, mainFolder+"/"+qText+"/image"+i+".jpg", lock);
					idt.start();
				} catch (Exception e) {
					System.out.println("Image "+imgUrl+" cannot be downloaded.");
				}
				//System.out.println("ImgUrl" + jsImgResponse.bossRsp.wResponse.results.get(i).imgUrl);
				//System.out.println("webSiteUrl " + jsImgResponse.bossRsp.wResponse.results.get(i).ImgWebsite);
			}
			try {
				while (lock.getRunningThreadsNumber() > 0) {
					synchronized(lock) {
						lock.wait();
					}
					System.out.println("Waiting for "+lock.getRunningThreadsNumber()+" thread(s) to finish");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	public List<String> getImagesListForQuery(StHttpRequest httpsRequest, String qText) {
		//String qText = "car";
		
		List<String> ret = new ArrayList<String>();
		String params = Params.ImagesService;
		try {
			params = params.concat("?q=");
			params = params.concat(URLEncoder.encode(tokensToWord(tokenize(qText)), "UTF-8").replace("+", "%20"));
			String url = Params.yahooServer + params;
	
			int responseCode = httpsRequest.sendGetRequest(url);
	
			String response = httpsRequest.getResponseBody();
			System.out.println("response " + response);
			Gson gson = new Gson();
	
			JsImgResponse jsImgResponse = gson.fromJson(response, JsImgResponse.class);
			if (jsImgResponse != null && jsImgResponse.bossRsp != null && jsImgResponse.bossRsp.wResponse != null && jsImgResponse.bossRsp.wResponse.results != null
					&& jsImgResponse.bossRsp.wResponse.results.size() != 0) {
	
				for (int i = 0; i < jsImgResponse.bossRsp.wResponse.results.size(); i++) {
					String imgUrl = "";
					try {
						imgUrl = jsImgResponse.bossRsp.wResponse.results.get(i).imgUrl;
						ret.add(imgUrl);
					} catch (Exception e) {
						System.out.println("Image "+imgUrl+" cannot be downloaded.");
					}
					//System.out.println("ImgUrl" + jsImgResponse.bossRsp.wResponse.results.get(i).imgUrl);
					//System.out.println("webSiteUrl " + jsImgResponse.bossRsp.wResponse.results.get(i).ImgWebsite);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public List<String> tokenize(String concept) {
		List<String> list = new ArrayList<String>();
		int pos = 0;
		for (int i=0;i<concept.length();i++) {
			if (Character.isUpperCase(concept.charAt(i)) && pos!=i) {
				if (i>0) {
					if (!Character.isUpperCase(concept.charAt(i-1))) {
						list.add(concept.substring(pos, i));
						pos = i;
					}
				}
			}
			else if ((concept.charAt(i) == '_') || (concept.charAt(i) == '-')) {
				list.add(concept.substring(pos, i));
				pos = i+1;
			}
		}
		
		list.add(concept.substring(pos, concept.length()));
		
		return list;
	}
	
	public String tokensToWord(List<String> list) {
		String ret  = "";
		for (String s : list)
			ret += s+" ";
		return ret.trim();
	}
	
	public static void main(String[] args) {
		/*try {
			String[] paths = {"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/YahooBossImgService/images/bicycle",
					"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/YahooBossImgService/images/car",
					"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/YahooBossImgService/images/vehicle",
					"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/YahooBossImgService/images/motorcycle",
					"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/YahooBossImgService/images/truck",
					"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/YahooBossImgService/images/bird",
					"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/YahooBossImgService/images/scooter"};
			
			String[] args1 = {"",
					"",
					"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/Multimedia-Indexing/models/surf_l2_128c_0.csv,D:/Projects/MULTISENSOR/Development/EclipseWorkspace/Multimedia-Indexing/models/surf_l2_128c_1.csv,D:/Projects/MULTISENSOR/Development/EclipseWorkspace/Multimedia-Indexing/models/surf_l2_128c_2.csv,D:/Projects/MULTISENSOR/Development/EclipseWorkspace/Multimedia-Indexing/models/surf_l2_128c_3.csv",
					"128,128,128,128",
					"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/Multimedia-Indexing/models/pca_surf_4x128_32768to1024.txt",
					"100",
					"1048576",
					"10",
					"surf",
					"true"};
			
			for (String path : paths) {
				args1[0] = path+"/";
				args1[1] = path+"-index/";
				
				File dirFile = new File(args1[1]);
				if (!dirFile.exists())
					dirFile.mkdirs();
				
				FolderIndexingMT.main(args1);
			}
		}catch (Exception e) {e.printStackTrace();}*/
		
		/*ImgService s = new ImgService();
		StHttpRequest httpsRequest = s.authorize();
		String[] queries = {"scooter"};
		try {
			s.getImagesForQueries(httpsRequest, queries, "D:/Projects/MULTISENSOR/Development/EclipseWorkspace/YahooBossImgService/images");
		}catch (Exception e) {
			e.printStackTrace();
		}*/
		
	}
}
