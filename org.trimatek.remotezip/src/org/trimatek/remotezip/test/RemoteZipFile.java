package org.trimatek.remotezip.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;

public class RemoteZipFile {

	public RemoteZipFile() {
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

		while (true) {

			HttpURLConnection req = (HttpURLConnection) url.openConnection();
			req.setRequestProperty("Range", "bytes=" + "-" + 278);
			req.connect();
			System.out.println("Respnse Code: " + req.getResponseCode());
			System.out.println("Content-Length: " + req.getContentLengthLong());

			InputStream is = req.getInputStream();
			byte[] bb = IOUtils.toByteArray(is);
			// System.out.println(Hex.encodeHexString( bytes ));
			byteArrayToHex(bb);

			req.disconnect();

			int pos = bb.length - 22;
			int state = 0;
			while (pos >= 0) {
				if (bb[pos] == 0x50) {
					if (bb[pos + 1] == 0x4b && bb[pos + 2] == 0x05
							&& bb[pos + 3] == 0x06) {
						System.out.println("found!");
						break; // found!!
					}
					pos -= 4;
				} else
					pos--;
			}

			if (pos < 0) {
				if (currentLength == 65536)
					break;

				if (currentLength == 1024)
					currentLength = 65536;
				else if (currentLength == 256)
					currentLength = 1024;
				else
					break;
			} else {
				// found it!! so at offset pos+3*4 there is Size, and pos+4*4
				// BinaryReader is so elegant but now it's too much
				size = makeInt(bb, pos + 12);
				offset = makeInt(bb, pos + 16);
				entries = makeShort(bb, pos + 10);
				return true;
			}

		}

		return false;
	}

	public static int makeInt(byte[] bb, int pos) {
		return bb[pos + 0] | (bb[pos + 1] << 8) | (bb[pos + 2] << 16)
				| (bb[pos + 3] << 24);
	}

	public static int makeShort(byte[] bb, int pos) {
		return bb[pos + 0] | (bb[pos + 1] << 8);
	}

	private static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		String s = null;
		for (byte b : a) {
			// sb.append(String.format("%02x", b & 0xff));
			s = String.format("%02x", b & 0xff);
			System.out.println(s);
		}
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		RemoteZipFile rz = new RemoteZipFile();
		rz.load("https://repo1.maven.org/maven2/abbot/abbot/1.4.0/abbot-1.4.0.jar");

	}

}
