package com.jaha.server.emaul.constants;

public enum Gu {

    GANGNAM("101"),
    GANGDONG("102"),
    DOBONG("103"),
    NOWON("104"),
    GANGBUK("105"),
    SEONGBUK("106"),
    JUNGNANG("107"),
    DONGDAEMUN("108"),
    SEONGDONG("109"),
    JONGNO("110"),
    JUNG("111"),
    GWANGJIN("112"),
    EUNPYEONG("113"),
    SEODAEMUN("114"),
    MAPO("115"),
    YONGSAN("116"),
    GANGSEO("117"),
    YANGCHEON("118"),
    YEONGDEUNGPO("119"),
    GURO("120"),
    DONGJAK("121"),
    GEUMCHEON("122"),
    GWANAK("123"),
    SEOCHO("124"),
    SONGPA("125");


    private String code;

    public String getCode() {
        return code;
    }

    Gu(String code) {
        this.code = code;
    }
}
