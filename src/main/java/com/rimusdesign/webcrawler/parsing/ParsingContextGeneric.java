package com.rimusdesign.webcrawler.parsing;


import com.rimusdesign.webcrawler.model.Page;
import com.rimusdesign.webcrawler.model.StaticContentType;
import com.rimusdesign.webcrawler.utils.CommonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;


/**
 * A generic implementation of {@link ParsingContext}.
 * Uses default tag and attribute names.
 *
 * @author Rimas Krivickas.
 */
public class ParsingContextGeneric implements ParsingContext {


    private static final Logger log = LoggerFactory.getLogger(ParsingContextGeneric.class);


    /**
     * {@inheritDoc}
     */
    @Override
    public Page parse (String url, String html) {

        // Create new Page
        Page page = new Page(url);

        // If not an HTML resource, return without processing
        if (html == null || html.isEmpty()) {

            log.debug("Not an HTML, return without parsing. For URL: " + url);
            return page;
        }

        // Generate document instance from provided html
        Document document = Jsoup.parse(html, url);

        // Set raw data
        page.setRawData(html);

        // Set title
        page.setTitle(document.title());

        // Extract and set static content
        handleStaticContent(document, page);

        // Extract and set links
        handleLinks(document, page);

        return page;
    }


    /**
     * Applies static content details extracted from {@link Document}
     * to provided {@link Page} instance.
     *
     * @param document instance from which to extract data
     * @param page     instance to set data for
     */
    private void handleStaticContent (Document document, Page page) {

        // Instantiate static item map if needed
        HashMap<StaticContentType, HashSet<String>> items = page.getStaticContentUrls() == null
                ? new HashMap<>() : page.getStaticContentUrls();

        // Set/reset item list to avoid null pointer exceptions
        page.setStaticContentUrls(items);

        // Extract and set images
        addStaticItems(StaticContentType.IMAGE, items,
                extractStaticItems(document, "img", "src"));

        // Extract and set audio
        addStaticItems(StaticContentType.AUDIO, items,
                extractStaticItems(document, "source", "src", "audio"));

        // Extract and set video
        addStaticItems(StaticContentType.VIDEO, items,
                extractStaticItems(document, "source", "src", "video"));

        // Extract and set plug-ins
        addStaticItems(StaticContentType.PLUGIN, items,
                extractStaticItems(document, "object", "data"));
        addStaticItems(StaticContentType.PLUGIN, items,
                extractStaticItems(document, "embed", "src"));

        // Extract and set iframes
        addStaticItems(StaticContentType.IFRAME, items,
                extractStaticItems(document, "iframe", "src"));
    }


    /**
     * Extracts the value of an attribute from specific tag.
     *
     * @param document instance to extract data from
     * @param tag      name of tag
     * @param srcAttr  name of attribute
     *
     * @return a set of attribute values
     */
    private HashSet<String> extractStaticItems (Document document,
                                                String tag,
                                                String srcAttr) {

        return this.extractStaticItems(document, tag, srcAttr, null);
    }


    /**
     * Extracts the value of an attribute from specific tag.
     *
     * @param document  instance to extract data from
     * @param tag       name of tag
     * @param srcAttr   name of attribute
     * @param parentTag name of parent tag of provided 'tag' parameter.
     *                  If provided, will extract only those attribute
     *                  values that are nested within specified
     *                  parent tag
     *
     * @return a set of attribute values, or 'null' if no values are found
     */
    private HashSet<String> extractStaticItems (Document document,
                                                String tag,
                                                String srcAttr,
                                                String parentTag) {

        // Get top level elements
        Elements topElements = document.body().getElementsByTag(parentTag == null ? tag : parentTag);

        // If no elements found return 'null'
        if (topElements == null || topElements.size() == 0) return null;

        // Declare/instantiate variables
        Elements childElements;
        String src;
        HashSet<String> set = new HashSet<>();

        // Extract values
        for (Element topElement : topElements) {

            if (parentTag == null || parentTag.isEmpty()) {

                // Get attribute value from non-nested tag
                src = topElement.attr(srcAttr);

                // If valid, add to set
                if (src != null && !src.isEmpty()) set.add(src);
            } else {

                // Get nested tags
                childElements = topElement.getElementsByTag(tag);

                for (Element childElement : childElements) {

                    // Get attribute value from nested tag
                    src = childElement.attr(srcAttr);

                    // If valid, add to set
                    if (src != null && !src.isEmpty()) set.add(src);
                }
            }
        }

        // If no values are found return 'null', otherwise return items
        return set.size() > 0 ? set : null;
    }


    /**
     * Adds items from source to target under specified {@link StaticContentType}.
     *
     * @param type   to use as a key for data access
     * @param target object to which the data will be added
     * @param source items to be added
     */
    private void addStaticItems (StaticContentType type,
                                 HashMap<StaticContentType, HashSet<String>> target,
                                 HashSet<String> source) {

        // Return is there are no items to add
        if (source == null || source.isEmpty()) return;

        if (!target.containsKey(type)) {

            // If target doesn't have data under provided key, set key and add data
            target.put(type, source);
        } else {

            // If target has data under provided key, append data
            target.get(type).addAll(source);
        }
    }


    /**
     * Sets internal/external URLs extracted from {@link Document} instance
     * to {@link Page} instance provided.
     *
     * @param document instance from which to extract data
     * @param page     instance to set data for
     */
    private void handleLinks (Document document, Page page) {

        // Instantiate URL sets if needed
        if (page.getInternalUrls() == null) page.setInternalUrls(new HashSet<>());
        if (page.getExternalUrls() == null) page.setExternalUrls(new HashSet<>());

        // Extract all links
        Elements elements = document.body().getElementsByTag("a");

        String url;

        for (Element element : elements) {

            // Extract URL from tag
            url = element.absUrl("href");

            // Proceed only if URL is usable
            if (url != null &&
                    !url.isEmpty() &&
                    !CommonUtils.containsUnsafeChars(url)) {

                // Clean up URL
                url = CommonUtils.cleanUpURL(url);

                // Add URL to relevant list
                if (isExternal(url, document.baseUri())) {

                    page.getExternalUrls().add(url);
                } else {

                    page.getInternalUrls().add(url);
                }
            }
        }
    }


    /**
     * A util for determining if provided URL is external
     * in regards to base URL.
     *
     * @param url     value to be validated
     * @param baseUrl value to be validated against
     *
     * @return 'false' if domain name of provided 'url' belongs to
     * domain of 'baseUrl'
     */
    private boolean isExternal (String url, String baseUrl) {

        return !CommonUtils.stripDomain(url).contains(CommonUtils.stripDomain(baseUrl));
    }

}
