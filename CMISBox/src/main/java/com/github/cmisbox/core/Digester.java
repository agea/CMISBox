package com.github.cmisbox.core;

import java.io.FileInputStream;
import java.io.IOException;

import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.SHA256Digest;

public class Digester {
	private static GeneralDigest md = new SHA256Digest();
	private static int buflen = 32768;

	public static String calculateDigest(String path) throws IOException {
		byte[] out = new byte[Digester.md.getDigestSize()];
		byte[] buffer = new byte[Digester.buflen];

		FileInputStream fis = new FileInputStream(path);
		int r;

		while ((r = fis.read(buffer)) != -1) {
			Digester.md.update(buffer, 0, r);
		}
		Digester.md.doFinal(out, 0);

		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < out.length; i++) {
			hexString.append(Integer.toHexString(0xFF & out[i]));
		}
		return hexString.toString();
	}
}
