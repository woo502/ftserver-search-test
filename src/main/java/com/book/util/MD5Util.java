package com.book.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

	public static String md5(String s) {
		String md5 = null;
		try {
			md5 = DigestUtils.md5Hex(s.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
		}
		return md5;
    }

}
