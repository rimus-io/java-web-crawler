import com.rimusdesign.webcrawler.Crawler;
import com.rimusdesign.webcrawler.model.Page;
import com.rimusdesign.webcrawler.utils.CommonUtils;
import org.apache.logging.log4j.CloseableThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Rimas Krivickas.
 */
public class Application {


    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static final String URL = "http://127.0.0.1:8080";


    public static void main (String[] args) {
        try (@SuppressWarnings("unused") final CloseableThreadContext.Instance ctc = CloseableThreadContext
                .put("uuid", CommonUtils.shortUUID())
        ) {

            // Instantiate crawler
            Crawler crawler = new Crawler(5);


            // Execute crawler A
            try {
                crawler.crawl(URL);
            } catch (Exception e) {
                log.error(e.getMessage());
            }


            log.info("Pages visited: " + crawler.getPages().size());

            for (Page page : crawler.getPages()){
                log.info("\n\nID: " + page.getId()
                        + "\nURL: " + page.getUrl()
                        + "\nMIME: " + page.getMimeType()
                        + "\nSTATUS: " + page.getStatusCode()
                        + "\nTitle: " + page.getTitle() + "\n");
            }

        }
    }

}
