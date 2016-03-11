package org.trimatek.remotezip.test;

import java.io.IOException;
import java.io.InputStream;

import org.trimatek.remotezip.model.RemoteZipEntry;
import org.trimatek.remotezip.tools.RemoteZipFile;
import org.trimatek.remotezip.tools.StreamUtils;

public class RemoteZipTest {

	public static void main(String[] args) throws IOException {
		RemoteZipFile rz = new RemoteZipFile();
		RemoteZipEntry[] entries =
		// rz.load("https://repo1.maven.org/maven2/abbot/abbot/1.4.0/abbot-1.4.0.jar");
		// rz.load("https://repo1.maven.org/maven2/bcel/bcel/5.1/bcel-5.1.jar");
		// rz.load("http://percro.sssup.it/~pit/tools/miranda.zip");
		rz.load("https://repo1.maven.org/maven2/jp/sf/amateras/mirage/1.2.3/mirage-1.2.3.jar");
		int c = 0;
		for (RemoteZipEntry e : entries) {
			System.out.println("[" + c++ + "]" + e.getName() + " cmethod: "
					+ e.getMethod() + " crc: " + e.getCrc() + " zsize: "
					+ e.getSize() + " comp size:  " + e.getCompressedSize()
					+ " time: " + e.getTime());
		}

		InputStream stream = rz.getInputStream(entries[162]);
		System.out.println(StreamUtils.printHexa(stream));
		// System.out.println(StreamUtils.printString(stream));
	}

}
