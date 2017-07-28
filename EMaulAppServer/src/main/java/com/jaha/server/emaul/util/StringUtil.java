package com.jaha.server.emaul.util;

import java.text.BreakIterator;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;

public class StringUtil extends StringUtils {

    public static String nvl(Object obj) {
        if (obj != null) {
            return obj.toString();
        }
        return "";
    }

    public static String nvl(Object obj, String defaultStr) {
        if (obj != null) {
            return obj.toString();
        }
        return defaultStr;
    }

    public static int nvlInt(Object obj) {
        if (!isBlank(nvl(obj))) {
            try {
                return Integer.parseInt(obj.toString());
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public static int nvlInt(Object obj, int defaultInt) {
        if (!isBlank(nvl(obj))) {
            try {
                return Integer.parseInt(obj.toString());
            } catch (Exception e) {
            }
        }
        return defaultInt;
    }

    public static Integer nvlInt(Object obj, Integer defaultInt) {
        if (!isBlank(nvl(obj))) {
            try {
                return Integer.parseInt(obj.toString());
            } catch (Exception e) {
            }
        }
        return defaultInt;
    }

    public static long nvlLong(Object obj) {
        if (!isBlank(nvl(obj))) {
            try {
                return Long.parseLong(obj.toString());
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public static long nvlLong(Object obj, long detaultLong) {
        if (!isBlank(nvl(obj))) {
            try {
                return Long.parseLong(obj.toString());
            } catch (Exception e) {
            }
        }
        return detaultLong;
    }

    public static Long nvlLong(Object obj, Long detaultLong) {
        if (!isBlank(nvl(obj))) {
            try {
                return Long.parseLong(obj.toString());
            } catch (Exception e) {
            }
        }
        return detaultLong;
    }

    public static double nvlDouble(Object obj) {
        if (!isBlank(nvl(obj))) {
            try {
                return Double.parseDouble(obj.toString());
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public static double nvlDouble(Object obj, double detaultLong) {
        if (!isBlank(nvl(obj))) {
            try {
                return Double.parseDouble(obj.toString());
            } catch (Exception e) {
            }
        }
        return detaultLong;
    }

    public static Double nvlDouble(Object obj, Double detaultDouble) {
        if (!isBlank(nvl(obj))) {
            try {
                return Double.parseDouble(obj.toString());
            } catch (Exception e) {
            }
        }
        return detaultDouble;
    }

    public static String defaultComma(Object obj) {
        DecimalFormat df = new DecimalFormat("#,##0");
        return df.format(nvl(obj.toString()));
    }

    public static boolean isNull(Object obj) {
        if (!isBlank(nvl(obj))) {
            return false;
        }
        return true;
    }

    /**
     * 글자수 계산
     *
     * @param value
     * @return
     */
    public static int getGraphemeLength(String value) {
        value = StringUtils.defaultString(value);

        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(value);
        int count = 0;

        while (it.next() != BreakIterator.DONE) {
            count++;
        }
        return count;
    }

}
