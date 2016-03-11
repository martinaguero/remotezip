package org.trimatek.remotezip.tools;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class StreamUtils {
	
	public static String printString(java.io.InputStream stream) {
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static String printHexa(InputStream stream) throws IOException {
		StringBuffer sb = new StringBuffer();
		byte[] bytes = IOUtils.toByteArray(stream);
		for (byte b : bytes) {
			sb.append(byteToHex(b) + " ");
		}
		return sb.toString();
	}
	
	private static String byteToHex(byte b) {
		return String.format("%02x", b & 0xff);
	}

}
