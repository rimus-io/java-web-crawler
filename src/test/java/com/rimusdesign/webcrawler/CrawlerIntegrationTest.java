package com.rimusdesign.webcrawler;


import com.rimusdesign.webcrawler.model.Page;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * NOTE: In a real world situation it is a good idea to run
 * integration tests before crawling as part of automated
 * process. This helps to prevent data corruption in case
 * selectors used to parse data have been modified, etc.
 *
 * @author Rimas Krivickas.
 */
public class CrawlerIntegrationTest {


    public static final String TEST_URL = "http://localhost:8080";
    public static final String[] URLS_IN_TEST = new String[]{
            TEST_URL,
            TEST_URL + "/video_example.html",
            TEST_URL + "/missing",
            TEST_URL + "/plugin_object_example.html",
            TEST_URL + "/static/tile.png.zip",
            TEST_URL + "/audio_example.html",
            TEST_URL + "/iframe_example.html",
            TEST_URL + "/plugin_embed_example.html",
            TEST_URL + "/plugin_examples.html"
    };

    private static Crawler crawler;


    @BeforeClass
    public static void setUpClass () throws Exception {

        crawler = new Crawler(5);

        crawler.crawl(TEST_URL);
    }


    @Test
    public void testCrawlPaths () throws Exception {

        String[] urlsExpected = URLS_IN_TEST;
        Arrays.sort(urlsExpected);

        String[] urlsFound = crawler.getPages().stream().map(Page::getUrl).toArray(String[]::new);
        Arrays.sort(urlsFound);

        assertArrayEquals("Should only crawl the paths that match expected list", urlsExpected, urlsFound);
    }


    @Test
    public void testParseHTMLOnly () throws Exception {

        assertEquals("Page list should contain exactly seven items with HTML MIME type",
                7, crawler.getPages().stream().filter(Page::isHtml).count());

        assertEquals("Two fetched items should have not been parsed, hence raw data value should be not set",
                2, crawler.getPages().stream().filter(item -> item.getRawData() == null).count());
    }


    @Test
    public void testStatusCodes () throws Exception {

        assertEquals("One URL should lead to a missing page (404)",
                1, crawler.getPages().stream().filter(item -> item.getStatusCode() == 404).count());

        assertEquals("Seven items should have been returned correctly (200)",
                7, crawler.getPages().stream().filter(item -> item.getStatusCode() == 200).count());

        assertEquals("One item should have no status code set as it's MIME type is not HTML",
                1, crawler.getPages().stream().filter(item -> item.getStatusCode() == 0).count());
    }
}