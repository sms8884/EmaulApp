package com.jaha.server.emaul.common.converter;

import com.jaha.server.emaul.constants.TodaySort;
import org.springframework.core.convert.converter.Converter;

public class TodaySortConverter implements Converter<String, TodaySort> {

    @Override
    public TodaySort convert(String code) {
        return TodaySort.value(code);
    }
}
