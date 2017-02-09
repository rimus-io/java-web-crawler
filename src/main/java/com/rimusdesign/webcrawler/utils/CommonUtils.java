package com.rimusdesign.webcrawler.utils;


import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * Various utilities.
 *
 * @author Rimas Krivickas.
 */
public class CommonUtils {


    private static final Logger log = LoggerFactory.getLogger(CommonUtils.class);


    /**
     * A simple string hashing util.
     *
     * @param s string to be hashed
     *
     * @return fixed length SHA-256 hash string value of string provided
     */
    public static String getHash (@NonNull String s) {

        MessageDigest messageDigest;

        try {

            // Get digest
            messageDigest = MessageDigest.getInstance("SHA-256");

            // Pass data, also ensure that encoding is consistent
            messageDigest.update(s.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {

            // Log error, and return
            log.error(e.getMessage());
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        // Compose hash string
        for (byte b : messageDigest.digest()) {
            stringBuilder.append(String.format("%02x", b));
        }

        return stringBuilder.toString();
    }


    /**
     * Generates a random, 12 characters long, UUID string.
     *
     * @return generated value
     */
    public static String shortUUID(){

        byte[] randomBytes = new byte[9];
        new SecureRandom().nextBytes(randomBytes);

        return DatatypeConverter.printBase64Binary(randomBytes);
    }


    /**
     * Strips path, scheme (http, https) and port from URL.
     *
     * @param url a URL
     *
     * @return domain name of provided URL
     */
    public static String stripDomain (@NonNull String url) {

        return url.replace("http://", "").replace("https://", "").replace("www.", "").split("/")[0].split(":")[0];
    }


    /**
     * Removes bookmark from URL.
     * All characters after '#' get stripped away.
     *
     * @param url value to be stripped of bookmark
     *
     * @return URL without bookmark
     */
    public static String stripBookmark (@NonNull String url) {

        return url.split("#")[0];
    }


    /**
     * Cleans up URL. Removes bookmark anchor and trailing slash.
     *
     * @param url value
     * @return Clean URL
     */
    public static String cleanUpURL (@NonNull String url ) {
        String cleanUrl = stripBookmark(url);
        return cleanUrl.endsWith("/") ? cleanUrl.substring(0,cleanUrl.length()-1) : cleanUrl;
    }


    /**
     * Checks if provided URL contains unsafe characters.
     * Reference: https://perishablepress.com/stop-using-unsafe-characters-in-urls
     *
     * @param url value to check
     * @return 'true' if unsafe characters are found
     */
    public static boolean containsUnsafeChars (@NonNull String url) {

        // Capture initial URL length
        int initLen = url.length();

        // Length will changed after stripping unsafe characters
        return url.replaceAll("[\"<>#%{}|\\\\^~\\[\\]`]+", "").length() != initLen;
    }
}
