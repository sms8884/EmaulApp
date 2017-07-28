package com.jaha.server.emaul.util;

import com.google.common.io.BaseEncoding;

import java.security.SecureRandom;

/**
 * Created by doring on 15. 4. 30..
 */
public class RandomKeys {
    public static String make(int size) {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[size];
        random.nextBytes(bytes);

        return BaseEncoding.base64Url().omitPadding().encode(bytes);
    }
}
