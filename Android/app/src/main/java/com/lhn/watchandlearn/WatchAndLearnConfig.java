package com.lhn.watchandlearn;

public class WatchAndLearnConfig {
    public static final String CONFIG_URL;

    public static enum State {
        Dev, Staging, Prod
    }

    public static final State state = State.Prod;

    static{
        switch(state) {
            case Prod:
            case Staging:
            case Dev:
            default:
                CONFIG_URL = "https://dl.dropboxusercontent.com/u/30421262/config.json";
        }
    }

    public static boolean isDebugMode() {
        return state == State.Dev || state == State.Staging;
    }
}
