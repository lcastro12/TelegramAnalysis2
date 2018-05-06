package com.coremedia.iso;

import java.io.UnsupportedEncodingException;
import net.hockeyapp.android.utils.HttpURLConnectionBuilder;

public final class Utf8 {
    public static byte[] convert(String s) {
        if (s == null) {
            return null;
        }
        try {
            return s.getBytes(HttpURLConnectionBuilder.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static String convert(byte[] b) {
        if (b == null) {
            return null;
        }
        try {
            return new String(b, HttpURLConnectionBuilder.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static int utf8StringLengthInBytes(String utf8) {
        if (utf8 == null) {
            return 0;
        }
        try {
            return utf8.getBytes(HttpURLConnectionBuilder.DEFAULT_CHARSET).length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }
}
