package com.jaha.server.emaul.controller;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Maps;
import com.jaha.server.emaul.service.StatService;

@Controller
public class StatController {

    @Autowired
    private StatService statService;

    @RequestMapping(value = "/api/stat/sharer", method = RequestMethod.POST)
    public ResponseEntity insertSharer(@RequestBody String json) {

        JSONObject obj = new JSONObject(json);

        Long postId = obj.getLong("postId");
        Long userId = obj.getLong("userId");
        String sns = obj.getString("sns");

        Map<String, String> input = Maps.newHashMap();
        input.put("postId", String.valueOf(postId));
        input.put("userId", String.valueOf(userId));
        input.put("sns", sns);

        statService.insertSharer(input);
        return new ResponseEntity(HttpStatus.CREATED);
    }


    @ExceptionHandler(value = {JSONException.class})
    public ResponseEntity JSONExceptionHandler() {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

}
