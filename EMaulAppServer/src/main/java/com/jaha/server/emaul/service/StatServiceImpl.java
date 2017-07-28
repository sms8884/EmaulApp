package com.jaha.server.emaul.service;

import com.jaha.server.emaul.mapper.StatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StatServiceImpl implements StatService {


    @Autowired
    private StatMapper statMapper;

    @Override
    public void insertSharer(Map<String, String> input) {
        statMapper.insertSharer(input);
    }
}
