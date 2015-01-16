package com.qburst.NutchSelenium.models;

import java.util.List;
import java.util.Map;

/**
 * JSon Model for MongoDB documents
 */
public class JsonModel {

    private String siteName;
    private String siteURL;
    private List filterURL;
    private int depth;
    private int outLinkURLNum;
    private Map<String,String> xPath;
    private String storageDirPath;
    private String outputFile;
    private String outlinkListFile;

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteURL() {
        return siteURL;
    }

    public void setSiteURL(String siteURL) {
        this.siteURL = siteURL;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List getFilterURL() {
        return filterURL;
    }

    public void setFilterURL(List filterURL) {
        this.filterURL = filterURL;
    }

    public int getOutLinkURLNum() {
        return outLinkURLNum;
    }

    public void setOutLinkURLNum(int outLinkURLNum) {
        this.outLinkURLNum = outLinkURLNum;
    }

    public Map<String, String> getxPath() {
        return xPath;
    }

    public void setxPath(Map<String, String> xPath) {
        this.xPath = xPath;
    }

    public String getStorageDirPath() {
        return storageDirPath;
    }

    public void setStorageDirPath(String storageDirPath) {
        this.storageDirPath = storageDirPath;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getOutlinkListFile() {
        return outlinkListFile;
    }

    public void setOutlinkListFile(String outlinkListFile) {
        this.outlinkListFile = outlinkListFile;
    }
}
