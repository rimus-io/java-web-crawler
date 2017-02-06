package com.rimusdesign.webcrawler.parsing;


import com.rimusdesign.webcrawler.model.Page;
import com.rimusdesign.webcrawler.model.TransientPage;
import com.rimusdesign.webcrawler.utils.CommonUtils;
import org.apache.logging.log4j.CloseableThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;


/**
 * Takes data from input queue, parses it within provided context,
 * and forwards the resulting {@link Page} instances to the output queue.
 *
 * @author Rimas Krivickas.
 */
public class Parser implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(Parser.class);

    private ParsingContext context;
    private LinkedBlockingQueue<TransientPage> fetchedDataQueue;
    private LinkedBlockingQueue<Page> parsedDataQueue;


    /**
     * @param context          an instance of parsing context, see {@link ParsingContext}
     * @param fetchedDataQueue input queue for receiving {@link TransientPage} objects for parsing
     * @param parsedDataQueue  output queue to forward parsed {@link Page} objects
     */
    public Parser (ParsingContext context, LinkedBlockingQueue<TransientPage> fetchedDataQueue, LinkedBlockingQueue<Page> parsedDataQueue) {

        this.context = context;
        this.fetchedDataQueue = fetchedDataQueue;
        this.parsedDataQueue = parsedDataQueue;
    }


    @Override
    public void run () {

        try (@SuppressWarnings("unused") final CloseableThreadContext.Instance ctc = CloseableThreadContext
                .put("uuid", CommonUtils.shortUUID())
        ) {

            log.debug("Thread started");

            TransientPage data;
            Page page;

            try {

                // Retrieve item from input queue
                while ((data = fetchedDataQueue.take()) != null) {

                    log.debug("Received data, URL: " + data.getUrl()+", MIME: " + data.getMimeType() );
                    // Parse only if contains HTML data
                    if (data.getHtml() != null && !data.getHtml().isEmpty() && data.getMimeType() != null && data.getMimeType().contains("text/html")) {

                        // Get parsed page from provided context
                        page = context.parse(data.getUrl(), data.getHtml());
                    } else {

                        // Create empty page if there's no HTML to parse
                        page = new Page(data.getUrl());
                    }

                    // Set status code
                    page.setStatusCode(data.getStatusCode());

                    // Set MIME type
                    page.setMimeType(data.getMimeType());

                    // Forward page to data manager
                    log.debug("Adding 'Page' object to output queue, URL: " + data.getUrl());
                    parsedDataQueue.add(page);
                }
            } catch (InterruptedException e) {

                // Nothing to do
                log.debug("Terminating thread");
            }
        }
    }
}
