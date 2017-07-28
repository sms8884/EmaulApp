package com.jaha.server.emaul.common.converter;

import java.util.Arrays;

import org.springframework.core.convert.converter.Converter;

import com.jaha.server.emaul.constants.Gu;

public class GuConverter implements Converter<String, Gu> {

    @Override
    public Gu convert(String source) {
        return Arrays.stream(Gu.values())
                .filter(g -> g.getCode().equals(source))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("존재하지 않는 구청코드 : %s.", source)));
    }
}
