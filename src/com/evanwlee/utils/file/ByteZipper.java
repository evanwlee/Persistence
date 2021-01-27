package com.evanwlee.utils.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 *
 * Static utility methods for zipping and unzipping byte arrays.
 * Uses GZIP rather than ZIP since there isn't much point to
 * using ZIP when you're not dealing with file entries.
 */
public class ByteZipper {
	/**
	 * Return the GZIPped form of the given byte array.
	 * The given array is left intact.
	 * @param unzippedBytes
	 * @return gzipped bytes
	 */
	static public byte[] zip(byte[] unzippedBytes) {
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

		try {
			GZIPOutputStream zipOut = new GZIPOutputStream(bytesOut);
			zipOut.write(unzippedBytes);

			zipOut.flush();
			zipOut.close();
		} catch (IOException ioe) {
			// writing out to a memory resident byte bucket,
			// so we trust we can't have an IOException.
			ioe.printStackTrace();
		}

		byte[] zippedBytes = bytesOut.toByteArray();
		return zippedBytes;
	}

	/**
	 * Return the raw form of the given GZIPped byte array.
	 * The given array is left intact.
	 * @param zippedBytes
	 * @return unzipped bytes
	 */
	static public byte[] unzip(byte[] zippedBytes) {
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		ByteArrayInputStream bytesIn = new ByteArrayInputStream(zippedBytes);

		try {
			GZIPInputStream zipIn = new GZIPInputStream(bytesIn);

			// completely read the unzipped bytes, storing them in bytesOut
			byte[] buffer = new byte[1024];
			for (int bytesRead = zipIn.read(buffer); bytesRead > -1; bytesRead = zipIn.read(buffer)) {
				bytesOut.write(buffer, 0, bytesRead);
			}

			zipIn.close();
		} catch (IOException ioe) {
			// reading from a memory resident byte bucket,
			// so we trust we can't have an IOException.
			ioe.printStackTrace();
		}

		byte[] unzippedBytes = bytesOut.toByteArray();
		return unzippedBytes;
	}
}
