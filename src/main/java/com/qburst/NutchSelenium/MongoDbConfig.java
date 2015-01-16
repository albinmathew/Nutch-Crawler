package com.qburst.NutchSelenium;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.util.JSON;
import com.qburst.NutchSelenium.configuration.Constants;
import com.qburst.NutchSelenium.configuration.CrawlConfiguration;
import com.qburst.NutchSelenium.models.JsonModel;
import com.qburst.NutchSelenium.mongodbutils.DbFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * MongoDB Singleton class
 */
public class MongoDbConfig {

    private static MongoDbConfig mongoDbInstance = null;
    private static CrawlConfiguration config;
    public static DbFunctions mongodb;
    public static DBCollection crawlConfig;
    private static String configFile = Constants.CONFIG_FILE;
    private static Logger logger = LoggerFactory.getLogger(MongoDbConfig.class);

    private MongoDbConfig() {
        initialize();
    }

    public void initialize() {
        mongodb = new DbFunctions();
        try {
            InputStream in = Files.newInputStream(Paths.get(configFile));
            Yaml yaml = new Yaml();
            config = yaml.loadAs(in,CrawlConfiguration.class);
        } catch(IOException e) {
            logger.info(e.getMessage());
        }

    }
    public static MongoDbConfig getMongoDbInstance() {
        if(mongoDbInstance == null)
            return new MongoDbConfig();
        else
            return mongoDbInstance;
    }

    public void setCollection() {
        mongodb.dbConnect("crawlDB");
        mongodb.createCollection("nutchcrawl");
        crawlConfig = mongodb.selectCollection("nutchcrawl");
    }

    public void setDocument() {
        JsonModel jsonModel = new JsonModel();
        Map hMap;
        if(config != null) {
            hMap = config.getxPath();
            jsonModel.setSiteName(config.getSiteName());
            jsonModel.setSiteURL(config.getSiteURL());
            jsonModel.setDepth(config.getDepth());
            jsonModel.setFilterURL(config.getFilterURL());
            jsonModel.setOutLinkURLNum(config.getOutLinkURLNum());
            jsonModel.setStorageDirPath(config.getStorageDirPath());
            jsonModel.setOutputFile(config.getOutputFile());
            jsonModel.setOutlinkListFile(config.getOutlinkListFile());
            jsonModel.setxPath(hMap);
            Gson gson = new Gson();
            String document = gson.toJson(jsonModel);
            BasicDBObject obj;
            obj = (BasicDBObject) JSON.parse(document);
            mongodb.insert(crawlConfig, obj);
            logger.info("Document inserted successfully");
        }
    }

    public DbFunctions getDatabaseObj() {
        return mongodb;
    }
    public DBCollection getCollection() {
        return crawlConfig;
    }

}
