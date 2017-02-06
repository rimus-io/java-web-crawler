package com.rimusdesign.webcrawler.model;


import com.rimusdesign.webcrawler.utils.CommonUtils;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * An entity to hold the fetched and parsed data.
 *
 * @author Rimas Krivickas.
 */
@Data
public class Page {


    private final String id;
    private final String url;
    private int statusCode;
    private String mimeType;
    private String rawData;
    private String title;
    private HashMap<StaticContentType, HashSet<String>> staticContentUrls;
    private HashSet<String> externalUrls;
    private HashSet<String> internalUrls;
    private PageState state;



    public Page (String url) {

        this.id = generateId(url); // Generate ID
        this.state = PageState.PENDING; // Set default state
        this.url = url;
    }


    public Set<String> getStaticItemsOfType (StaticContentType type) {

        return staticContentUrls.containsKey(type) ? staticContentUrls.get(type) : null;
    }


    public boolean isHtml () {

        return rawData != null;
    }

    public static String generateId (String url){
        return CommonUtils.getHash(url);
    }

}
