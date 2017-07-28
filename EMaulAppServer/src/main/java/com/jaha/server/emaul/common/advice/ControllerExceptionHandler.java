package com.jaha.server.emaul.common.advice;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jaha.server.emaul.common.exception.EmaulException;
import com.jaha.server.emaul.model.ApiResponse;

@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(value = EmaulException.class)
    @ResponseBody
    public ApiResponse<?> handleErrorEmaul(HttpServletRequest req, EmaulException e) throws Exception {
        // LOGGER.error("================================================================================");
        LOGGER.error("### Exception caught: {}", e.getMessage());
        // LOGGER.error("================================================================================");
        return new ApiResponse<>(e.getCode(), e.getMessage(), e.getObject());
    }

}
