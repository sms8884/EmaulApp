package com.jaha.server.emaul.common.code;

public enum Code {

    /* @formatter:off */
    CODE_GROUP_NEWS_TYPE("NEWS_TYPE", "뉴스구분"), CODE_GROUP_NEWS_CATEGORY("NEWS_CTG", "마을뉴스 카테고리")

    , NEWS_TYPE_GENERAL("GNRL", "일반뉴스"), NEWS_TYPE_CARD("CARD", "카드뉴스")

    , NEWS_CATEGORY_CULTURE("CULTURE", "문화"), NEWS_CATEGORY_EVENT("EVENT", "구행사"), NEWS_CATEGORY_FOOD("FOOD", "맛집"), NEWS_CATEGORY_LIFE("LIFE", "생활"), NEWS_CATEGORY_PEOPLE("PEOPLE", "사람"), NEWS_CATEGORY_PLACE("PLACE", "장소"), NEWS_CATEGORY_TRAVEL("TRAVEL", "여행")

    , AGE_00("00", "0~10세"), AGE_10("10", "11~20세"), AGE_20("20", "21~30세"), AGE_30("30", "31~40세"), AGE_40("40", "41~50세"), AGE_50("50", "51~60세"), AGE_60("60", "61~70세"), AGE_70("70", "71세이상")

    , GENDER_MALE("M", "남자"), GENDER_FEMALE("F", "여자")
    
    , APP_AP_OPEN_TYPE_SERVER("1", "서버연동형"), APP_AP_OPEN_TYPE_BLE("2", "BLE"), APP_AP_OPEN_TYPE_WIFI("3", "WIFI")
    
    ;
    /* @formatter:on */

    Code(String code, String name) {
        this.code = code;
        this.name = name;
    }

    private final String code;

    private final String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("Code:%s, Name:%s", getCode(), getName());
    }
}
