package com.qburst.NutchSelenium.models;

import java.util.HashMap;

/**
 * Created by qbuser on 10/11/14.
 */
public class DataHolder {

    private static DataHolder instance=null;

    private HashMap<String, Object> objectHolder;

    private DataHolder() {
        initialize();
    }

    private void initialize() {
        objectHolder=new HashMap<String, Object>();
    }

    public static synchronized DataHolder getInstance() {

        if(instance==null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public Object getObjectWithKey(String key) {
        return objectHolder.get(key);
    }

    public void setObjectWithKey(String key, Object object) {
        objectHolder.put(key, object);
    }

}
