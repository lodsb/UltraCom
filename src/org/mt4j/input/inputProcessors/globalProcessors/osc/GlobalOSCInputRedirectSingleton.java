package org.mt4j.input.inputProcessors.globalProcessors.osc;

import java.util.HashMap;

import org.mt4j.input.inputProcessors.globalProcessors.GlobalOSCInputProcessor;

public class GlobalOSCInputRedirectSingleton {
    private static GlobalOSCInputRedirectSingleton instance = null;

    public static GlobalOSCInputRedirectSingleton getInstance() {
        if (instance == null) {
            instance = new GlobalOSCInputRedirectSingleton();
        }

        return instance;
    }

    private HashMap<String, GlobalOSCInputProcessor> urlToGOSCMap =
            new HashMap<String, GlobalOSCInputProcessor>();

    private Object monitor = new Object();

    public GlobalOSCInputProcessor getTargetFromUrl(String url) {
        synchronized (monitor) {
            return this.urlToGOSCMap.get(url);
        }
    }

    public void registerGlobalOSCInputProcessor(String url,
                                                GlobalOSCInputProcessor goip) {
        synchronized (monitor) {
            this.urlToGOSCMap.put(url, goip);
        }
    }


}
