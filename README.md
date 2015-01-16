## Synopsis

A custom web crawler using Apache Nutch v1.9.
The URL to be crawled and the parameters for crawling is read from documents in a MongoDB collection. This custom web crawler utilizes a list of XPath expressions to extract information from the crawled URLs and stores the retrieved information in a csv formatted file.


## Setup MongoDB
1. Open the MongoDB shell.
```
$ mongo
```

2. Create a mongoDB database, "crawlDB" if it does not exist
```
$ use crawlDB
```
3.  Create a mongoDB collection, "nutchcrawl"
```
 $  db.createCollection("nutchcrawl")
```
4. Insert MongoDB documents to "nutchcrawl" collection
```
 $ db.nutchcrawler.insert({sample_document})
```

Note : Alternatively, a MongoDB utility , Robomongo can be used to create MongoDB  collection and documents.



### MongoDB sample document

The following is a sample MongoDB document for specifying the website URL and the crawler parameters.

sample_document :
```
 {
    "_id" : ObjectId("547db209eac4617525bf3820"),
    "siteName" : "synonyms.com",
    "siteURL" : "http://synonyms.ca/a-synonyms-1.htm",
    "urlRegex" : "http://synonyms.ca/*",
    "depth" : 2,
    "xPath" : {
        "Word" : "//*[@id=\"Content\"]/h1",
        "List" : "//*[@id=\"Content\"]/p/text()[4]"
    },
    "outLinkURLNum" : -1,
    "storageDirPath" : "build/crawler/nutch/synonyms",
    "outputFile" : "synonyms.csv",
    "outlinkListFile" : "synonymslinks"
}
```
siteName  : Name of the website URL to be  crawled.
siteURL :  URL of the site to be crawled.
urlRegex : Restricts the crawling to the specified URLs that satisfies the regular expression specified in this field.
depth : Link depth from the root page that should be crawled.
outLinkURLNum : Maximum number of pages that will be retrieved at each level upto the depth.
xPath : XPath expressions to extract information from the crawled pages.
storageDirPath : Path of the directory that stores the results of crawling(crawlDB, segments).
outputFile : Path of the CSV formatted file that stores the result of XPath expression evaluation.
outlinkListFile : Path of the file that stores the outlink URLs from the crawled pages.

## Installation

1. Build with Maven
 ```
 $ mvn clean package
 ```
2. Run the jar file
```
 $ java -jar target/NutchCrawler-1.0-SNAPSHOT.jar
```

## Sources
 http://wiki.apache.org/nutch/NutchTutorial# crawler
