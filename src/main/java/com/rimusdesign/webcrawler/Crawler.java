package com.rimusdesign.webcrawler;


import com.rimusdesign.webcrawler.fetching.Fetcher;
import com.rimusdesign.webcrawler.model.Page;
import com.rimusdesign.webcrawler.model.TransientPage;
import com.rimusdesign.webcrawler.parsing.Parser;
import com.rimusdesign.webcrawler.parsing.ParsingContextGeneric;
import com.rimusdesign.webcrawler.utils.CommonUtils;
import lombok.NonNull;
import org.apache.logging.log4j.CloseableThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Retrieves data from all the pages under the initial domain name.
 * Crawls entire website tree.
 *
 * @author Rimas Krivickas.
 */
public class Crawler {


    private static final Logger log = LoggerFactory.getLogger(Crawler.class);

    private Repository repository;
    private LinkedBlockingQueue<String> urlQueue;
    private LinkedBlockingQueue<TransientPage> fetchedDataQueue;
    private LinkedBlockingQueue<Page> parsedDataQueue;

    private CrawlDataManager crawlDataManager;

    private Thread dataManagerThread;
    private Thread parserThread;
    private Thread fetcherThread;

    private int numFetchers;


    /**
     * @param numFetchers number of threads to use for fetching data
     */
    public Crawler (int numFetchers) {

        this.numFetchers = numFetchers;
    }


    public Crawler () {

        // Use default number of fetchers
        this(1);
    }


    /**
     * Starts the crawling of provided domain.
     *
     * @param url root domain to be crawled
     * @throws Exception
     */
    public void crawl (@NonNull String url) throws Exception {

        log.debug("Crawling URL: " + url);

        repository = new Repository();
        urlQueue = new LinkedBlockingQueue<>();
        fetchedDataQueue = new LinkedBlockingQueue<>();
        parsedDataQueue = new LinkedBlockingQueue<>();

        crawlDataManager = new CrawlDataManager(repository, urlQueue, parsedDataQueue);

        // Persist initial page
        Page initialPage = new Page(url);
        repository.save(initialPage);

        // Pass initial URL to be fetched
        urlQueue.add(initialPage.getUrl());

        start();

    }


    /**
     * @return A set of fetched and parsed pages
     */
    public HashSet<Page> getPages () {

        return repository != null ? repository.getItems() : null;
    }


    private void startFetcher () {

        fetcherThread = new Thread(new Fetcher(numFetchers, urlQueue, fetchedDataQueue));
        fetcherThread.start();
    }


    private void startParser () {

        parserThread = new Thread(new Parser(new ParsingContextGeneric(), fetchedDataQueue, parsedDataQueue));
        parserThread.start();
    }


    private void startManager () {

        dataManagerThread = new Thread(crawlDataManager);
        dataManagerThread.start();
    }


    private boolean isRunning () {

        return crawlDataManager != null ? !crawlDataManager.isDone() : false;
    }


    private void start () throws InterruptedException {

        try (@SuppressWarnings("unused") final CloseableThreadContext.Instance ctc = CloseableThreadContext
                .put("uuid", CommonUtils.shortUUID())
        ) {


            startManager();
            startParser();
            startFetcher();

            while (isRunning()) {
                // Periodically check if parsing is done
                Thread.sleep(1);
            }

            fetcherThread.interrupt();
            parserThread.interrupt();

            log.info("END");
        }
    }
}
