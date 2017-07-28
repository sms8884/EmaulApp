package com.jaha.server.emaul.v2.model.board;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonView;
import com.jaha.server.emaul.v2.model.board.json.CommentView;
import com.jaha.server.emaul.v2.model.board.json.PostView;

public class ApiPostResponse<T> implements Serializable {

    /** SID */
    private static final long serialVersionUID = -6714097136744580227L;

    @JsonView({PostView.Faq.class, PostView.FaqList.class, PostView.SystemNotice.class, PostView.SystemNoticeList.class, PostView.Event.class, PostView.EventList.class, PostView.Group.class,
            PostView.GroupList.class, CommentView.Comment.class, CommentView.CommentList.class})
    private ApiPostResponseHeader header;

    @JsonView({PostView.Faq.class, PostView.FaqList.class, PostView.SystemNotice.class, PostView.SystemNoticeList.class, PostView.Event.class, PostView.EventList.class, PostView.Group.class,
            PostView.GroupList.class, CommentView.Comment.class, CommentView.CommentList.class})
    private T body;

    @JsonView({PostView.Faq.class, PostView.FaqList.class, PostView.SystemNotice.class, PostView.SystemNoticeList.class, PostView.Event.class, PostView.EventList.class, PostView.Group.class,
            PostView.GroupList.class, CommentView.Comment.class, CommentView.CommentList.class})
    private Object footer;

    public ApiPostResponse() {
        this.header = new ApiPostResponseHeader();
        // this.footer = new ApiResponseFooter();
    }

    public ApiPostResponse(String resultCode, String resultMessage) {
        this.header = new ApiPostResponseHeader(resultCode, resultMessage);
    }

    public ApiPostResponse(String resultCode, String resultMessage, T body) {
        this.header = new ApiPostResponseHeader(resultCode, resultMessage);
        this.body = body;
    }

    public ApiPostResponse(ApiPostResponseHeader header, T body) {
        this.header = header;
        this.body = body;
    }

    public ApiPostResponse(ApiPostResponseHeader header, T body, Object footer) {
        this.header = header;
        this.body = body;
        this.footer = footer;
    }

    public ApiPostResponseHeader getHeader() {
        return header;
    }

    public void setHeader(ApiPostResponseHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public Object getFooter() {
        return footer;
    }

    public void setFooter(Object footer) {
        this.footer = footer;
    }

}
