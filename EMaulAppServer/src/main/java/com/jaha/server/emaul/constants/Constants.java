package com.jaha.server.emaul.constants;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

/**
 * @author shavrani
 */
public interface Constants {

    /** 파일 최상위 그룹 */
    static final String FILE_CATEGORY_NOTICE = "system-notice";// 시스템공지사항
    static final String FILE_CATEGORY_MAIN_TEMPLATE = "main-template";// 앱메인템플릿

    static final int PHONE_USER_ACCOUNT_MAX = 4;// 휴대폰번호로 등록가능한 최대 계정개수

    /** 마을뉴스 앱 URI */
    static final String APP_URI_MAUL_NEWS = "emaul://today-detail?id=%s&newsCategory=%s";
    /** 게시판 앱 URI */
    static final String APP_URI_BOARD_POST = "emaul://post-detail?id=%s";

    public static final int IDX_APP_URL_ANDROID = 0;
    // public static final int IDX_APP_URL_IOS = 1;
    public static final String MESSAGE_SENDER_PHONE_NUMBER = "028670816";
    public static final String[] APP_URLS = {"https://goo.gl/iyyLTO"};
    public static final String HTTP_RESULT_OK = "0";
    public static final String HTTP_RESULT_AUTH_FAIL = "1";
    public static final String HTTP_RESULT_NO_TARGET_USER = "2";
    public static final String HTTP_RESULT_EXIST_TARGET_USER = "3";
    public static final String PARAM_KIND_ANDROID = "android";

    /** 기본 날짜형식: yyyyMMddHHmmss */
    public static final String DEFAULT_DATE_FORMAT = "yyyyMMddHHmmss";
    public static final SimpleDateFormat DEFAULT_SDF = new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.KOREA);

    public static final String SHORT_DATE_FORMAT = "yyyyMMdd";
    public static final SimpleDateFormat SHORT_DATE_SDF = new SimpleDateFormat(SHORT_DATE_FORMAT, Locale.KOREA);

    /** AP 테스트 아파트 ID */
    static final Long AP_TEST_APT_ID = 191L;

    /** AP 작동에서 제외할 아파트 ID */
    static final List<Long> AP_EXCLUDE_APT_ID = Lists.newArrayList(1L, 576L);

    /** 푸쉬광고의 접두사 */
    static final String ADVERT_PUSH_PREFIX = "(후원)";// (광고) or (후원)

}
