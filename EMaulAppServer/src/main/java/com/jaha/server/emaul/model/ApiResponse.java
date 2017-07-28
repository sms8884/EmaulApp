package com.jaha.server.emaul.model;

public class ApiResponse<T> {

    private ApiResponseHeader header;

    private T body;

    public ApiResponse() {
        header = new ApiResponseHeader();
    }

    public ApiResponse(T body) {
        this.header = new ApiResponseHeader();
        this.body = body;
    }

    public ApiResponse(String resultCode, String resultMessage) {
        header = new ApiResponseHeader(resultCode, resultMessage);
    }

    public ApiResponse(String resultCode, String resultMessage, T body) {
        header = new ApiResponseHeader(resultCode, resultMessage);
        this.body = body;
    }

    public ApiResponseHeader getHeader() {
        return header;
    }

    public void setHeader(ApiResponseHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

}
