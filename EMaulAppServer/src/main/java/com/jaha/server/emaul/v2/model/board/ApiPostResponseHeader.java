package com.jaha.server.emaul.v2.model.board;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonView;
import com.jaha.server.emaul.v2.constants.CommonConstants.ResponseCode;
import com.jaha.server.emaul.v2.model.board.json.CommentView;
import com.jaha.server.emaul.v2.model.board.json.PostView;

public class ApiPostResponseHeader implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 응답 코드 "00" : 요청 성공, 이외의 경우 오류상태
     */
    @JsonView({PostView.Faq.class, PostView.FaqList.class, PostView.SystemNotice.class, PostView.SystemNoticeList.class, PostView.Event.class, PostView.EventList.class, PostView.Group.class,
            PostView.GroupList.class, CommentView.Comment.class, CommentView.CommentList.class})
    private String resultCode = "00";

    /**
     * 응답 메시지
     */
    @JsonView({PostView.Faq.class, PostView.FaqList.class, PostView.SystemNotice.class, PostView.SystemNoticeList.class, PostView.Event.class, PostView.EventList.class, PostView.Group.class,
            PostView.GroupList.class, CommentView.Comment.class, CommentView.CommentList.class})
    private String resultMessage = "SUCCESS";

    public ApiPostResponseHeader() {

    }

    public ApiPostResponseHeader(String resultCode, String resultMessage) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
    }

    public ApiPostResponseHeader(ResponseCode responseCode) {
        this.resultCode = responseCode.getCode();
        this.resultMessage = responseCode.getMessage();
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

}
