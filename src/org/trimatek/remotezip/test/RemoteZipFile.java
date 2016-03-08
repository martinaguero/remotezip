package org.trimatek.remotezip.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

public class RemoteZipFile {
	
	public RemoteZipFile(){
		System.setProperty("https.proxyHost", "proxy.up");
		System.setProperty("https.proxyPort", "8080");
	}

	private ZipEntry[] entries;
	private String baseUrl;
	private int maxFileOffset;

	public boolean load(String path) throws IOException {

		int centralOffset, centralSize;
		int totalEntries;
		if (!findCentralDirectory(path)) {
			return false;
		}

		return false;
	}

	private boolean findCentralDirectory(String path) throws IOException {

		URL url = new URL(path);
		int currentLength = 256;
		int entries = 0;
		int size = 0;
		int offset = -1;
		
//		while(true){
			
			HttpURLConnection req = (HttpURLConnection)url.openConnection();
			req.setRequestProperty("Range", "bytes=" + "-" + 278);
			req.connect();
			System.out.println("Respnse Code: " + req.getResponseCode());
	        System.out.println("Content-Length: " + req.getContentLengthLong());
	        
	        
	        
	        InputStream is = req.getInputStream();
	        byte[] bytes = IOUtils.toByteArray(is);
	        System.out.println(Hex.encodeHexString( bytes ));
	        
	        
//		}
		
		return false;
	}
	
	public static void main(String[] args) throws IOException {
		RemoteZipFile rz = new RemoteZipFile();
		rz.load("https://repo1.maven.org/maven2/abbot/abbot/1.4.0/abbot-1.4.0.jar");

	}

}
