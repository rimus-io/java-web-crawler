package com.rimusdesign.webcrawler.fetching;


import com.rimusdesign.webcrawler.model.TransientPage;
import com.rimusdesign.webcrawler.utils.CommonUtils;
import org.apache.logging.log4j.CloseableThreadContext;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Fetcher is responsible for retrieving URLs from input queue,
 * retrieving data, and passing it to the output queue for parsing.
 *
 * @author Rimas Krivickas.
 */
public class Fetcher implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(Fetcher.class);

    private final int numFetchers;
    private LinkedBlockingQueue<String> urlQueue;
    private LinkedBlockingQueue<TransientPage> fetchedDataQueue;


    /**
     * @param numFetchers      number of fetcher threads to be used
     * @param urlQueue         input queue for retrieving URLs
     * @param fetchedDataQueue output queue
     */
    public Fetcher (int numFetchers, LinkedBlockingQueue<String> urlQueue, LinkedBlockingQueue<TransientPage> fetchedDataQueue) {

        this.numFetchers = numFetchers;
        this.urlQueue = urlQueue;
        this.fetchedDataQueue = fetchedDataQueue;
    }


    @Override
    public void run () {

        try (@SuppressWarnings("unused") final CloseableThreadContext.Instance ctc = CloseableThreadContext
                .put("uuid", CommonUtils.shortUUID())
        ) {

            log.debug("Thread started");

            String url;

            // Instantiate thread pool
            ExecutorService executorService = Executors.newFixedThreadPool(numFetchers);

            try {

                // Take URLs from the input queue
                while ((url = urlQueue.take()) != null) {

                    log.debug("Received URL: " + url);

                    // Fetch data
                    executorService.execute(new FetcherRunnable(url, fetchedDataQueue));

                    // TODO add pause
//                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {

                log.debug("Thread terminating");
            } finally {

                // Ensure all threads terminate
                log.debug("Shutting down fetcher thread pool");

                executorService.shutdown();
            }
        }
    }
}


/**
 * A thread used by fetcher thread pool.
 * <p>
 * This is where actual HTTP requests are made, data retrieved,
 * and results are passed to the output queue.
 */
class FetcherRunnable implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(FetcherRunnable.class);

    private String url;
    private LinkedBlockingQueue<TransientPage> fetchedDataQueue;


    /**
     * @param url              URL from which to fetch data
     * @param fetchedDataQueue queue to write fetched data to
     */
    public FetcherRunnable (String url, LinkedBlockingQueue<TransientPage> fetchedDataQueue) {

        this.url = url;
        this.fetchedDataQueue = fetchedDataQueue;
    }


    @Override
    public void run () {

        try (@SuppressWarnings("unused") final CloseableThreadContext.Instance ctc = CloseableThreadContext
                .put("uuid", CommonUtils.shortUUID())
                .put("url", url)
        ) {

            log.debug("Thread started");

            // Set initial values
            String mimeType = null;
            String html = null;
            int statusCode = 0;

            try {

                // Get the HTTP response
                Connection.Response response = Jsoup.connect(url).method(Connection.Method.GET).execute();

                // Set status code
                statusCode = response.statusCode();

                // Set MIME type
                mimeType = response.contentType();

                // Parse document and set HTML
                html = response.parse().html();
            } catch (HttpStatusException e) {

                // Set status code
                statusCode = e.getStatusCode();

                log.debug("Failed to fetch data. HTTP status code: " + e.getStatusCode());
            } catch (UnsupportedMimeTypeException e) {

                // Non HTML data, nothing to do
                log.debug("Non HTML resource. MIME type: " + e.getMimeType());
            } catch (IOException e) {

                log.error(e.getMessage());
            }


            // Forward data for parsing
            fetchedDataQueue.add(new TransientPage(url, mimeType, statusCode, html));

            log.debug("Forwarded data for parsing. Data fetched from URL: " + url);

            log.debug("Thread terminating");
        }
    }
}