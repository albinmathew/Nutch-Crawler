package com.qburst.NutchSelenium.filter;

import com.qburst.NutchSelenium.configuration.Constants;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.net.URLFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Filters urls based on the configuration passed in
 */
public class CustomURLFilter implements URLFilter {

    private Configuration nutchConfiguration;
    private Logger logger = Logger.getLogger(CustomURLFilter.class.getName());

    public CustomURLFilter() {
    }

    @Override
    public String filter(String s) {
        String patternToPass = nutchConfiguration.get(Constants.URL_FILTER_COLLECTIONS_KEY);
        logger.info(patternToPass);
        //for(String patternToPass:nutchConfiguration.getStrings(Constants.URL_FILTER_COLLECTIONS_KEY)) {
        if (s.matches(patternToPass)) {
            logger.info("Matched");
            return s;
        }
        return null;
    }
    // return null;

    @Override
    public void setConf(Configuration entries) {
        this.nutchConfiguration=entries;
    }

    @Override
    public Configuration getConf() {
        return nutchConfiguration;
    }
}