package com.rimusdesign.webcrawler.model;


import lombok.Data;


/**
 * A trancient value object to be used for passing data
 * between fetcher and parser threads.
 *
 * @author Rimas Krivickas.
 */
@Data
public class TransientPage {


    private final String url;
    private final String mimeType;
    private final int statusCode;
    private final String html;

}
