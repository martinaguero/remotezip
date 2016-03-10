package org.trimatek.remotezip.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.zip.InflaterInputStream;

import org.trimatek.remotezip.test.RemoteZipFile;

public class PartialInputStream extends InflaterInputStream {

	private InputStream baseStream;
	private long filepos;
	private long end;
	private HttpURLConnection request;

	public PartialInputStream(InputStream in) {
		super(in);
	}

	public PartialInputStream(InputStream baseStream,
			HttpURLConnection request, long len) {
		super(baseStream);
		this.baseStream = baseStream;
		filepos = 0;
		end = len;
		this.request = request;
	}

	public int available() {
		long amount = end - filepos;
		if (amount > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) amount;
	}

	public int readByte() throws IOException {
		if (filepos == end) {
			return -1;
		}
		filepos++;
		return new DataInputStream(baseStream).readByte();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		if (len > end - filepos) {
			len = (int) (end - filepos);
			if (len == 0) {
				return 0;
			}
		}
		int count = RemoteZipFile.readAll(b, off, len, baseStream);
		if (count > 0) {
			filepos += len;
		}
		return count;
	}

	public long SkipBytes(long amount) throws IOException {
		if (amount < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (amount > end - filepos) {
			amount = end - filepos;
		}
		filepos += amount;
		for (int i = 0; i < amount; i++)
			new DataInputStream(baseStream).readByte();
		return amount;
	}

	public void close() throws IOException {
		request.disconnect();
		baseStream.close();
	}

}
