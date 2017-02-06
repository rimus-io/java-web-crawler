package com.rimusdesign.webcrawler.parsing;


import com.rimusdesign.webcrawler.model.Page;


/**
 * Defines required functionality for parsing within context.
 * Different implementations can be created for different
 * websites if, for example, there's a need to extract data
 * by HTML tag class names etc.
 *
 * @author Rimas Krivickas.
 */
public interface ParsingContext {


    /**
     * Generates an instance of {@link Page}, and populates all data
     * from provided HTML.
     * If provided data is 'null', or is not HTML, method must return an
     * instance of {@link Page}, but no data should be populated.
     *
     * @param url  URL from which data has been fetched
     * @param html HTML data
     * @return instance of {@link Page}
     */
    Page parse (String url, String html);

}
