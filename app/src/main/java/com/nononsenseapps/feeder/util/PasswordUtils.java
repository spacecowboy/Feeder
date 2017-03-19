package com.nononsenseapps.feeder.util;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Some password utilities
 */
public class PasswordUtils {

    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    // Intention is not uber-security, but to prevent trivial password extraction by root-apps
    // in case you are reusing this password elsewhere. You should still use a unique password...
    private final static String ANDROID_SALT = "4fb3a4355d7bfed240015f8e51e7b42f3455c17e";


    /**
     *
     * @param username of user
     * @param password actually the salted hash of the password
     * @return a BASE64 encoded "username:password" string preceded by "Basic "
     */
    public static String getBase64BasicHeader(final String username, final String password) {
        if (username == null || password == null) {
           throw new NullPointerException("Username or password was null");
        }
        final byte[] base;
        try {
            base = (username + ":" + password).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("PasswordUtils", e.getLocalizedMessage());
            return null;
        }
        return "Basic " + Base64.encodeToString(base, Base64.NO_WRAP);
    }

    /**
     * Convert a plaintext password into a salted hash. The intention being that apps with
     * root-access can't trivially extract your password and then use that to log into your
     * google account or whatever.
     *
     * You should still be using a unique password!
     *
     * @param password the plaintext password
     * @return a hash
     */
    public static String getSaltedHashedPassword(final String password) {
        return sha1Hash(ANDROID_SALT + password);
    }

    // http://stackoverflow.com/questions/5980658/how-to-sha1-hash-a-string-in-android
    private static String sha1Hash(String toHash) {
        String hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return hash;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
