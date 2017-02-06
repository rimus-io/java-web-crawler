package com.rimusdesign.webcrawler.parsing;


import com.rimusdesign.webcrawler.model.Page;
import com.rimusdesign.webcrawler.model.StaticContentType;
import com.rimusdesign.webcrawler.utils.CommonUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Rimas Krivickas.
 */
public class ParsingContextGenericTest {


    public static final String URL = "http://localhost";

    public static final String HTML_TITLE = "Parsing context title";
    public static final String HTML_VIDEO_SRC_1 = "video.mp4";
    public static final String HTML_VIDEO_SRC_2 = "video.ogg";
    public static final String HTML_AUDIO_SRC = "audio.mp3";
    public static final String HTML_IMAGE_SRC_1 = "internal_img.png";
    public static final String HTML_IMAGE_SRC_2 = "http://www.extern.com/img.jpg";
    public static final String HTML_IFRAME_SRC = "http://www.extern.com";
    public static final String HTML_INT_URL_1 = "/internal_one";
    public static final String HTML_INT_URL_2 = "/internal_two";
    public static final String HTML_EXT_URL_1 = "http://www.extern.com";
    public static final String HTML_EXT_URL_2 = "http://www.extern.com/two";
    public static final String HTML_EMBED_SRC = "embed.mdi";
    public static final String HTML_OBJECT_SRC = "object.swf";

    public static final String HTML = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>" + HTML_TITLE + "</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "\n" +
            "<h1>Test HTML</h1>\n" +
            "\n" +
            "<h2>Video</h2>\n" +
            "<video width=\"200\" controls>\n" +
            "    <source src=\"" + HTML_VIDEO_SRC_1 + "\" type=\"video/mp4\">\n" +
            "    <source src=\"" + HTML_VIDEO_SRC_2 + "\" type=\"video/ogg\">\n" +
            "</video>\n" +
            "\n" +
            "<h2>Audio</h2>\n" +
            "<audio controls=\"controls\">\n" +
            "    <source src=\"" + HTML_AUDIO_SRC + "\" />\n" +
            "</audio>\n" +
            "\n" +
            "<h2>Images</h2>\n" +
            "<img src=\"" + HTML_IMAGE_SRC_1 + "\">\n" +
            "<img src=\"" + HTML_IMAGE_SRC_2 + "\">\n" +
            "\n" +
            "<h2>IFrame</h2>\n" +
            "<iframe width=\"300\" height=\"200\" src=\"" + HTML_IFRAME_SRC + "\" frameborder=\"0\" allowfullscreen=\"\"></iframe>\n" +
            "\n" +
            "<h2>Internal URLs</h2>\n" +
            "<a href=\"" + HTML_INT_URL_1 + "\">Internal URL One</a>\n" +
            "<a href=\"" + HTML_INT_URL_1 + "\">Internal URL One Copy</a>\n" +
            "<a href=\"" + HTML_INT_URL_2 + "\">Internal URL Two</a>\n" +
            "\n" +
            "<h2>External URLs</h2>\n" +
            "<a href=\"" + HTML_EXT_URL_1 + "\">External URL One</a>\n" +
            "<a href=\"" + HTML_EXT_URL_2 + "\">External URL Two</a>\n" +
            "\n" +
            "<h2>Embed</h2>\n" +
            "<embed src=\"" + HTML_EMBED_SRC + "\" width=\"100\" height=\"100\"></embed>\n" +
            "\n" +
            "<h2>Object</h2>\n" +
            "<object data=\"" + HTML_OBJECT_SRC + "\" type=\"application/x-shockwave-flash\" width=\"100\" height=\"100\">\n" +
            "    <param name=\"quality\" value=\"high\"/>\n" +
            "    <param name=\"wmode\" value=\"transparent\"/>\n" +
            "</object>\n" +
            "\n" +
            "</body>\n" +
            "</html>";

    private static Page page;


    @BeforeClass
    public static void setUpClass () throws Exception {

        page = new ParsingContextGeneric().parse(URL, HTML);
    }


    @Test
    public void testParse () throws Exception {

        assertEquals("ID Should match a hash of provided URL value", CommonUtils.getHash(URL), page.getId());
        assertEquals("Raw HTML value should not be modified", HTML, page.getRawData());
        assertEquals("Title should be extracted correctly", HTML_TITLE, page.getTitle());
        assertEquals("URL should match", URL, page.getUrl());

        // Test external URLs
        assertTrue(page.getExternalUrls().contains(HTML_EXT_URL_1));
        assertTrue(page.getExternalUrls().contains(HTML_EXT_URL_2));
        assertEquals("Should contain only two external URLs", 2, page.getExternalUrls().size());

        // Test internal URLs
        assertTrue(page.getInternalUrls().contains(URL + HTML_INT_URL_1));
        assertTrue(page.getInternalUrls().contains(URL + HTML_INT_URL_2));
        assertEquals("Should contain only two internal URLs", 2, page.getInternalUrls().size());

        // Test static content
        assertEquals("Should contain eight static content items",
                8, (long) page.getStaticContentUrls().values().stream().map(HashSet::size).reduce(0, (prev, next) -> prev + next));

        Set<String> list;

        // Test static content (images)
        list = page.getStaticItemsOfType(StaticContentType.IMAGE);
        assertTrue(list.contains(HTML_IMAGE_SRC_1));
        assertTrue(list.contains(HTML_IMAGE_SRC_2));
        assertEquals("Should only contain two image sources", 2, list.size());

        // Test static content (audio)
        list = page.getStaticItemsOfType(StaticContentType.AUDIO);
        assertTrue(list.contains(HTML_AUDIO_SRC));
        assertEquals("Should only contain one audio source", 1, list.size());

        // Test static content (video)
        list = page.getStaticItemsOfType(StaticContentType.VIDEO);
        assertTrue(list.contains(HTML_VIDEO_SRC_1));
        assertTrue(list.contains(HTML_VIDEO_SRC_2));
        assertEquals("Should only contain two video sources", 2, list.size());

        // Test static content (plugin)
        list = page.getStaticItemsOfType(StaticContentType.PLUGIN);
        assertTrue(list.contains(HTML_EMBED_SRC));
        assertTrue(list.contains(HTML_OBJECT_SRC));
        assertEquals("Should only contain two plugin sources", 2, list.size());

        // Test static content (iframe)
        list = page.getStaticItemsOfType(StaticContentType.IFRAME);
        assertTrue(list.contains(HTML_IFRAME_SRC));
        assertEquals("Should only contain one iframe source", 1, list.size());

    }
}