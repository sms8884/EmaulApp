package com.jaha.server.emaul.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.google.common.io.ByteStreams;

/**
 * Created by doring on 15. 3. 20..
 */
public class Responses {

    public static ResponseEntity<byte[]> getFileEntity(File toServeUp, String fileName) {
        final HttpHeaders headers = new HttpHeaders();

        InputStream inputStream;

        try {
            inputStream = new FileInputStream(toServeUp);
        } catch (FileNotFoundException e) {
            String msg = "ERROR: File not found.";
            headers.setContentType(MediaType.TEXT_PLAIN);
            return new ResponseEntity<>(msg.getBytes(), headers, HttpStatus.NOT_FOUND);
        }

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        try {
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Long fileSize = toServeUp.length();
        headers.setContentLength(fileSize.intValue());

        try {
            return new ResponseEntity<>(ByteStreams.toByteArray(inputStream), headers, HttpStatus.OK);
        } catch (Exception e) {
            String msg = "ERROR: Unknown.";
            headers.setContentType(MediaType.TEXT_PLAIN);
            return new ResponseEntity<>(msg.getBytes(), headers, HttpStatus.NOT_FOUND);
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
