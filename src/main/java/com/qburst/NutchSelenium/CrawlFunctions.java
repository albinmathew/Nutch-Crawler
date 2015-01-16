package com.qburst.NutchSelenium;


import com.mongodb.DBObject;
import com.qburst.NutchSelenium.configuration.Constants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.crawl.Generator;
import org.apache.nutch.crawl.Injector;
import org.apache.nutch.crawl.LinkDb;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.parse.*;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.xerces.dom.DocumentFragmentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Crawler functions
 */
public class CrawlFunctions extends NutchCrawler {
    public static Configuration conf;
    private FileSystem fs;
    private Path dirPath;
    private Path urlPath;
    private Path crawldbPath;
    private Path segmentDirPath;
    private Path linkDbPath;

    private int fileNum = 1;
    private Logger logger = LoggerFactory.getLogger(CrawlFunctions.class);
    private DBObject dbObject;

    public CrawlFunctions(DBObject obj) {
        init(obj);
    }

    public void init(DBObject docObject) {
        String urlDir = Constants.URL_DIR;
        String crawlDbDir = Constants.CRAWLDB_DIR;
        String segmentDir = Constants.SEGMENT_DIR;
        String storageDir = (String) docObject.get(Constants.STORAGE_DIR);
        String linkDbDir = "linkdb";
        logger.info("Storage directory path " + storageDir);
        dirPath = new Path(storageDir);
        urlPath = new Path(dirPath, urlDir);
        crawldbPath = new Path(dirPath, crawlDbDir);
        segmentDirPath = new Path(dirPath, segmentDir);
        linkDbPath = new Path(dirPath,linkDbDir);
        dbObject = docObject;
    }

    public void setup(DBObject docObj) throws IOException {
        conf = NutchConfiguration.create();
        setNutchConfiguration();
        fs = FileSystem.get(conf);
        fs.delete(dirPath, true);
        fs.mkdirs(dirPath);

        fs.mkdirs(urlPath);
        String url;

        //check for http proxy settings
        checkProxyConf();

        String outputFile = (String) docObj.get(Constants.OUTPUT_FILE);
        logger.info(outputFile);
        File file = new File(outputFile);
        if (file.exists()) {
            file.delete();
        }

        String linkUrlFileName = (String) docObj.get(Constants.OUTLINK_FILE);
        File linkUrlFile = new File(linkUrlFileName);
        if (linkUrlFile.exists()) {
            linkUrlFile.delete();
        }
        url = (String) docObj.get(Constants.SITE_URL);
        addUrl(url);
    }

    public void setNutchConfiguration() throws IOException {
        conf.set(Constants.HTTP_AGENT_NAME, "NutchCrawler");
//        conf.set(Constants.HTTP_PROXY_HOST, "localhost");
//        conf.setInt(Constants.HTTP_PROXY_PORT, 8123);

        conf.set(Constants.PLUGIN_FOLDERS, "plugins");
        conf.set(Constants.PLUGIN_INCLUDES, "protocol-http|urlfilter-regex|parse-(html|tika)|index-(basic|anchor)|indexer-solr|scoring-opic|urlnormalizer-(pass|regex|basic)");
        conf.setBoolean(Constants.PLUGIN_AUTO_ACTIVATION, true);
        this.addURLRegex();
    }

    public void checkProxyConf() {
        String httpProxyPort = conf.get(Constants.HTTP_PROXY_PORT);
        logger.info(httpProxyPort);

        if (httpProxyPort != null && !httpProxyPort.isEmpty()) {
            logger.info("HTTP proxy configred for Tor");
        }
    }

    //Add URL to input file
    public void addUrl(String url) throws IOException {
        String seedFileName = Constants.SEED_FILE;
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        File seedFile;
        try {
            seedFile = new File(urlPath.toString(), seedFileName);
            seedFile.createNewFile();
            fileWriter = new FileWriter(seedFile, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(url);
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null && fileWriter != null) {
                bufferedWriter.close();
                fileWriter.close();
            }
        }
    }

    //Inject URL to crawlDB
    public void injectURL(Configuration conf) throws IOException {
        Injector injector = new Injector(conf);
        if(crawldbPath !=null && urlPath !=null) {
            injector.inject(crawldbPath, urlPath);
        }
    }

    //Generate fetch lists into segment
    public Path[] generateSegment(Configuration conf) throws IOException {
        long currentTime = System.currentTimeMillis();
        Generator generator = new Generator(conf);
        return generator.generate(crawldbPath, segmentDirPath, 2, (Integer) dbObject.get(Constants.OUTLINK_URL_NUM), currentTime);
    }

    //Perform Fetch of URLs from fetch list
    public void fetchURL(Configuration conf, Path[] segment) throws IOException {
        Fetcher fetcher = new Fetcher(conf);
        if (segment != null) {
            for (Path aSegment : segment) {
                int num_threads = 3;
                fetcher.fetch(aSegment, num_threads);

                ParseSegment parseSegment = new ParseSegment(conf);
                parseSegment.parse(aSegment);

                contentParse(conf, aSegment);
                getOutLinks(conf, aSegment);
                parseForXPath(conf, aSegment);
            }
            updateDB(conf, segment);
        } else {
            logger.info("No segments to fetch");
        }
    }

