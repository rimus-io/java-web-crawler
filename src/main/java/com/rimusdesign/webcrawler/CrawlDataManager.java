package com.rimusdesign.webcrawler;


import com.rimusdesign.webcrawler.model.Page;
import com.rimusdesign.webcrawler.model.PageState;
import com.rimusdesign.webcrawler.utils.CommonUtils;
import lombok.Getter;
import org.apache.logging.log4j.CloseableThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author Rimas Krivickas.
 */
public class CrawlDataManager implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(CrawlDataManager.class);

    private Repository repository;
    private LinkedBlockingQueue<String> urlQueue;
    private LinkedBlockingQueue<Page> parsedDataQueue;

    @Getter
    private boolean isDone;


    public CrawlDataManager (Repository repository, LinkedBlockingQueue<String> urlQueue, LinkedBlockingQueue<Page> parsedDataQueue) {

        this.repository = repository;
        this.urlQueue = urlQueue;
        this.parsedDataQueue = parsedDataQueue;
    }


    @Override
    public void run () {

        isDone = false;
        Page page;

        try (@SuppressWarnings("unused") final CloseableThreadContext.Instance ctc = CloseableThreadContext
                .put("uuid", CommonUtils.shortUUID())
        ) {


            while (!isDone) {
                try {
                    page = parsedDataQueue.take();
                    handlePageData(page);
                } catch (InterruptedException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            log.debug("Terminating thread");
        }
    }


    private void handlePageData (Page page) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        if (page.isHtml()) {

            Page pendingPage;
            for (String url : page.getInternalUrls()) {

                // Instantiate pending page
                pendingPage = new Page(url);

                if (!repository.contains(pendingPage.getId())) {

                    repository.save(pendingPage);

                    // Add URL to the fetch queue
                    urlQueue.add(pendingPage.getUrl());
                }
            }
        }

        // Update status, and override pending page with parsed one
        page.setState(PageState.READY);
        repository.save(page);

        if (!repository.hasPendingItems()) {

            log.info("Crawl complete");
            isDone = true;
        }
    }
}
