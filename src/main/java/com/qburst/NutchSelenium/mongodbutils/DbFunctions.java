package com.qburst.NutchSelenium.mongodbutils;

import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * MongoDB helper functions
 */
public class DbFunctions {
    private static MongoClient mongoClient;
    private static  DB database;

    private static DBCursor handlenext ;
    private static Logger logger = LoggerFactory.getLogger(DbFunctions.class);

    public void dbConnect(String dbName) {
        try {
            mongoClient = new MongoClient("10.3.0.38", 27017);
            database = mongoClient.getDB(dbName);
        } catch(UnknownHostException e) {
            logger.warn(e.getMessage());
        }
    }
    long count;
    public long getDocumentCount(DBCollection collection) {
        count = collection.count();
        return count;
    }

    public void createCollection(String collectionName) {
        DBObject options = new BasicDBObject();
        if(!database.collectionExists(collectionName)) {
            database.createCollection(collectionName, options);
        }
        else {
            logger.info("Collection exists");
        }
    }

    public DBCollection selectCollection(String collectionName) {
        DBCollection collection;
        collection = database.getCollection(collectionName);
        return collection;
    }


    public DBObject getNextDocument(DBCollection collection) {
        DBObject emptyObj = new BasicDBObject();
        if(handlenext.hasNext()) {
            return handlenext.next();

        }
        return emptyObj;
    }

    public DBObject getFirstDocument(DBCollection collection) {
        handlenext = collection.find();
        handlenext.next();
        return handlenext.one();
    }

    public Object find(String parameterName,DBCollection collection) {
        DBCursor handle = collection.find();
        BasicDBObject obj = (BasicDBObject) handle.next();
        return obj.get(parameterName);
    }
    
    public ArrayList<DBObject> getAllDoc(DBCollection coll) {
        ArrayList<DBObject> docList = new ArrayList<>();
        DBCursor cursor = coll.find();
        while (cursor.hasNext()) {
            docList.add(cursor.next());
        }
        return docList;
    }

    public void insert(DBCollection collection, BasicDBObject docObject) {
        BasicDBObject queryObj = new BasicDBObject("siteURL",docObject.get("siteURL"));
        collection.update(queryObj, docObject, true, false);
    }


    public void dbDisconnect() {
        mongoClient.close();
    }
}
