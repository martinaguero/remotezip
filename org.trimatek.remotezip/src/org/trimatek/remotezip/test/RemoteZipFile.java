package org.trimatek.remotezip.test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.trimatek.remotezip.model.RemoteZipEntry;

public class RemoteZipFile {

	private RemoteZipEntry[] entries;
	private String baseUrl;

	int maxFileOffset;
	int centralOffset, centralSize;
	int totalEntries;

	public RemoteZipFile() {
		System.setProperty("https.proxyHost", "proxy.up");
		System.setProperty("https.proxyPort", "8080");
	}

	public RemoteZipEntry[] getEntries() {
		return entries;
	}

	public boolean load(String path) throws IOException {

		if (!findCentralDirectory(path)) {
			return false;
		}

		maxFileOffset = centralOffset;

		baseUrl = path;
		entries = new RemoteZipEntry[totalEntries];

		URL url = new URL(path);
		HttpURLConnection req = (HttpURLConnection) url.openConnection();
		req.setRequestProperty("Range", "bytes=" + centralOffset + "-"
				+ centralOffset + centralSize);
		req.connect();

		System.out.println("Response Code: " + req.getResponseCode());
		System.out.println("Content-Length: " + req.getContentLengthLong());
		System.out.println("Total entries: " + totalEntries);

		InputStream s = req.getInputStream();

		try {

			for (int i = 0; i < totalEntries; i++) {
				if (readLeInt(s) != ZipInputStream.CENSIG) {
					throw new ZipException("Wrong Central Directory signature");
				}

				readLeInt(s);
				readLeShort(s);
				int method = readLeShort(s);
				int dostime = readLeInt(s);
				int crc = readLeInt(s);
				int csize = readLeInt(s);
				int size = readLeInt(s);
				int nameLen = readLeShort(s);
				int extraLen = readLeShort(s);
				int commentLen = readLeShort(s);

				readLeInt(s);
				readLeInt(s);
				int offset = readLeInt(s);

				byte[] buffer = new byte[Math.max(nameLen, commentLen)];

				readAll(buffer, 0, nameLen, s);
				String name = new String(buffer, "UTF-8");

				RemoteZipEntry entry = new RemoteZipEntry(name);

				entry.setMethod((int) (method & 0xffffffffL));
				entry.setCrc(crc & 0xffffffffL);
				entry.setSize(size & 0xffffffffL);
				entry.setCompressedSize(csize & 0xffffffffL);
				// TODO revisar dato de tiempo
				entry.setTime(dostime);

				System.out.println(name + " cmethod: " + entry.getMethod()
						+ " crc: " + entry.getCrc() + " zsize: "
						+ entry.getSize() + " comp size:  "
						+ entry.getCompressedSize() + " time: "
						+ entry.getTime());

				if (extraLen > 0) {
					byte[] extra = new byte[extraLen];
					readAll(extra, 0, extraLen, s);
					entry.setExtra(extra);
				}

				if (commentLen > 0) {
					readAll(buffer, 0, commentLen, s);
					entry.setComment(new String(buffer, "UTF-8"));
				}

				entry.setZipFileIndex(i);
				entry.setOffset(offset);
				entries[i] = entry;

			}
		} finally {
			s.close();
			req.disconnect();
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
			req.setRequestProperty("Range", "bytes=" + "-"
					+ (currentLength + 22));
			req.connect();
			System.out.println("Respnse Code: " + req.getResponseCode());
			System.out.println("Content-Length: " + req.getContentLength());

			InputStream is = req.getInputStream();
			byte[] bb = new byte[req.getContentLength()];
			// System.out.println(Hex.encodeHexString( bytes ));
			// byteArrayToHex(bb);

			int endSize = readAll(bb, 0, req.getContentLength(), is);

			req.disconnect();

			int pos = endSize - 22;
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
				centralSize = makeInt(bb, pos + 12);
				centralOffset = makeInt(bb, pos + 16);
				totalEntries = makeShort(bb, pos + 10);
				System.out.println(totalEntries);
				return true;
			}

		}

		return false;
	}

	public static int makeInt(byte[] bb, int pos) {
		int zero = bb[pos + 0];
		if (zero < 0)
			zero += 256;
		int one = bb[pos + 1];
		if (one < 0)
			one += 256;
		int three = bb[pos + 2];
		if (three < 0)
			three += 256;
		int four = bb[pos + 3];
		if (four < 0)
			four += 256;
		return zero | one << 8 | three << 16 | four << 24;
	}

	public static int makeShort(byte[] bb, int pos) {
		int zero = bb[pos + 0];
		if (zero < 0)
			zero += 256;
		int one = bb[pos + 1];
		if (one < 0)
			one += 256;
		return zero | one << 8;
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

	private static String byteToHex(byte b) {
		return String.format("%02x", b & 0xff);
	}

	public static int readAll(byte[] bb, int p, int sst, InputStream s)
			throws IOException {
		int ss = 0;
		while (ss < sst) {
			int r = s.read(bb, p, sst - ss);
			if (r <= 0)
				return ss;
			ss += r;
			p += r;
		}
		return ss;
	}

	int readLeInt(InputStream s) throws IOException {
		return readLeShort(s) | readLeShort(s) << 16;
	}

	int readLeShort(InputStream s) throws IOException {
		int first = new DataInputStream(s).readByte();
		if (first < 0)
			first += 256;
		int second = new DataInputStream(s).readByte();
		if (second < 0)
			second += 256;
		return first | second << 8;
	}

	public InputStream getInputStream(RemoteZipEntry entry)
			throws MalformedURLException, IOException {

		if (entry.getSize() == 0) {
			return null;
		}

		if (entries == null) {
			throw new IllegalStateException("ZipFile has closed");
		}

		int index = entry.getZipFileIndex();
		if (index < 0 || index >= entries.length
				|| entries[index].getName() != entry.getName()) {
			throw new IndexOutOfBoundsException();
		}

		HttpURLConnection req = (HttpURLConnection) new URL(baseUrl)
				.openConnection();
		int limit = (int) (entry.getOffset() + entry.getCompressedSize() + 16 + 65536 * 2);
		if (limit >= maxFileOffset) {
			limit = maxFileOffset - 1;
		}

		req.setRequestProperty("Range", "bytes=" + entry.getOffset() + "-"
				+ limit);

		InputStream baseStream = req.getInputStream();

		skipLocalHeader(baseStream, entries[index]);

		return null;
	}

	private void skipLocalHeader(InputStream baseStream, RemoteZipEntry entry)
			throws IOException {
		if (readLeInt(baseStream) != ZipEntry.LOCSIG) {
			throw new ZipException("Wrong Local header signature");
		}

		skip(baseStream, 10 + 12);
		int namelen = readLeShort(baseStream);
		int extralen = readLeShort(baseStream);
		skip(baseStream, namelen + extralen);

	}

	private static void skip(InputStream s, int n) throws IOException {
		for (int i = 0; i < n; i++)
			new DataInputStream(s).readByte();
	}

	public static void main(String[] args) throws IOException {
		RemoteZipFile rz = new RemoteZipFile();
		rz.load("https://repo1.maven.org/maven2/abbot/abbot/1.4.0/abbot-1.4.0.jar");
		// rz.load("https://repo1.maven.org/maven2/bcel/bcel/5.1/bcel-5.1.jar");
		// rz.load("http://percro.sssup.it/~pit/tools/miranda.zip");
		rz.getInputStream(rz.getEntries()[0]);
		
		
	}

}
