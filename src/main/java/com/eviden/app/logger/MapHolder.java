package com.eviden.app.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapHolder<String, ILoggingEvent> {
    private Map eventMap = new ConcurrentHashMap<String, ILoggingEvent>();
    private MapHolder(){}

    private static MapHolder MAP_INSTANCE = null;

    public static MapHolder create(){
        if(MAP_INSTANCE == null){
            MAP_INSTANCE = new MapHolder();
        }
        return MAP_INSTANCE;
    }

    public void putEvent(String key,ILoggingEvent value){
        eventMap.put(key,value);
    }

    public Map getEventMap(){
        return eventMap;
    }

}