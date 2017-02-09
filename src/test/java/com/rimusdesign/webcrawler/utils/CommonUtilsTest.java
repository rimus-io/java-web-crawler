package com.rimusdesign.webcrawler.utils;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Rimas Krivickas.
 */
public class CommonUtilsTest {


    @Test(expected = NullPointerException.class)
    public void testGetHashNull () throws Exception {

        CommonUtils.getHash(null);
    }


    @Test
    public void testGetHash () throws Exception {

        assertEquals("Must have matching lengths", CommonUtils.getHash("abc").length(), CommonUtils.getHash("123456789").length());
        assertNotEquals("Must return different hashes", CommonUtils.getHash(""), CommonUtils.getHash(" "));
        assertNotEquals("Must return different hashes", CommonUtils.getHash("abc"), CommonUtils.getHash("Abc"));
        assertEquals("Must return identical hashes", CommonUtils.getHash("abc"), CommonUtils.getHash("abc"));
    }


    @Test
    public void testShortUUID () throws Exception {

        assertEquals("Must return a string 12 characters long", 12, CommonUtils.shortUUID().length());
        assertNotEquals("Must return different UUIDs", CommonUtils.shortUUID(), CommonUtils.shortUUID());
    }


    @Test(expected = NullPointerException.class)
    public void testStripDomainNull () throws Exception {

        CommonUtils.stripDomain(null);
    }


    @Test
    public void testStripDomain () throws Exception {

        assertEquals("Should correctly extract domain from ULR with 'http'",
                "my-domain.com", CommonUtils.stripDomain("http://my-domain.com/"));

        assertEquals("Should correctly extract domain from ULR with 'https'",
                "my-domain.com", CommonUtils.stripDomain("https://my-domain.com/"));

        assertEquals("Should correctly extract domain from ULR with 'www' sub-domain",
                "my-domain.com", CommonUtils.stripDomain("www.my-domain.com/"));

        assertEquals("Should correctly extract domain from ULR with port number",
                "my-domain.com", CommonUtils.stripDomain("www.my-domain.com:8080/abcd"));

        assertEquals("Should correctly extract domain from URI",
                "my-domain.com", CommonUtils.stripDomain("http://www.my-domain.com/items/123456"));

        assertEquals("Should correctly extract domain from ULR with parameters",
                "my-domain.com", CommonUtils.stripDomain("http://www.my-domain.com/items/123456?a=abc&b=123"));

        assertEquals("Should correctly extract domain from ULR with bookmark anchor",
                "my-domain.com", CommonUtils.stripDomain("http://www.my-domain.com/items/123456#anchor"));
    }


    @Test(expected = NullPointerException.class)
    public void testStripBookmarkNull () throws Exception {

        CommonUtils.stripBookmark(null);
    }


    @Test
    public void testStripBookmark () throws Exception {

        assertEquals("Should strip everything after, and including bookmark anchor",
                "http://www.my-domain.com", CommonUtils.stripBookmark("http://www.my-domain.com#anchor"));

        assertEquals("Should strip everything after, and including bookmark anchor",
                "http://www.my-domain.com/items/123456", CommonUtils.stripBookmark("http://www.my-domain.com/items/123456#anchor?queryString=123"));

        assertEquals("Should strip everything after, and including bookmark anchor",
                "http://www.my-domain.com?queryString=123", CommonUtils.stripBookmark("http://www.my-domain.com?queryString=123#anchor"));
    }


    @Test(expected = NullPointerException.class)
    public void testContainsUnsafeCharsNull () throws Exception {

        CommonUtils.containsUnsafeChars(null);
    }


    @Test
    public void testContainsUnsafeChars () throws Exception {

        assertTrue("Contains unsafe character '\"'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=\"abc"));

        assertTrue("Contains unsafe character '<'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=<abc"));

        assertTrue("Contains unsafe character '>'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=>abc"));

        assertTrue("Contains unsafe character '%'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=%abc"));

        assertTrue("Contains unsafe character '{'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q={abc"));

        assertTrue("Contains unsafe character '}'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=}abc"));

        assertTrue("Contains unsafe character '|'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=|abc"));

        assertTrue("Contains unsafe character '\\'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=\\abc"));

        assertTrue("Contains unsafe character '^'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=^abc"));

        assertTrue("Contains unsafe character '~'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=~abc"));

        assertTrue("Contains unsafe character '['",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=[abc"));

        assertTrue("Contains unsafe character ']'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=]abc"));

        assertTrue("Contains unsafe character '`'",
                CommonUtils.containsUnsafeChars("http://my-domain.com?q=`abc"));
    }


    @Test(expected = NullPointerException.class)
    public void testCleanUpURLNull () throws Exception {

        CommonUtils.cleanUpURL(null);
    }

    @Test
    public void testCleanUpURL () throws Exception {

        assertEquals("Empty URL should return empty string",
                "", CommonUtils.cleanUpURL(""));

        assertEquals("URL '/' should return an empty string",
                "", CommonUtils.cleanUpURL("/"));

        assertEquals("Bookmark anchor, and trailing slash should be stripped",
                "http://www.my-domain.com", CommonUtils.cleanUpURL("http://www.my-domain.com/#anchor"));
    }
}