package com.pinterest.android.pdk;

import android.support.v4.util.Pair;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Utils {

    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";
    private static final  String DEFAULT_CONTENT_CHARSET ="ISO-8859-1";
    private static final String TAG = "PDK";
    private static DateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static <T> boolean isEmpty(Collection<T> c) {
        return (c == null) || (c.size() == 0);
    }

    public static boolean isEmpty(Map m) {
        return (m == null) || (m.size() == 0);
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    /**
     * Log errors
     *
     * @param s base String
     * @param params Objects to format in
     */
    public static void loge(String s, Object... params) {
        if (PDKClient.isDebugMode())
            Log.e(TAG, String.format(s, params));
    }

    /**
     * Log info
     *
     * @param s base String
     * @param params Objects to format in
     */
    public static void log(String s, Object... params) {
        if (PDKClient.isDebugMode())
            Log.i(TAG, String.format(s, params));
    }

    public static DateFormat getDateFormatter() {
        return _dateFormat;
    }

    public static String getUrlWithQueryParams(String url, List<Pair> params) {
        if (url == null) {
            return null;
        }

        url = url.replace(" ", "%20");

        if(!url.endsWith("?"))
            url += "?";

        if (params != null && params.size() > 0) {
            String paramString = format(params, "utf-8");
            url += paramString;
        }
        return url;
    }


    public static String format (final List <? extends Pair> parameters,
            final String encoding) {
        final StringBuilder result = new StringBuilder();
        for (final Pair parameter : parameters) {
            final String encodedName = encode(String.valueOf(parameter.first), encoding);
            final String value = String.valueOf(parameter.second);
            final String encodedValue = value != null ? encode(value, encoding) : "";
            if (result.length() > 0)
                result.append(PARAMETER_SEPARATOR);
            result.append(encodedName);
            result.append(NAME_VALUE_SEPARATOR);
            result.append(encodedValue);
        }
        return result.toString();
    }


    private static String encode (final String content, final String encoding) {
        try {
            return URLEncoder.encode(content,
                    encoding != null ? encoding : DEFAULT_CONTENT_CHARSET);
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }

    public static String sha1Hex(byte[] byteArray) {

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
        byte[] encoded = messageDigest.digest(byteArray);
        StringBuilder builder = new StringBuilder();
        for (byte b : encoded) {
            builder.append(Integer.toHexString((b & 0xf0) >> 4));
            builder.append(Integer.toHexString(b & 0x0f));
        }
        return builder.toString();
    }
}
