package com.qburst.NutchSelenium.configuration;

import java.util.List;
import java.util.Map;

/**
 * Configuration file model class
 */
public class CrawlConfiguration {
    private Map xPath;
    private String siteURL;
    private String siteName;
    private String outlinkListFile;
    private String outputFile;
    private int depth;
    private int outLinkURLNum;
    private String storageDirPath;
    private String databaseName;
    private String collectionName;
    private List filterURL;

    public Map getxPath() {
        return xPath;
    }

    public void setxPath(Map xPath) {
        this.xPath = xPath;
    }

    public String getSiteURL() {
        return siteURL;
    }

    public void setSiteURL(String siteURL) {
        this.siteURL = siteURL;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getOutlinkListFile() {
        return outlinkListFile;
    }

    public void setOutlinkListFile(String outlinkListFile) {
        this.outlinkListFile = outlinkListFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getOutLinkURLNum() {
        return outLinkURLNum;
    }

    public void setOutLinkURLNum(int outLinkURLNum) {
        this.outLinkURLNum = outLinkURLNum;
    }

    public String getStorageDirPath() {
        return storageDirPath;
    }

    public void setStorageDirPath(String storageDirPath) {
        this.storageDirPath = storageDirPath;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getCollectionName() {
        if(collectionName != null)
            return collectionName;
        else
            return "";
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public List getFilterURL() {
        return filterURL;
    }

    public void setFilterURL(List filterURL) {
        this.filterURL = filterURL;
    }
}

