package com.jaha.server.emaul.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaha.server.emaul.common.code.Code;
import com.jaha.server.emaul.constants.TodaySort;
import com.jaha.server.emaul.model.ApiResponse;
import com.jaha.server.emaul.model.BoardCategory;
import com.jaha.server.emaul.model.BoardComment;
import com.jaha.server.emaul.model.BoardCommentReply;
import com.jaha.server.emaul.model.BoardPost;
import com.jaha.server.emaul.model.CommonCode;
import com.jaha.server.emaul.model.SimpleUser;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.BoardService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.Responses;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.TagUtils;
import com.jaha.server.emaul.util.Thumbnails;
import com.jaha.server.emaul.v2.constants.BoardConstants;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushAction;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushAlarmSetting;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushGubun;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushMessage;
import com.jaha.server.emaul.v2.constants.CommonConstants.PushTargetType;
import com.jaha.server.emaul.v2.util.PushUtils;

/**
 * Created by doring on 15. 3. 9..
 */
@Controller
public class BoardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoardController.class);

    private static final int MAX_POST_IMAGE_COUNT = 3;

    @Autowired
    private BoardService boardService;
    @Autowired
    private UserService userService;
    // @Autowired
    // private GcmService gcmService;

    @Autowired
    Environment environment;

    // [START] 광고 푸시 추가 by realsnake 2016.10.28
    @Autowired
    private PushUtils pushUtils;

    // [END]

    @RequestMapping(value = "/api/board/categories/{type}", method = RequestMethod.GET)
    public @ResponseBody List<BoardCategory> listCategory(HttpServletRequest req, @PathVariable(value = "type") String type) {

        return boardService.getCategories(SessionAttrs.getUserId(req.getSession()), type);
    }

    @RequestMapping(value = "/api/board/post/{postId}", method = RequestMethod.GET)
    public @ResponseBody String handleGetPost(HttpServletRequest req, @PathVariable(value = "postId") Long postId) throws JsonProcessingException, UnsupportedEncodingException {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        BoardPost post = boardService.get(user, postId);

        if (post == null) {
            return "DELETED";
        }

        if (StringUtils.isNotEmpty(post.file1)) {
            post.file1 = URLEncoder.encode(post.file1, "UTF-8");
            LOGGER.info(">>> 첨부파일 1 : " + post.file1);
        }

        if (StringUtils.isNotEmpty(post.file2)) {
            post.file2 = URLEncoder.encode(post.file2, "UTF-8");
            LOGGER.info(">>> 첨부파일 2 : " + post.file2);
        }


        try {
            post.user = userService.convertUserForPost(post.user, user.house.apt.id, post.user.house.dong, post.category.getUserPrivacy());

            if (post.content.indexOf("<!DOCTYPE html>") > -1) {
                post.content = post.content.replaceAll("(\r\n|\n)", "");
            }
        } catch (Exception e) {
            LOGGER.error("<<게시판 상세 조회 중 오류>>", e);
        }

        post.user = userService.convertToPublicUser(post.user);

        if ("today".equals(post.category.type))
            post.hashtags = boardService.fetchHashtags(post.id);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(post);
    }

    @RequestMapping(value = "/api/board/comment/{commentId}", method = RequestMethod.GET)
    public @ResponseBody String handleGetComment(HttpServletRequest req, @PathVariable(value = "commentId") Long commentId) throws JsonProcessingException {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        BoardComment comment = boardService.getComment(user, commentId);
        if (comment == null) {
            return "DELETED";
        }

        try {
            comment.user = userService.convertUserForPost(comment.user, user.house.apt.id, comment.user.house.dong, comment.post.category.getUserPrivacy());
        } catch (Exception e) {
            LOGGER.error("<<(무시) 게시판 댓글 조회 중 오류>>", e);
        }

        comment.user = userService.convertToPublicUser(comment.user);
        comment.post = null;

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(comment);
    }

    @RequestMapping(value = "/api/board/post/write", method = RequestMethod.POST)
    public @ResponseBody BoardPost writePost(HttpServletRequest req, @RequestParam(value = "content", required = false, defaultValue = "") String content, @RequestParam(value = "title",
            required = false, defaultValue = "") String title, @RequestParam(value = "categoryId") Long categoryId, @RequestParam(value = "rangeSido", required = false) String rangeSido,
            @RequestParam(value = "rangeSigungu", required = false) String rangeSigungu, @RequestParam(value = "files", required = false) MultipartFile[] files) {
        String reqIp = req.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(reqIp)) {
            reqIp = req.getRemoteAddr();
        }

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        BoardCategory category = boardService.getCategory(categoryId);

        if (!category.isUserWritable(user)) {
            return null;
        }

        BoardPost post = new BoardPost();
        post.user = user;
        post.category = boardService.getCategory(categoryId);
        post.regDate = new Date();
        if (BoardConstants.ContentMode.HTML.getValue().equals(post.category.contentMode)) {
            content = String.format(BoardConstants.APP_HTML_FORMAT, content);
        }
        post.content = content;
        post.title = title == null || title.isEmpty() ? null : title;
        post.imageCount = files == null ? 0 : files.length;
        post.rangeAll = false;
        post.rangeSido = rangeSido == null ? "" : rangeSido;
        post.rangeSigungu = rangeSigungu == null ? "" : rangeSigungu;
        post.displayYn = "Y";
        post.reqIp = reqIp;

        post = boardService.save(post);

        if (files != null) {
            long postId = post.id;
            long postParentNum = post.id / 1000l;
            final int len = files.length;
            for (int i = 0; i < len; i++) {
                try {
                    File dir = new File(String.format("/nas/EMaul/board/post/image/%s/%s", postParentNum, postId));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File dest = new File(dir, String.format("%s.jpg", i));
                    dest.createNewFile();
                    files[i].transferTo(dest);
                    Thumbnails.create(dest);
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
        }

        // 관리자들에게 민원 푸시 발송
        if ("complaint".equals(post.category.type)) {
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
            // List<User> adminUsers = userService.getAdminUsers(user.house.apt.id);
            //
            // List<Long> applyAdminIds = Lists.transform(adminUsers, new Function<User, Long>() {
            // @Override
            // public Long apply(User input) {
            // return input.id;
            // }
            // });
            //
            // GcmSendForm form = new GcmSendForm();
            // Map<String, String> msg = Maps.newHashMap();
            // msg.put("type", "action");
            // msg.put("title", "새로운 민원이 접수되었습니다.");
            // msg.put("value", post.content);
            // msg.put("action", "emaul://post-detail?id=" + post.id);
            // form.setUserIds(applyAdminIds);
            // form.setMessage(msg);
            //
            // gcmService.sendGcm(form);

            List<SimpleUser> targetUserList = this.pushUtils.findPushTargetAdminList(PushAlarmSetting.BOARD, Lists.newArrayList(user.house.apt.id));
            title = PushMessage.BOARD_COMPLAINT_TITLE.getValue();
            String value = TagUtils.removeTag(post.content).replaceAll("<!DOCTYPE html>", StringUtils.EMPTY);
            String action = String.format(PushAction.BOARD.getValue(), post.id);

            this.pushUtils.sendPush(PushGubun.BOARD_COMPLAINT, title, value, action, String.valueOf(post.id), false, targetUserList);
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
        } else if ("community".equals(post.category.type)) {
            // 글등록 후 push 발송여부가 Y인 게시판 카테고리
            if ("Y".equals(post.category.pushAfterWrite)) {
                // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
                // List<User> users = userService.getAdminUsers(user.house.apt.id);
                //
                // GcmSendForm form = new GcmSendForm();
                // Map<String, String> msg = Maps.newHashMap();
                // msg.put("type", "action");
                // msg.put("title", post.category.name + " 게시판에 " + post.user.getFullName() + "님의 새로운 글이 등록되었습니다.");
                // msg.put("value", post.content);
                // msg.put("action", "emaul://post-detail?id=" + post.id);
                // form.setUserIds(Lists.transform(users, input -> input.id));
                // form.setMessage(msg);
                //
                // gcmService.sendGcm(form);

                List<SimpleUser> targetUserList = this.pushUtils.findPushTargetUserList(PushTargetType.APT, PushAlarmSetting.BOARD, Lists.newArrayList(user.house.apt.id));
                title = String.format(PushMessage.BOARD_POST_REG.getValue(), post.category.name, post.user.getFullName());
                String value = TagUtils.removeTag(post.content).replaceAll("<!DOCTYPE html>", StringUtils.EMPTY);
                String action = String.format(PushAction.BOARD.getValue(), post.id);

                this.pushUtils.sendPush(PushGubun.BOARD_POST, title, value, action, String.valueOf(post.id), false, targetUserList);
                // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
            }
        }

        try {
            post.user = userService.convertUserForPost(post.user, user.house.apt.id, post.user.house.dong, post.category.getUserPrivacy());
        } catch (Exception e) {
            LOGGER.error("<<(무시) 게시판 등록 중 오류>>", e);
        }

        post.user = userService.convertToPublicUser(post.user);
        return post;
    }

    @RequestMapping(value = "/api/board/post/modify", method = RequestMethod.POST)
    public @ResponseBody BoardPost modifyPost(HttpServletRequest req, @RequestParam(value = "content", required = false, defaultValue = "") String content, @RequestParam(
            value = "jsonRemovedImagePositions", required = false, defaultValue = "") String jsonRemovedImagePositions, @RequestParam(value = "postId") Long postId, @RequestParam(value = "title",
            required = false, defaultValue = "") String title, @RequestParam(value = "files", required = false) MultipartFile[] files) {
        String reqIp = req.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(reqIp)) {
            reqIp = req.getRemoteAddr();
        }

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        BoardPost post = boardService.get(user, postId);

        if (!post.isDeletable) {
            return null;
        }

        List<Integer> removedPositions = Lists.newArrayList();
        if (jsonRemovedImagePositions != null) {
            removedPositions = new Gson().fromJson(jsonRemovedImagePositions, new TypeToken<List<Integer>>() {}.getType());
        }

        long postParentNum = post.id / 1000l;

        // 기존 이미지 정리
        for (int i = 0; i < MAX_POST_IMAGE_COUNT; i++) {
            File f = new File(String.format("/nas/EMaul/board/post/image/%s/%s/%s.jpg", postParentNum, postId, i));
            File fThumb = new File(String.format("/nas/EMaul/board/post/image/%s/%s/%s-thumb.jpg", postParentNum, postId, i));
            if (removedPositions.contains(i)) {
                f.delete();
                fThumb.delete();
                continue;
            }
            for (int j = 0; j < i; j++) {
                File temp = new File(String.format("/nas/EMaul/board/post/image/%s/%s/%s.jpg", postParentNum, postId, j));
                File tempThumb = new File(String.format("/nas/EMaul/board/post/image/%s/%s/%s-thumb.jpg", postParentNum, postId, j));
                if (!temp.exists()) {
                    f.renameTo(temp);
                    fThumb.renameTo(tempThumb);
                    break;
                }
            }
        }
        final int newImageStartIndex = post.imageCount - removedPositions.size();

        post.title = title;
        post.content = content;
        post.imageCount = newImageStartIndex + (files == null ? 0 : files.length);
        post.reqIp = reqIp;
        post.modId = user.id;
        post.modDate = new Date();

        post = boardService.save(post);

        if (files != null) {
            final int len = newImageStartIndex + files.length;
            int fileIndex = 0;
            for (int i = newImageStartIndex; i < len; i++) {
                try {
                    File dir = new File(String.format("/nas/EMaul/board/post/image/%s/%s", postParentNum, postId));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File dest = new File(dir, String.format("%s.jpg", i));
                    dest.createNewFile();
                    files[fileIndex++].transferTo(dest);
                    Thumbnails.create(dest);
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
        }

        try {
            post.user = userService.convertUserForPost(post.user, user.house.apt.id, post.user.house.dong, post.category.getUserPrivacy());
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        post.user = userService.convertToPublicUser(post.user);
        return post;
    }

    @RequestMapping(value = "/api/board/post/list", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<BoardPost> getPosts(HttpServletRequest req, @RequestParam(value = "categoryId") Long categoryId, @RequestParam(value = "lastPostId", required = false,
            defaultValue = "0") Long lastPostId) {

        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        ScrollPage<BoardPost> page = boardService.getPosts(userId, categoryId, user.house.apt.address.시도명, user.house.apt.address.시군구명, lastPostId, 20);
        BoardCategory category = boardService.getCategory(categoryId);

        List<BoardPost> list = page.getContent();
        for (BoardPost post : list) {
            try {
                post.user = userService.convertUserForPost(post.user, user.house.apt.id, post.user.house.dong, category.getUserPrivacy());

                if (post.content.indexOf("<!DOCTYPE html>") > -1) {
                    post.content = post.content.replaceAll("(\r\n|\n)", "");
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }

            post.user = userService.convertToPublicUser(post.user);
        }

        return page;
    }

    // rest로 post list와 같이 쓸 수 있지만, 나중에 많이 바뀔꺼라서 이렇게 함.
    @RequestMapping(value = "/api/board/today/list")
    public @ResponseBody ScrollPage<BoardPost> getTodayPosts(HttpServletRequest req, @RequestParam(value = "categoryId") Long categoryId, @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "lastPostId", required = false, defaultValue = "0") Long lastPostId, @RequestParam(value = "newsType", required = false) String newsType, @RequestParam(
                    value = "newsCategory", required = false) String newsCategory, @RequestParam(value = "count", required = false) Integer count,
            @RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "hashtag", required = false) String hashtag, @RequestParam(value = "sort", required = false,
                    defaultValue = "RECENT") TodaySort todaySort) {

        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        // 구버전 요청
        if (StringUtils.isEmpty(newsType) && StringUtils.isEmpty(newsCategory)) {
            // tag 매핑
            newsCategory = tagToNewsCategory(tag);
            if (!StringUtils.isEmpty(newsCategory)) {
                // 구버전 요청일 경우 NEWS_TYPE 은 일반뉴스로만 조회
                newsType = Code.NEWS_TYPE_GENERAL.getCode();
            }
        }

        int postCount = 20;
        if (count != null && count > 0) {
            postCount = count;
        }

        ScrollPage<BoardPost> page =
                boardService.getTodayPosts(userId, categoryId, null, user.house.apt.address.시도명, user.house.apt.address.시군구명, lastPostId, postCount, newsType, newsCategory, keyword, null, hashtag,
                        todaySort);

        List<BoardPost> list = page.getContent();
        for (BoardPost post : list) {
            post.user = userService.convertToPublicUser(post.user);
            post.hashtags = boardService.fetchHashtags(post.id);
        }

        return page;
    }

    // rest로 post list와 같이 쓸 수 있지만, 나중에 많이 바뀔꺼라서 이렇게 함.
    @RequestMapping(value = "/api/board/today/list8", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<BoardPost> getTodayPosts(HttpServletRequest req, @RequestParam(value = "tag", required = false) String tag,
            @RequestParam(value = "newsType", required = false) String newsType, @RequestParam(value = "newsCategory", required = false) String newsCategory, @RequestParam(value = "hashtag",
                    required = false) String hashtag, @RequestParam(value = "sort", required = false, defaultValue = "RECENT") TodaySort todaySort) {

        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        List<BoardCategory> categories = boardService.getCategories(userId, "today");

        // 구버전 요청
        if (StringUtils.isEmpty(newsType) && StringUtils.isEmpty(newsCategory)) {
            // tag 매핑
            newsCategory = tagToNewsCategory(tag);
            if (!StringUtils.isEmpty(newsCategory)) {
                // 구버전 요청일 경우 NEWS_TYPE 은 일반뉴스로만 조회
                newsType = Code.NEWS_TYPE_GENERAL.getCode();
            }
        }

        ScrollPage<BoardPost> page =
                boardService.getNewsPosts(userId, categories.get(0).id, null, user.house.apt.address.시도명, user.house.apt.address.시군구명, null, 8, newsType, newsCategory, null, hashtag, todaySort);

        List<BoardPost> list = page.getContent();
        for (BoardPost post : list) {
            post.user = userService.convertToPublicUser(post.user);
            post.hashtags = boardService.fetchHashtags(post.id);
        }

        return page;
    }

    /**
     * 잠금화면 목록 조회
     *
     * @param req
     * @return
     */
    @RequestMapping(value = {"/api/public/board/today/slide"}, method = RequestMethod.GET)
    @ResponseBody
    // public ScrollPage<BoardPost> getSlidePosts(HttpServletRequest req) {
    public Map<String, Object> getSlidePosts(HttpServletRequest req) {

        Long userId = SessionAttrs.getUserId(req.getSession());

        Long categoryId = null;
        String sido = null;
        String sigungu = null;

        if (userId != null) {
            User user = userService.getUser(userId);
            List<BoardCategory> categories = boardService.getCategories(userId, "today");
            categoryId = categories.get(0).id;
            sido = user.house.apt.address.시도명;
            sigungu = user.house.apt.address.시군구명;
        }

        String slideYn = "Y";
        int listCount = 10;

        // ScrollPage<BoardPost> page = boardService.getPosts(userId, categories.get(0).id, null,
        // user.house.apt.address.시도명, user.house.apt.address.시군구명, null, listCount, null, null, slideYn);
        ScrollPage<BoardPost> page = boardService.getPosts(userId, categoryId, null, sido, sigungu, null, listCount, null, null, slideYn);

        List<BoardPost> list = page.getContent();
        for (BoardPost post : list) {
            post.user = userService.convertToPublicUser(post.user);
            post.hashtags = boardService.fetchHashtags(post.id);
        }

        // TODO: 광고 컨텐츠
        ScrollPage<?> advert = new ScrollPage<>();

        Map<String, Object> map = new HashMap<>();

        map.put("news", page);
        map.put("advert", advert);

        return map;
    }

    @RequestMapping(value = "/api/board/post/list16", method = RequestMethod.GET)
    public @ResponseBody List<BoardPost> getLatest16Posts(HttpServletRequest req) {
        List<BoardPost> posts = boardService.getFirst16Posts(SessionAttrs.getUserId(req.getSession()));
        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        if (posts != null) {
            for (BoardPost post : posts) {
                try {
                    post.user = userService.convertUserForPost(post.user, user.house.apt.id, post.user.house.dong, post.category.getUserPrivacy());
                } catch (Exception e) {
                    LOGGER.error("", e);
                }

                post.user = userService.convertToPublicUser(post.user);
            }
        }

        return posts;
    }

    @RequestMapping(value = "/api/board/comment/write", method = RequestMethod.POST)
    public @ResponseBody BoardComment writeComment(HttpServletRequest req, @RequestBody String json) {
        String reqIp = req.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(reqIp)) {
            reqIp = req.getRemoteAddr();
        }

        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        // 방문자인 경우 차단
        if (user == null || user.type == null || user.type.anonymous == true) {
            LOGGER.info("### 사용자 정보 없거나 방문객");
            return null;
        }

        JSONObject obj = new JSONObject(json);

        BoardComment cmt = new BoardComment();
        cmt.content = obj.getString("content");
        cmt.post = boardService.get(user, obj.getLong("postId"));
        cmt.regDate = new Date();
        cmt.user = userService.getUser(userId);
        cmt.displayYn = "Y";
        cmt.reqIp = reqIp;

        cmt = boardService.save(cmt);

        try {
            cmt.user = userService.convertUserForPost(cmt.user, cmt.user.house.apt.id, cmt.user.house.dong, cmt.post.category.getUserPrivacy());
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        cmt.user = userService.convertToPublicUser(cmt.user);

        if (!userId.equals(cmt.post.user.id)) {
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
            // if (cmt.post.user.setting.notiPostReply && !cmt.post.user.type.deactivated && !cmt.post.user.type.blocked) {
            // // send replied notification
            // GcmSendForm form = new GcmSendForm();
            // Map<String, String> msg = Maps.newHashMap();
            // msg.put("type", "action");
            // msg.put("value", cmt.content);
            // if ("today".equalsIgnoreCase(cmt.post.category.type)) {
            // msg.put("action", "emaul://today-detail?id=" + cmt.post.id);
            // } else {
            // msg.put("action", "emaul://post-detail?id=" + cmt.post.id);
            // }
            //
            // if ("complaint".equals(cmt.post.category.type)) {
            // msg.put("title", "접수하신 민원의 답변이 등록되었습니다.");
            // } else {
            // msg.put("titleResId", "new_comment_replied");
            // }
            //
            // form.setUserIds(Lists.newArrayList(cmt.post.user.id));
            // form.setMessage(msg);
            //
            // gcmService.sendGcm(form);
            // }

            Long postId = cmt.post.id;
            String categoryType = cmt.post.category.type;

            List<SimpleUser> targetUserList = this.pushUtils.findPushTargetUserList(PushTargetType.USER, PushAlarmSetting.BOARD, Lists.newArrayList(cmt.post.user.id));

            PushGubun pushGubun = null;
            String title = null;
            String value = cmt.content;
            String action = String.format(PushAction.BOARD.getValue(), postId);
            boolean titleResIdYn = false;

            if ("complaint".equals(categoryType)) {
                title = PushMessage.BOARD_COMPLAINT_COMMENT_TITLE.getValue();
                pushGubun = PushGubun.BOARD_COMPLAINT;
            } else {
                title = PushMessage.BOARD_COMMENT_REG.getValue();
                pushGubun = PushGubun.BOARD_COMMENT;
                titleResIdYn = true;
            }

            this.pushUtils.sendPush(pushGubun, title, value, action, String.valueOf(postId), titleResIdYn, targetUserList);
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
        }

        try {
            cmt.post.user = userService.convertUserForPost(cmt.post.user, cmt.post.user.house.apt.id, cmt.post.user.house.dong, cmt.post.category.getUserPrivacy());
        } catch (Exception e) {
            LOGGER.error("<<(무시) 게시판 댓글 작성 중 오류>>", e);
        }

        cmt.post.user = userService.convertToPublicUser(cmt.post.user);

        return cmt;
    }

    @RequestMapping(value = "/api/board/comment/modify", method = RequestMethod.POST)
    public @ResponseBody BoardComment modifyComment(HttpServletRequest req, @RequestBody String json) {
        String reqIp = req.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(reqIp)) {
            reqIp = req.getRemoteAddr();
        }

        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        // 방문자인 경우 차단
        if (user == null || user.type == null || user.type.anonymous == true)
            return null;

        JSONObject obj = new JSONObject(json);

        String content = obj.getString("content");
        Long commentId = obj.getLong("commentId");

        BoardComment cmt = boardService.getComment(user, commentId);
        if (!cmt.isDeletable) {
            return null;
        }

        cmt.content = content;
        cmt.reqIp = reqIp;
        cmt.modId = userId;
        cmt.modDate = new Date();

        cmt = boardService.save(cmt);
        cmt.user = userService.convertToPublicUser(cmt.user);
        cmt.post.user = userService.convertToPublicUser(cmt.post.user);

        return cmt;
    }

    @RequestMapping(value = "/api/board/comment-reply/write", method = RequestMethod.POST)
    public @ResponseBody BoardCommentReply writeCommentReply(HttpServletRequest req, @RequestBody String json) {
        String reqIp = req.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(reqIp)) {
            reqIp = req.getRemoteAddr();
        }

        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        // 방문자인 경우 차단
        if (user == null || user.type == null || user.type.anonymous == true) {
            LOGGER.info("### 사용자 정보 없거나 방문객");
            return null;
        }
        JSONObject obj = new JSONObject(json);

        BoardCommentReply reply = new BoardCommentReply();
        reply.content = obj.getString("content");
        reply.comment = boardService.getComment(user, obj.getLong("commentId"));
        reply.regDate = new Date();
        reply.user = userService.getUser(userId);
        reply.displayYn = "Y";
        reply.reqIp = reqIp;

        reply = boardService.save(reply);
        reply.user = userService.convertToPublicUser(reply.user);

        if (!userId.equals(reply.comment.user.id)) {
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
            // if (reply.comment.user.setting.notiPostCommentReply && !reply.comment.user.type.deactivated && !reply.comment.user.type.blocked) {
            // // send replied notification
            // GcmSendForm form = new GcmSendForm();
            // Map<String, String> msg = Maps.newHashMap();
            // msg.put("type", "action");
            // msg.put("titleResId", "new_comment_reply_added");
            // msg.put("value", reply.content);
            // msg.put("action", "emaul://comment-detail?id=" + reply.comment.id + "&postId=" + reply.comment.post.id);
            // form.setUserIds(Lists.newArrayList(reply.comment.user.id));
            // form.setMessage(msg);
            //
            // gcmService.sendGcm(form);
            // }

            Long postId = reply.comment.post.id;

            List<SimpleUser> targetUserList = this.pushUtils.findPushTargetUserList(PushTargetType.USER, PushAlarmSetting.BOARD, Lists.newArrayList(reply.comment.user.id));
            String title = PushMessage.BOARD_REPLY_REG.getValue();
            String value = reply.content;
            String action = String.format(PushAction.BOARD.getValue(), postId);

            this.pushUtils.sendPush(PushGubun.BOARD_REPLY, title, value, action, String.valueOf(postId), true, targetUserList);
            // ////////////////////////////////////////////////// GCM변경, 20161028, 전강욱 ////////////////////////////////////////////////////
        }

        reply.comment.user = userService.convertToPublicUser(reply.comment.user);
        reply.comment.post = null;

        return reply;
    }

    @RequestMapping(value = "/api/board/comment-reply/modify", method = RequestMethod.POST)
    public @ResponseBody BoardCommentReply modifyCommentReply(HttpServletRequest req, @RequestBody String json) {
        String reqIp = req.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(reqIp)) {
            reqIp = req.getRemoteAddr();
        }

        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        // 방문자인 경우 차단
        if (user == null || user.type == null || user.type.anonymous == true)
            return null;

        JSONObject obj = new JSONObject(json);

        String content = obj.getString("content");
        Long replyId = obj.getLong("replyId");

        BoardCommentReply reply = boardService.getCommentReply(user, replyId);
        reply.content = content;
        reply.reqIp = reqIp;
        reply.modId = userId;
        reply.modDate = new Date();

        reply = boardService.save(reply);

        reply.user = userService.convertToPublicUser(reply.user);
        reply.comment.user = userService.convertToPublicUser(reply.comment.user);
        reply.comment.post = null;

        return reply;
    }

    @RequestMapping(value = "/api/board/comment/list", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<BoardComment> getComments(HttpServletRequest req, @RequestParam(value = "postId") Long postId, @RequestParam(value = "lastCommentId", required = false,
            defaultValue = "0") Long lastCommentId) {

        // 방문자인 경우 차단
        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        if (user == null || user.type == null || user.type.anonymous == true) {
            LOGGER.info("### 사용자 정보 없거나 방문객");
            return null;
        }

        ScrollPage<BoardComment> page = boardService.getComments(SessionAttrs.getUserId(req.getSession()), postId, lastCommentId);

        List<BoardComment> list = page.getContent();
        for (BoardComment boardComment : list) {
            BoardCategory category = boardComment.post.category;
            boardComment.post = null;

            try {
                boardComment.user = userService.convertUserForPost(boardComment.user, user.house.apt.id, boardComment.user.house.dong, category.getUserPrivacy());
            } catch (Exception e) {
                LOGGER.error("<<(무시) 게시판 댓글 목록 조회 중 오류>>", e);
            }

            boardComment.user = userService.convertToPublicUser(boardComment.user);
        }


        return page;
    }

    @RequestMapping(value = "/api/board/comment-reply/list", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<BoardCommentReply> getCommentReplies(HttpServletRequest req, @RequestParam(value = "commentId") Long commentId, @RequestParam(value = "lastCommentReplyId",
            required = false, defaultValue = "0") Long lastCommentReplyId) {

        // 방문자인 경우 차단
        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        if (user == null || user.type == null || user.type.anonymous == true)
            return null;

        ScrollPage<BoardCommentReply> page = boardService.getCommentReplies(SessionAttrs.getUserId(req.getSession()), commentId, lastCommentReplyId);

        List<BoardCommentReply> list = page.getContent();
        for (BoardCommentReply reply : list) {
            BoardCategory category = reply.comment.post.category;
            reply.comment = null;

            try {
                reply.user = userService.convertUserForPost(reply.user, user.house.apt.id, reply.user.house.dong, category.getUserPrivacy());
            } catch (Exception e) {
                LOGGER.error("<<(무시) 게시판 답글 목록 조회 중 오류>>", e);
            }

            reply.user = userService.convertToPublicUser(reply.user);
        }

        return page;
    }

    @RequestMapping(value = "/api/board/{type}/block/{doBlock}/{id}", method = RequestMethod.PUT)
    public @ResponseBody String handleBlock(HttpServletRequest req, @PathVariable("type") String type, @PathVariable("doBlock") Boolean block, @PathVariable("id") Long id) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if (!user.type.admin && !user.type.jaha) {
            return "1";
        }

        if ("post".equalsIgnoreCase(type)) {
            boardService.handleBlockPost(id, block);
        } else if ("comment".equalsIgnoreCase(type)) {
            boardService.handleBlockComment(id, block);
        } else if ("reply".equalsIgnoreCase(type)) {
            boardService.handleBlockCommentReply(id, block);
        }

        return "0";
    }

    @RequestMapping(value = "/api/board/empathy/{type}/{postId}", method = RequestMethod.GET)
    public @ResponseBody String toggleEmpathy(HttpServletRequest req, @PathVariable("postId") Long postId, @PathVariable("type") String type) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        if ("on".equalsIgnoreCase(type)) {
            boardService.setEmpathy(user.id, postId, true);
            return "1";
        } else if ("off".equalsIgnoreCase(type)) {
            boardService.setEmpathy(user.id, postId, false);
            return "0";
        } else if ("check".equalsIgnoreCase(type)) {
            return boardService.isAlreadyEmpathy(user.id, postId) ? "1" : "0";
        }

        return "0";
    }


    @RequestMapping(value = "/api/board/inc-view-count/{postId}", method = RequestMethod.PUT)
    public void increasePostViewCount(@PathVariable(value = "postId") Long postId) {
        boardService.increasePostViewCount(postId);
    }

    @RequestMapping(value = "/api/board/post/delete/{postId}", method = RequestMethod.DELETE)
    public @ResponseBody String deletePost(HttpServletRequest req, @PathVariable(value = "postId") Long postId) {
        String reqIp = req.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(reqIp)) {
            reqIp = req.getRemoteAddr();
        }

        return boardService.deletePost(SessionAttrs.getUserId(req.getSession()), postId, reqIp) ? "1" : "0";
    }

    @RequestMapping(value = "/api/board/comment/delete/{commentId}", method = RequestMethod.DELETE)
    public @ResponseBody String deleteComment(HttpServletRequest req, @PathVariable(value = "commentId") Long commentId) {
        String reqIp = req.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(reqIp)) {
            reqIp = req.getRemoteAddr();
        }

        // 방문자인 경우 차단
        Long userId = SessionAttrs.getUserId(req.getSession());
        User user = userService.getUser(userId);

        if (user == null || user.type == null || user.type.anonymous == true)
            return null;

        return boardService.deleteComment(SessionAttrs.getUserId(req.getSession()), commentId, reqIp) ? "1" : "0";
    }

    @RequestMapping(value = "/api/board/comment-reply/delete/{replyId}", method = RequestMethod.DELETE)
    public @ResponseBody String deleteCommentReply(HttpServletRequest req, @PathVariable(value = "replyId") Long replyId) {
        String reqIp = req.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isEmpty(reqIp)) {
            reqIp = req.getRemoteAddr();
        }

        Long userId = SessionAttrs.getUserId(req.getSession());

        // 방문자인 경우 차단
        User user = userService.getUser(userId);

        if (user == null || user.type == null || user.type.anonymous == true)
            return null;

        return boardService.deleteCommentReply(userId, replyId, reqIp) ? "1" : "0";
    }

    @RequestMapping(value = "/api/board/post/{type}/{postId}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleImageRequest(@PathVariable("type") String type, @PathVariable("postId") String postId, @PathVariable("fileName") String fileName) {

        try {
            String decFilename = URLDecoder.decode(fileName, "utf-8");
            File toServeUp = new File("/nas/EMaul/board/post/" + type, String.format("/%s/%s/%s", Long.valueOf(postId) / 1000l, postId, decFilename));

            return Responses.getFileEntity(toServeUp, postId + "-" + fileName);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("<<게시판 이미지 조회 중 오류>>", e);
        }
        return null;
    }

    /**
     * 로그인 체크 없이 이미지 보기
     *
     * @param postId
     * @param fileName
     * @return
     */
    @RequestMapping(value = "/api/public/board/post/image/{postId}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handlePublicImageRequest(@PathVariable("postId") String postId, @PathVariable("fileName") String fileName) {

        try {
            List<String> categoryType = boardService.getCategoryType(postId);
            if (categoryType != null && !categoryType.isEmpty()) {
                // today 만 로그인 체크 없이 이미지 보기
                if (categoryType.contains("today")) {
                    String decFilename = URLDecoder.decode(fileName, "utf-8");
                    File toServeUp = new File("/nas/EMaul/board/post/image", String.format("/%s/%s/%s", Long.valueOf(postId) / 1000l, postId, decFilename));
                    return Responses.getFileEntity(toServeUp, postId + "-" + fileName);
                }
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("<<게시판 이미지 조회 중 오류>>", e);
        }
        return null;
    }

    /**
     * 아파트별로 이미지와 파일을 리턴한다. 서수원자이 홈페이지에서 이관한 데이터, 이미지, 파일을 처리하기 위해서... 일단 웹에서도 접근해야하니 비로그인 접속 가능하게 (2016.06.30)
     *
     * @author PNS
     * @param aptId
     * @param type
     * @param fileName
     * @return
     */
    @RequestMapping(value = "/api/public/files/{aptId}/{type}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleFileRequestByApt(@PathVariable("aptId") String aptId, @PathVariable("type") String type, @PathVariable("fileName") String fileName) {

        try {
            String decFilename = URLDecoder.decode(fileName, "utf-8");
            File file = new File("/nas/EMaul/files", String.format("/%s/%s/%s", aptId, type, decFilename));
            // Local PC TEST
            // File file = new File("D:\\nas\\EMaul\\files", String.format("\\%s\\%s\\%s", aptId, type, decFilename));

            return Responses.getFileEntity(file, aptId + "-" + fileName);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("<<게시판 이미지 조회 중 오류>>", e);
        }
        return null;
    }

    /**
     * 뉴스 카테고리 목록 조회
     *
     * @return
     */
    @RequestMapping(value = "/api/board/news-categories/get", method = RequestMethod.GET)
    @ResponseBody
    public ApiResponse<?> getNewsCategories() {
        List<CommonCode> items = new ArrayList<CommonCode>();
        CommonCode allCode = new CommonCode();
        allCode.setCode("ALL");
        allCode.setName("전체");
        allCode.setSortOrder(0);
        items.add(allCode);
        List<CommonCode> newsCategories = boardService.getNewsCategories();
        items.addAll(newsCategories);
        Map<String, Object> map = new HashMap<>();
        map.put("items", items);

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
        apiResponse.setBody(map);

        return apiResponse;
    }

    /*
     * 구버전 tag 매핑 오늘의 소식 -> 생활 우리동네 맛집 -> 맛집
     */
    private String tagToNewsCategory(String tag) {

        if (StringUtils.isEmpty(tag)) {
            return null;
        }

        String newsCategory = null;
        if ("오늘의 소식".equals(tag)) {
            newsCategory = Code.NEWS_CATEGORY_LIFE.getCode();
        } else if ("우리동네 맛집".equals(tag)) {
            newsCategory = Code.NEWS_CATEGORY_FOOD.getCode();
        } else {
            // 사용하지 않는 태그가 들어올 경우 없는 카테고리로 변환
            newsCategory = "XXXX";
        }
        LOGGER.debug("### 구버전 tag 매핑 : [{}] ==> [{}]", tag, newsCategory);
        return newsCategory;
    }
}