    public void addLinkDB(Configuration conf, Path[] segment) throws IOException {
        if(conf !=null && segment !=null) {
            LinkDb linkDb = new LinkDb(conf);
            linkDb.setConf(conf);
            linkDb.invert(linkDbPath, segment, true, true, false);
        }
    }


    public void contentParse(final Configuration conf, Path segment) throws IOException {
        String contentFile = Constants.REDUCER_OUTPUT_FILE;
        File htmlFile;
        FileWriter fw = null;
        BufferedWriter bw = null;
        Path content = new Path(new Path(segment, Content.DIR_NAME), contentFile);
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, content, conf);
        Text key = new Text();
        Content HtmlContent = new Content();
        try {
            while (reader.next(key, HtmlContent)) {
                htmlFile = new File(dirPath.toString(), URLEncoder.encode(key.toString()
                                .replace("http://www.programcreek.com/java-api-examples/index.php?api=", "")
                                .replace("http://www.programcreek.com/java-api-examples/?action=index","index")
                                .concat(".html"),
                        "UTF-8"));
                fileNum++;
                htmlFile.createNewFile();
                fw = new FileWriter(htmlFile);
                bw = new BufferedWriter(fw);
                bw.write(new String(HtmlContent.getContent()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                bw.close();
                if (fw != null) {
                    fw.close();
                }
                reader.close();
            }
        }
    }

    public void parseForXPath(Configuration conf, Path segment) throws IOException {
        String contentFile = Constants.REDUCER_OUTPUT_FILE;
        Path parseContent = new Path(new Path(segment, Content.DIR_NAME), contentFile);
        Text key = new Text();
        Content parseText = new Content();
        ParseHTMLContent htmlContent = new ParseHTMLContent();
        try (SequenceFile.Reader parseReader = new SequenceFile.Reader(fs, parseContent, conf)) {
            while (parseReader.next(key, parseText)) {
                HTMLMetaTags metaTags = new HTMLMetaTags();
                DocumentFragment doc = new DocumentFragmentImpl();
                ParseUtil pUtil = new ParseUtil(conf);
                ParseResult pResult = pUtil.parse(parseText);
                try {
                    htmlContent.setConf(conf);
                    htmlContent.setDbObject(dbObject);
                    htmlContent.filter(parseText, pResult, metaTags, doc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            parseReader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    public void getOutLinks(Configuration conf, Path segment) throws IOException {
        String fileName = Constants.REDUCER_OUTPUT_FILE;
        Path content = new Path(new Path(segment, ParseData.DIR_NAME), fileName);

        Text key = new Text();
        ParseData outLinkContent = new ParseData();
        try (SequenceFile.Reader reader = new SequenceFile.Reader(fs, content, conf)) {
            while (reader.next(key, outLinkContent)) {
                String outlinkFile = (String) dbObject.get(Constants.OUTLINK_FILE);
                File outLinkUrlsFile = new File(outlinkFile);
                Outlink[] outLinks = outLinkContent.getOutlinks();
                for (Outlink outLink : outLinks) {
                    String outLinkUrl = outLink.getToUrl();
                    writeToFile(outLinkUrl, outLinkUrlsFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDB(Configuration conf, Path[] segments) throws IOException {
        CrawlDb db = new CrawlDb(conf);
        if(crawldbPath !=null && segments !=null) {
            db.update(crawldbPath, segments, false, true);
        }
    }

    public void writeToFile(String value, File file) throws IOException {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(value);
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null && fileWriter != null) {
                bufferedWriter.close();
                fileWriter.close();
            }
        }
    }

    public void addUrlFilter() {
        StringBuilder urlRegex = new StringBuilder();
        urlRegex.append("+");

        if (dbObject.get(Constants.FILTER_URL)!=null /*|| dbObject.get(Constants.SITE_URL) != null*/) {
            List urlsToFilter = (ArrayList) dbObject.get(Constants.FILTER_URL);
            urlRegex.append("(");
            for (Object anUrlsToFilter : urlsToFilter) {
                urlRegex = urlRegex.append(anUrlsToFilter).append("/*").append("|");
            }
            String pattern = dbObject.get(Constants.SITE_URL).toString().replaceAll("\\?","\\\\?");
            urlRegex.append(pattern).append("$");
            urlRegex.append(")");
        } else {
            urlRegex.append(".");
        }
        logger.info(urlRegex.toString());
        conf.set(Constants.URLFILTER_REGEX_RULES, urlRegex.toString());
    }

    public void addURLRegex() {
        StringBuilder urlRegex = new StringBuilder();
        urlRegex.append("+");
        if(dbObject.get(Constants.URLREGEX_STR)!=null && dbObject.get(Constants.SITE_URL)!=null) {
            String patternRegex = (String) dbObject.get(Constants.URLREGEX_STR);
            String pattern = patternRegex.replaceAll("\\?", "\\\\?");
            String siteURLRegex = dbObject.get(Constants.SITE_URL).toString().replaceAll("\\?","\\\\?");
            urlRegex.append(pattern);
            urlRegex.append("|").append(siteURLRegex).append("$");
        }
        else {
            urlRegex.append(".");
        }
        logger.info( urlRegex.toString());
        conf.set(Constants.URLFILTER_REGEX_RULES,urlRegex.toString());
    }

}
