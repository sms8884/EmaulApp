package com.jaha.server.emaul.v2.model.board;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonView;
import com.jaha.server.emaul.v2.model.board.json.CommentView;
import com.jaha.server.emaul.v2.model.board.json.PostView;

public class ApiPostResponseBody implements Serializable {

    /** SID */
    private static final long serialVersionUID = -1201676860215673251L;

    @JsonView({PostView.Faq.class, PostView.FaqList.class, PostView.SystemNotice.class, PostView.SystemNoticeList.class, PostView.Event.class, PostView.EventList.class, PostView.Group.class,
            PostView.GroupList.class})
    private BoardCategoryVo category;

    @JsonView({PostView.Faq.class, PostView.FaqList.class, PostView.SystemNotice.class, PostView.SystemNoticeList.class, PostView.Event.class, PostView.EventList.class, PostView.Group.class,
            PostView.GroupList.class, CommentView.Comment.class, CommentView.CommentList.class})
    private Object content;

    @JsonView({PostView.Group.class, PostView.GroupList.class})
    private Object contentExt;

    public BoardCategoryVo getCategory() {
        return category;
    }

    public void setCategory(BoardCategoryVo category) {
        this.category = category;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Object getContentExt() {
        return contentExt;
    }

    public void setContentExt(Object contentExt) {
        this.contentExt = contentExt;
    }

}
