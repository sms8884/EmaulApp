package com.jaha.server.emaul.constants;

public enum TodaySort {
    RECENT("recent"),
    POPULAR("popular");

    private String code;

    TodaySort(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static TodaySort value(String code) {
        switch (code){
            case "recent" :
                return RECENT;
            case "popular" :
                return POPULAR;
            default :
                return RECENT;
        }
    }

}
