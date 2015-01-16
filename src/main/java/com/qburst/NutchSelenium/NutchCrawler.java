package com.qburst.NutchSelenium;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.qburst.NutchSelenium.configuration.Constants;
import com.qburst.NutchSelenium.mongodbutils.DbFunctions;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Nutch crawler main class
 */
public class NutchCrawler {

    private static DbFunctions mongodb;
    private static Logger logger = LoggerFactory.getLogger(NutchCrawler.class);

    public static void main(String args[]) {
        try {
            MongoDbConfig mongoConf = MongoDbConfig.getMongoDbInstance();
            mongoConf.setCollection();
            mongoConf.setDocument();
            mongodb = mongoConf.getDatabaseObj();
            DBCollection crawlConfig = mongoConf.getCollection();

            List<DBObject> docs = mongodb.getAllDoc(crawlConfig);

            long startTime = System.currentTimeMillis();
            if(docs.size() > 0) {
                Path[] segment;
                for (DBObject doc : docs) {
                    CrawlFunctions crawlFunctions = new CrawlFunctions(doc);
                    crawlFunctions.setup(doc);
                    Configuration conf = CrawlFunctions.conf;
                    crawlFunctions.injectURL(conf);

                    int depth = (Integer) doc.get(Constants.DEPTH_KEY);
                    logger.info("Depth of crawl : " + depth);
                    for (int i = 1; i <= depth; i++) {
                        segment = crawlFunctions.generateSegment(conf);
                        Thread.sleep(5000);
                        crawlFunctions.fetchURL(conf, segment);
                        crawlFunctions.addLinkDB(conf, segment);
                    }
                }
            } else {
                logger.warn("No documents in MongoDB to crawl. Crawler stopped");
            }
            long endTime = System.currentTimeMillis();
            long time = endTime - startTime;
            logger.info("Total time for crawling is "+time+" milliseconds");
        }catch(IOException | InterruptedException e) {
            logger.warn(e.getMessage());
        } finally {
            mongodb.dbDisconnect();
        }
    }

}
