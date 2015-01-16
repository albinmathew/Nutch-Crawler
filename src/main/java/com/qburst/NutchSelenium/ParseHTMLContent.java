package com.qburst.NutchSelenium;

import com.csvreader.CsvWriter;
import com.mongodb.DBObject;

import com.qburst.NutchSelenium.configuration.Constants;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.HtmlParseFilter;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.protocol.Content;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.TagNode;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.htmlcleaner.HtmlCleaner;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.*;
import java.util.*;


/**
 * Parse the HTML content and retrieve data of given XPath
 */
public class ParseHTMLContent extends NutchCrawler implements HtmlParseFilter {

    private Configuration conf;
    private static final List<String> htmlMimeTypes = Arrays.asList("text/html", "application/xhtml+xml");
    private HtmlCleaner cleaner = new HtmlCleaner();
    private DomSerializer domSerializer;
    private CsvWriter csvWriter;

    private DBObject dbObject;
    private static Logger logger = LoggerFactory.getLogger(ParseHTMLContent.class);


    public ParseHTMLContent() {
        init();
    }

    public void init() {
        CleanerProperties properties = cleaner.getProperties();
        properties.setAllowHtmlInsideAttributes(true);
        properties.setAllowMultiWordAttributes(true);
        properties.setRecognizeUnicodeChars(true);
        properties.setOmitComments(true);
        properties.setNamespacesAware(false);
        domSerializer = new DomSerializer(properties);
    }

    @Override
    public ParseResult filter(Content content, ParseResult parseResult, HTMLMetaTags metaTags, DocumentFragment doc){
        byte[] rawContent = content.getContent();
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            org.w3c.dom.Document cleanedXmlHtml = documentBuilder.newDocument();
            if(htmlMimeTypes.contains(content.getContentType())) {
                Reader rawContentReader = new InputStreamReader(new ByteArrayInputStream(rawContent));
                TagNode tagNode = cleaner.clean(rawContentReader);
                cleanedXmlHtml = domSerializer.createDOM(tagNode);
            }
            Object key;
            Object value;
            Object mapKey;
            HashMap map;

            map = (HashMap) dbObject.get(Constants.XPATH_KEY);
            Set keySet = map.keySet();
            Iterator keyItr = keySet.iterator();
            String fileName = (String) dbObject.get(Constants.OUTPUT_FILE);
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
                CsvWriter csvWriterHead = new CsvWriter(new FileWriter(file, true), ',');
                while (keyItr.hasNext()) {
                    mapKey = keyItr.next();
                    csvWriterHead.write(mapKey.toString());
                }
                if (!keyItr.hasNext()) {
                    csvWriterHead.endRecord();
                    csvWriterHead.close();
                }
            }
            csvWriter = new CsvWriter(new FileWriter(file, true), ',');
            Iterator itr = keySet.iterator();
            List outputList;
            while (itr.hasNext()) {
                key = itr.next();
                value = map.get(key);
                XPath NameValue = new DOMXPath(value.toString());
                outputList = getXPathValue(NameValue, cleanedXmlHtml);
                for (Object anOutputList : outputList) {
                    String output = anOutputList.toString();
                    String cleanedOutput = output.replace("&amp;", "&");
                    csvWriter.write(cleanedOutput);
                }
                if (!itr.hasNext()) {
                    csvWriter.endRecord();
                }
            }
        } catch(IOException | ParserConfigurationException | JaxenException e) {
            logger.warn(e.getMessage());

        } finally {
            csvWriter.close();
        }
        return parseResult;
    }

    @Override
    public void setConf(Configuration conf){
        this.conf = conf;
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    public void setDbObject(DBObject obj) {
        dbObject = obj;
    }


    public List getXPathValue (XPath xpath, org.w3c.dom.Document cleanHtml ) {
        String value;
        List valueList = new ArrayList();
        try {
            List nodeList = xpath.selectNodes(cleanHtml);
            for (Object node : nodeList) {
                if (node instanceof Node) {
                    value = ((Node) node).getTextContent();
                    valueList.add(value);
                } else {
                    value = String.valueOf(node);
                    valueList.add(value);
                }
            }
        }catch(JaxenException e) {
            logger.warn(e.getMessage());
        }
        return valueList;
    }
}
