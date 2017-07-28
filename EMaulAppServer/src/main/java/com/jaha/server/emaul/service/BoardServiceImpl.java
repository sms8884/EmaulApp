package com.jaha.server.emaul.service;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.jaha.server.emaul.common.code.Code;
import com.jaha.server.emaul.constants.TodaySort;
import com.jaha.server.emaul.model.BoardCategory;
import com.jaha.server.emaul.model.BoardComment;
import com.jaha.server.emaul.model.BoardCommentReply;
import com.jaha.server.emaul.model.BoardEmpathy;
import com.jaha.server.emaul.model.BoardPost;
import com.jaha.server.emaul.model.CommonCode;
import com.jaha.server.emaul.model.Hashtag;
import com.jaha.server.emaul.model.House;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.UserNickname;
import com.jaha.server.emaul.repo.BoardCategoryRepository;
import com.jaha.server.emaul.repo.BoardCommentReplyRepository;
import com.jaha.server.emaul.repo.BoardCommentRepository;
import com.jaha.server.emaul.repo.BoardEmpathyRepository;
import com.jaha.server.emaul.repo.BoardPostRepository;
import com.jaha.server.emaul.repo.CommonCodeRepository;
import com.jaha.server.emaul.repo.HashtagRepository;
import com.jaha.server.emaul.repo.UserRepository;
import com.jaha.server.emaul.util.HtmlUtil;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.Util;
import com.jaha.server.emaul.v2.constants.CommonConstants.AppMainAlarm;

/**
 * Created by doring on 15. 3. 9..
 */
@Service
public class BoardServiceImpl implements BoardService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String DISP_Y = "Y";


    @Autowired
    private BoardCategoryRepository boardCategoryRepository;
    @Autowired
    private BoardPostRepository boardPostRepository;
    @Autowired
    private BoardCommentRepository boardCommentRepository;
    @Autowired
    private BoardCommentReplyRepository boardCommentReplyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BoardEmpathyRepository boardEmpathyRepository;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public BoardPost save(BoardPost post) {
        return boardPostRepository.saveAndFlush(post);
    }

    @Override
    public BoardComment save(BoardComment comment) {
        // this.boardPostRepository.updateCommentCountPlus(comment.post.id);
        return boardCommentRepository.saveAndFlush(comment);
    }

    @Override
    public BoardCommentReply save(BoardCommentReply reply) {
        // this.boardCommentRepository.updateReplyCountPlus(reply.comment.id);
        return boardCommentReplyRepository.saveAndFlush(reply);
    }

    @Override
    public BoardPost get(User user, long postId) {
        BoardPost post = boardPostRepository.findByIdAndDisplayYn(postId, DISP_Y);
        if (post == null) {
            return null;
        }

        // 카드뉴스 이미지 변환
        convertCardNewsImage(post);

        post.isDeletable = user.type.jaha || post.user.id.equals(user.id);

        return post;
    }

    @Override
    public BoardComment getComment(User user, long commentId) {
        BoardComment cmt = boardCommentRepository.findOne(commentId);
        if (cmt == null) {
            return null;
        }
        cmt.isDeletable = user.type.jaha || cmt.user.id.equals(user.id);

        return cmt;
    }

    @Override
    public BoardCommentReply getCommentReply(User user, long replyId) {
        BoardCommentReply reply = boardCommentReplyRepository.findOne(replyId);
        if (reply == null) {
            return null;
        }
        reply.isDeletable = user.type.jaha || reply.user.id.equals(user.id);

        return reply;
    }

    @Override
    public BoardCategory getCategory(Long id) {
        return boardCategoryRepository.findOne(id);
    }

    @Override
    public List<String> getCategoryType(String postId) {
        List<String> list =
                jdbcTemplate.query("SELECT c.type as type FROM board_post p LEFT JOIN board_category c ON c.id = p.category_id WHERE p.id = ?", new Object[] {postId}, new RowMapper<String>() {
                    @Override
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        String categoryName = rs.getString("type");
                        return categoryName;
                    }
                });
        return list;
    }

    @Override
    public List<BoardCategory> getCategories(Long userId, String type) {

        final User user = userRepository.findOne(userId);
        // List<BoardCategory> list = boardCategoryRepository.findByTypeAndAptId(type, user.house.apt.id, new Sort(Sort.Direction.ASC, "ord"));
        List<BoardCategory> list = boardCategoryRepository.findByTypeAndAptIdAndDelYn(type, user.house.apt.id, "N", new Sort(Sort.Direction.ASC, "ord"));

        for (BoardCategory boardCategory : list) {
            boardCategory.isWritable = boardCategory.isUserWritable(user);
        }

        return Lists.newArrayList(Collections2.filter(list, new Predicate<BoardCategory>() {
            @Override
            public boolean apply(BoardCategory input) {
                return input.isUserReadable(user);
            }
        }));
    }


    @Override
    public List<BoardCategory> getCategories(String type) {

        List<BoardCategory> list = boardCategoryRepository.findByTypeAndDelYn(type, "N", new Sort(Sort.Direction.ASC, "ord"));
        return list;
    }



    @Override
    public ScrollPage<BoardPost> getPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count) {
        return getPosts(userId, categoryId, tag, sido, sigungu, lastPostId, count, null, null, null, null);
    }

    @Override
    public ScrollPage<BoardPost> getPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count, String newsType, String newsCategory, String slideYn) {
        return getPosts(userId, categoryId, tag, sido, sigungu, lastPostId, count, newsType, newsCategory, slideYn, null);
    }

    @Override
    public ScrollPage<BoardPost> getPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count, String newsType, String newsCategory, String slideYn,
            String hashtag) {
        return getPosts(userId, categoryId, tag, sido, sigungu, lastPostId, count, newsType, newsCategory, slideYn, null, TodaySort.RECENT);
    }

    @Override
    public ScrollPage<BoardPost> getPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count, String newsType, String newsCategory, String slideYn,
            String hashtag, TodaySort todaySort) {

        if (lastPostId == null || lastPostId == 0l) {
            lastPostId = Long.MAX_VALUE;
        }

        User user = null;
        String categoryType = null;

        if (userId == null && "Y".equalsIgnoreCase(slideYn)) {
            // 비로그인 상태, 잠금화면에서 들어왔을 경우 기본 categoryType 은 today 로 설정
            categoryType = "today";
        } else {
            user = userRepository.findOne(userId);
            categoryType = boardCategoryRepository.findOne(categoryId).type;
        }

        List<Object> paramList = new ArrayList<>();
        paramList.add(categoryId);
        paramList.add(sido);
        paramList.add(sigungu);
        paramList.add(categoryType);
        paramList.add(lastPostId);

        String sqlTagFrom = "";
        String sqlTagSelect = "";
        if (tag != null) {
            // logger.info("### tag parma ###");
            sqlTagFrom = ", board_post_has_tag l ";
            // sqlTagSelect = " AND b.id = l.post_id AND l.tag = '" + tag + "' ";
            sqlTagSelect = " AND b.id = l.post_id AND l.tag = ? ";
            paramList.add(tag);
        }

        if (!Strings.isNullOrEmpty(hashtag)) {
            // logger.info("### tag parma ###");
            sqlTagFrom = ", board_post_hashtag l ";
            // sqlTagSelect = " AND b.id = l.post_id AND l.tag = '" + tag + "' ";
            sqlTagSelect = " AND b.id = l.post_id AND l.name = ? ";
            paramList.add(hashtag);
        }


        // =====================================================================
        // 마을뉴스 [S]

        String sqlNews = "";

        if ("today".equals(categoryType)) {
            /*
             * [2016.05.17] 뉴스 카테고리 검색조건 추가 게시판 출력 여부 조건 추가
             */
            if (!StringUtils.isEmpty(newsType)) {
                sqlNews += " AND b.news_type = ? ";
                paramList.add(newsType);
            }
            if (StringUtils.isEmpty(newsCategory)) {
                sqlNews += " AND b.news_category in (select code from common_code where code_group = ? and use_yn = 'Y') ";
                paramList.add(Code.CODE_GROUP_NEWS_CATEGORY.getCode());
            } else {
                sqlNews += " AND b.news_category = getUsedCode(?,?) ";
                paramList.add(Code.CODE_GROUP_NEWS_CATEGORY.getCode());
                paramList.add(newsCategory);
            }

            // 잠금화면 최신뉴스
            if ("Y".equalsIgnoreCase(slideYn)) {
                sqlNews += " AND b.slide_yn = 'Y' ";

                // 성별,연령은 잠금화면에서만 조회
                if (user != null) {
                    String userGenderCode = getGenderCode(user.gender);
                    String userAgeCode = getYearToAgeCode(user.birthYear);

                    // 성별
                    if (!StringUtils.isEmpty(userGenderCode)) {
                        sqlNews += " AND (b.gender is null OR " + "      b.gender = 'ALL' OR " + "      b.gender = ?) ";
                        paramList.add(userGenderCode);
                    }

                    // 연령
                    if (!StringUtils.isEmpty(userAgeCode)) {
                        sqlNews += " AND (b.age is null OR " + "      b.age = 'ALL' OR " + "      INSTR(b.age, ?) > 0) ";
                        paramList.add(userAgeCode);
                    }
                }
            }

            // 현재 출력여부는 news 에서만 사용
            sqlNews += " AND b.display_yn = 'Y' ";
        }
        // 마을뉴스 [E]
        // =====================================================================

        ScrollPage<BoardPost> ret = new ScrollPage<>();


        String orderBy = "";
        if (categoryType.equals("notice")) {

            orderBy = " ORDER BY b.top_fix DESC, b.reg_date DESC LIMIT ";

        } else {
            if (todaySort == TodaySort.RECENT)
                orderBy = " ORDER BY b.reg_date DESC LIMIT ";
            else if (todaySort == TodaySort.POPULAR)
                orderBy = " ORDER BY b.view_count DESC LIMIT ";
        }


        List<BoardPost> list = jdbcTemplate.query(
                "SELECT b.*, u.id as 'u_user_id', u.nickname, u.has_profile_image, u.full_name, h.dong, h.ho, c.name AS 'category_name', c.content_mode "
                        + "FROM board_post b, user u, board_category c, house h " + sqlTagFrom + " WHERE b.display_yn = 'Y' AND (b.category_id=? OR \n"
                        + "((b.range_sido=IF(b.range_sigungu='', ?, null) OR b.range_sigungu=? OR b.range_all=1) AND c.type = ?)) "
                        + "AND b.id < ? AND b.user_id = u.id AND b.category_id=c.id AND u.house_id = h.id " + sqlTagSelect + sqlNews + orderBy + count,
                paramList.toArray(), new RowMapper<BoardPost>() {
                    // new Object[]{categoryId, sido, sigungu, categoryType, lastPostId}, new RowMapper<BoardPost>() {
                    @Override
                    public BoardPost mapRow(ResultSet rs, int rowNum) throws SQLException {
                        BoardPost post = new BoardPost();
                        post.id = rs.getLong("id");
                        post.content = rs.getString("content");
                        post.imageCount = rs.getInt("image_count");
                        post.rangeSido = rs.getString("range_sido");
                        post.rangeSigungu = rs.getString("range_sigungu");
                        post.rangeAll = rs.getBoolean("range_all");
                        post.regDate = new Date(rs.getTimestamp("reg_date").getTime());
                        post.file1 = rs.getString("file1");
                        post.file2 = rs.getString("file2");
                        post.viewCount = rs.getLong("view_count");
                        post.countEmpathy = rs.getLong("count_empathy");
                        post.blocked = rs.getBoolean("blocked");
                        post.commentCount = rs.getLong("comment_count");
                        post.title = rs.getString("title");
                        post.topFix = rs.getBoolean("top_fix");


                        // [2016.05.17] 뉴스구분, 뉴스카테고리 항목 추가
                        post.newsType = rs.getString("news_type");
                        post.newsCategory = rs.getString("news_category");

                        User newUser = new User();
                        UserNickname nickname = new UserNickname();
                        nickname.name = rs.getString("nickname");

                        newUser.id = rs.getLong("u_user_id");
                        newUser.setNickname(nickname.name == null ? null : nickname);
                        newUser.setFullNameRawData(rs.getString("full_name"));
                        newUser.hasProfileImage = rs.getBoolean("has_profile_image");
                        post.user = newUser;

                        BoardCategory category = new BoardCategory();
                        category.name = rs.getString("category_name");
                        category.contentMode = rs.getString("content_mode");
                        post.category = category;

                        // 게시판 카테고리 모드가 html이고 게시판 title이 비어있는 경우에만 게시판 내용을 변환하여 타이틀로 수정
                        if ("html".equals(post.category.contentMode) && StringUtils.isBlank(rs.getString("title"))) {
                            String tempContent = post.content;
                            String tempTitle = HtmlUtil.removeTag(tempContent);

                            if (tempTitle.length() > 300) {
                                post.title = tempTitle.substring(0, 300);
                            } else {
                                post.title = tempTitle;
                            }
                        } else {
                            post.title = rs.getString("title");
                        }

                        House house = new House();
                        house.dong = rs.getString("dong");
                        post.user.house = house;

                        // [2016.05.17] 카드뉴스 이미지 추출 추가
                        convertCardNewsImage(post);

                        return post;
                    }
                });

        ret.setContent(list);

        final int size = list.size();
        if (size >= count) {
            ret.setNextPageToken(String.valueOf(list.get(size - 1).id));
        }
        if (user != null) {
            for (BoardPost post : list) {
                post.isDeletable = user.type.jaha || user.id.equals(post.user == null ? 0l : post.user.id);
            }
        }

        return ret;
    }

    // 장기적으로는 게시판과 뉴스를 분리하는 방향으로 하기 위해서 일단은 getPosts()를 getNewsPosts()로 하나 더 만듬 by PNS 20160930
    @Override
    public ScrollPage<BoardPost> getNewsPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count, String newsType, String newsCategory, String slideYn,
            String hashtag, TodaySort todaySort) {

        if (lastPostId == null || lastPostId == 0l) {
            lastPostId = Long.MAX_VALUE;
        }

        User user = null;
        String categoryType = null;

        if (userId == null && "Y".equalsIgnoreCase(slideYn)) {
            // 비로그인 상태, 잠금화면에서 들어왔을 경우 기본 categoryType 은 today 로 설정
            categoryType = "today";
        } else {
            user = userRepository.findOne(userId);
            categoryType = boardCategoryRepository.findOne(categoryId).type;
        }

        List<Object> paramList = new ArrayList<>();
        paramList.add(categoryId);
        paramList.add(sido);
        paramList.add(sigungu);
        paramList.add(categoryType);
        paramList.add(lastPostId);

        String sqlTagFrom = "";
        String sqlTagSelect = "";
        if (tag != null) {
            // logger.info("### tag parma ###");
            sqlTagFrom = ", board_post_has_tag l ";
            // sqlTagSelect = " AND b.id = l.post_id AND l.tag = '" + tag + "' ";
            sqlTagSelect = " AND b.id = l.post_id AND l.tag = ? ";
            paramList.add(tag);
        }

        if (!Strings.isNullOrEmpty(hashtag)) {
            // logger.info("### tag parma ###");
            sqlTagFrom = ", board_post_hashtag l ";
            // sqlTagSelect = " AND b.id = l.post_id AND l.tag = '" + tag + "' ";
            sqlTagSelect = " AND b.id = l.post_id AND l.name = ? ";
            paramList.add(hashtag);
        }


        // =====================================================================
        // 마을뉴스 [S]

        String sqlNews = "";

        if ("today".equals(categoryType)) {
            /*
             * [2016.05.17] 뉴스 카테고리 검색조건 추가 게시판 출력 여부 조건 추가
             */
            if (!StringUtils.isEmpty(newsType)) {
                sqlNews += " AND b.news_type = ? ";
                paramList.add(newsType);
            }
            if (StringUtils.isEmpty(newsCategory)) {
                sqlNews += " AND b.news_category in (select code from common_code where code_group = ? and use_yn = 'Y') ";
                paramList.add(Code.CODE_GROUP_NEWS_CATEGORY.getCode());
            } else {
                sqlNews += " AND b.news_category = getUsedCode(?,?) ";
                paramList.add(Code.CODE_GROUP_NEWS_CATEGORY.getCode());
                paramList.add(newsCategory);
            }

            // 잠금화면 최신뉴스
            if ("Y".equalsIgnoreCase(slideYn)) {
                sqlNews += " AND b.slide_yn = 'Y' ";

                // 성별,연령은 잠금화면에서만 조회
                if (user != null) {
                    String userGenderCode = getGenderCode(user.gender);
                    String userAgeCode = getYearToAgeCode(user.birthYear);

                    // 성별
                    if (!StringUtils.isEmpty(userGenderCode)) {
                        sqlNews += " AND (b.gender is null OR " + "      b.gender = 'ALL' OR " + "      b.gender = ?) ";
                        paramList.add(userGenderCode);
                    }

                    // 연령
                    if (!StringUtils.isEmpty(userAgeCode)) {
                        sqlNews += " AND (b.age is null OR " + "      b.age = 'ALL' OR " + "      INSTR(b.age, ?) > 0) ";
                        paramList.add(userAgeCode);
                    }
                }
            }

            // 현재 출력여부는 news 에서만 사용
            sqlNews += " AND b.display_yn = 'Y' ";
        }
        // 마을뉴스 [E]
        // =====================================================================

        ScrollPage<BoardPost> ret = new ScrollPage<>();


        String orderBy = "";
        if (categoryType.equals("notice")) {

            orderBy = " ORDER BY b.top_fix DESC, b.reg_date DESC LIMIT ";

        } else {
            if (todaySort == TodaySort.RECENT)
                orderBy = " ORDER BY b.reg_date DESC LIMIT ";
            else if (todaySort == TodaySort.POPULAR)
                orderBy = " ORDER BY b.view_count DESC LIMIT ";
        }


        List<BoardPost> list = jdbcTemplate.query("SELECT b.*, u.id as 'u_user_id', u.nickname, u.has_profile_image, u.full_name, h.dong, h.ho, c.name AS 'category_name', c.content_mode "
                + "FROM board_post b, user u, board_category c, house h " + sqlTagFrom + " WHERE b.display_yn = 'Y' AND (b.category_id=? OR \n"
                // [START] 단체관리자 기능 추가 : 마을 뉴스 리스트 by PNS 2016.09.26
                // 마을뉴스 타겟주소 구조가 '경기도'의 경우 '수원시 권선구' 시/구가 같이 있는 구조라서 '수원시' 전체에 권한을 주기가 힘듬 ==> 문자열에 들어있는지만 체크하는 것으로 수정하고 range_all은 제일 먼제 체크하게 수정함
                + "(b.range_all=1 OR (b.range_sido=IF(b.range_sigungu='', ?, null) OR (b.range_sigungu!='' AND LOCATE(b.range_sigungu, ?) > 0)) AND c.type = ?)) "
                // [END]
                + "AND b.id < ? AND b.user_id = u.id AND b.category_id=c.id AND u.house_id = h.id " + sqlTagSelect + sqlNews + orderBy + count, paramList.toArray(), new RowMapper<BoardPost>() {
                    // new Object[]{categoryId, sido, sigungu, categoryType, lastPostId}, new RowMapper<BoardPost>() {
                    @Override
                    public BoardPost mapRow(ResultSet rs, int rowNum) throws SQLException {
                        BoardPost post = new BoardPost();
                        post.id = rs.getLong("id");
                        post.content = rs.getString("content");
                        post.imageCount = rs.getInt("image_count");
                        post.rangeSido = rs.getString("range_sido");
                        post.rangeSigungu = rs.getString("range_sigungu");
                        post.rangeAll = rs.getBoolean("range_all");
                        post.regDate = new Date(rs.getTimestamp("reg_date").getTime());
                        post.file1 = rs.getString("file1");
                        post.file2 = rs.getString("file2");
                        post.viewCount = rs.getLong("view_count");
                        post.countEmpathy = rs.getLong("count_empathy");
                        post.blocked = rs.getBoolean("blocked");
                        post.commentCount = rs.getLong("comment_count");
                        post.title = rs.getString("title");
                        post.topFix = rs.getBoolean("top_fix");


                        // [2016.05.17] 뉴스구분, 뉴스카테고리 항목 추가
                        post.newsType = rs.getString("news_type");
                        post.newsCategory = rs.getString("news_category");

                        User newUser = new User();
                        UserNickname nickname = new UserNickname();
                        nickname.name = rs.getString("nickname");

                        newUser.id = rs.getLong("u_user_id");
                        newUser.setNickname(nickname.name == null ? null : nickname);
                        newUser.setFullNameRawData(rs.getString("full_name"));
                        newUser.hasProfileImage = rs.getBoolean("has_profile_image");
                        post.user = newUser;

                        BoardCategory category = new BoardCategory();
                        category.name = rs.getString("category_name");
                        category.contentMode = rs.getString("content_mode");
                        post.category = category;

                        // 게시판 카테고리 모드가 html이고 게시판 title이 비어있는 경우에만 게시판 내용을 변환하여 타이틀로 수정
                        if ("html".equals(post.category.contentMode) && StringUtils.isBlank(rs.getString("title"))) {
                            String tempContent = post.content;
                            String tempTitle = HtmlUtil.removeTag(tempContent);

                            if (tempTitle.length() > 300) {
                                post.title = tempTitle.substring(0, 300);
                            } else {
                                post.title = tempTitle;
                            }
                        } else {
                            post.title = rs.getString("title");
                        }

                        House house = new House();
                        house.dong = rs.getString("dong");
                        post.user.house = house;

                        // [2016.05.17] 카드뉴스 이미지 추출 추가
                        convertCardNewsImage(post);

                        return post;
                    }
                });

        ret.setContent(list);

        final int size = list.size();
        if (size >= count) {
            ret.setNextPageToken(String.valueOf(list.get(size - 1).id));
        }
        if (user != null) {
            for (BoardPost post : list) {
                post.isDeletable = user.type.jaha || user.id.equals(post.user == null ? 0l : post.user.id);
            }
        }

        return ret;
    }

    @Override
    public ScrollPage<BoardPost> getPosts(Long userId, Long categoryId, String sido, String sigungu, Long lastPostId, int count) {
        return getPosts(userId, categoryId, null, sido, sigungu, lastPostId, count);
    }

    @Override
    public List<BoardPost> getFirst16Posts(Long userId) {
        User user = userRepository.findOne(userId);

        // 방문자인 경우 차단
        if (user == null || user.type == null || user.type.anonymous == true)
            return null;

        List<BoardCategory> categories = boardCategoryRepository.findByTypeAndAptIdAndDelYn("community", user.house.apt.id, "N");
        List<BoardCategory> categoriesNotice = boardCategoryRepository.findByTypeAndAptIdAndDelYn("notice", user.house.apt.id, "N");
        categories.addAll(categoriesNotice);
        List<Long> categoryIds = Lists.transform(categories, input -> input.id);

        List<BoardPost> posts = boardPostRepository.findFirst16ByCategoryIdInAndDisplayYn(categoryIds, new Sort(Sort.Direction.DESC, "regDate"), DISP_Y);

        for (BoardPost post : posts) {
            post.isDeletable = user.type.jaha || user.id.equals(post.user == null ? 0l : post.user.id);
        }

        return posts;
    }

    @Override
    public ScrollPage<BoardComment> getComments(Long userId, Long postId, Long lastCommentId) {
        if (lastCommentId == null || lastCommentId == 0l) {
            lastCommentId = Long.MAX_VALUE;
        }
        User user = userRepository.findOne(userId);

        ScrollPage<BoardComment> ret = new ScrollPage<>();
        // List<BoardComment> list = boardCommentRepository.findFirst20ByPostIdAndIdLessThan(postId, lastCommentId, new Sort(Sort.Direction.DESC, "id")); // regDate
        List<BoardComment> list = boardCommentRepository.findFirst20ByPostIdAndIdLessThanAndDisplayYn(postId, lastCommentId, "Y", new Sort(Sort.Direction.DESC, "id")); // regDate
        ret.setContent(list);
        final int size = list.size();
        if (size >= 20) {
            ret.setNextPageToken(String.valueOf(list.get(size - 1).id));
        }
        if (user != null) {
            for (BoardComment comment : list) {
                comment.isDeletable = user.type.jaha || user.id.equals(comment.user == null ? 0l : comment.user.id);
            }
        }
        return ret;
    }

    @Override
    public ScrollPage<BoardCommentReply> getCommentReplies(Long userId, Long commentId, Long lastCommentReplyId) {
        if (lastCommentReplyId == null || lastCommentReplyId == 0l) {
            lastCommentReplyId = Long.MAX_VALUE;
        }
        User user = userRepository.findOne(userId);

        ScrollPage<BoardCommentReply> ret = new ScrollPage<>();
        // List<BoardCommentReply> list = boardCommentReplyRepository.findFirst20ByCommentIdAndIdLessThan(commentId, lastCommentReplyId, new Sort(Sort.Direction.DESC, "id")); // regDate
        List<BoardCommentReply> list = boardCommentReplyRepository.findFirst20ByCommentIdAndIdLessThanAndDisplayYn(commentId, lastCommentReplyId, "Y", new Sort(Sort.Direction.DESC, "id")); // regDate
        ret.setContent(list);
        final int size = list.size();
        if (size >= 20) {
            ret.setNextPageToken(String.valueOf(list.get(size - 1).id));
        }
        if (user != null) {
            for (BoardCommentReply reply : list) {
                reply.isDeletable = user.type.jaha || user.id.equals(reply.user == null ? 0l : reply.user.id);
            }
        }
        return ret;
    }

    @Override
    public void increasePostViewCount(Long postId) {
        jdbcTemplate.update("UPDATE board_post SET view_count=view_count+1 WHERE id=?", postId);
    }

    @Override
    @Transactional
    public Boolean deletePost(Long userId, Long postId, String reqIp) {
        User user = userRepository.findOne(userId);
        BoardPost post = boardPostRepository.findByIdAndDisplayYn(postId, DISP_Y);

        if (user != null && post != null && post.user != null) {
            if (user.type.jaha || user.id.equals(post.user.id)) {
                if (post.imageCount != null && post.imageCount != 0) {
                    // deletePostImages(post);
                }

                post.delete(userId, reqIp);
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    private void deletePostImages(BoardPost post) {
        long postId = post.id;
        long postParentNum = post.id / 1000l;

        File dir = new File(String.format("/nas/EMaul/board/post/image/%s/%s", postParentNum, postId));
        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                file.delete();
            }
        }
        dir.delete();
    }

    @Override
    public Boolean deleteComment(Long userId, Long commentId, String reqIp) {
        User user = userRepository.findOne(userId);
        BoardComment comment = boardCommentRepository.findOne(commentId);

        if (user != null && comment != null && comment.user != null) {
            if (user.type.jaha || user.id.equals(comment.user.id)) {
                this.boardCommentRepository.updateCommentHide(commentId, reqIp, userId);
                this.boardPostRepository.updateCommentCountMinus(comment.post.id);
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean deleteCommentReply(Long userId, Long replyId, String reqIp) {
        User user = userRepository.findOne(userId);
        BoardCommentReply reply = boardCommentReplyRepository.findOne(replyId);

        if (user != null && reply != null && reply.user != null) {
            if (user.type.jaha || user.type.admin || user.id.equals(reply.user.id)) {
                this.boardCommentReplyRepository.updateCommentReplyHide(replyId, reqIp, userId);
                this.boardCommentRepository.updateReplyCountMinus(reply.comment.id);
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean isAlreadyEmpathy(Long userId, Long postId) {
        return boardEmpathyRepository.findOneByUserIdAndPostId(userId, postId) != null;
    }

    @Override
    public void setEmpathy(Long userId, Long postId, boolean isEmpathy) {
        if (isEmpathy) {
            if (!isAlreadyEmpathy(userId, postId)) {
                boardEmpathyRepository.save(new BoardEmpathy(userId, postId));
            }
        } else {
            List<BoardEmpathy> list = boardEmpathyRepository.findByUserIdAndPostId(userId, postId);
            boardEmpathyRepository.delete(list);
        }
    }

    @Override
    public void handleBlockPost(Long postId, boolean block) {
        boardPostRepository.setBlocked(postId, block);
    }

    @Override
    public void handleBlockComment(Long commentId, boolean block) {
        boardCommentRepository.setBlocked(commentId, block);
    }

    @Override
    public void handleBlockCommentReply(Long replyId, boolean block) {
        boardCommentReplyRepository.setBlocked(replyId, block);
    }

    @Override
    public List<CommonCode> getNewsCategories() {
        return commonCodeRepository.findByCodeGroupAndUseYnOrderBySortOrderAsc(Code.CODE_GROUP_NEWS_CATEGORY.getCode(), "Y");
    }


    private String getYearToAgeCode(String year) {
        if (!StringUtils.isEmpty(year)) {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            int intYear = Util.getInt(year);

            if (intYear > 0) {
                cal1.set(Calendar.YEAR, intYear);
                cal2.setTime(new Date());

                int diff = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR) + 1;

                if (diff > 70) {
                    return Code.AGE_70.getCode();
                } else if (diff > 60) {
                    return Code.AGE_60.getCode();
                } else if (diff > 50) {
                    return Code.AGE_50.getCode();
                } else if (diff > 40) {
                    return Code.AGE_40.getCode();
                } else if (diff > 30) {
                    return Code.AGE_30.getCode();
                } else if (diff > 20) {
                    return Code.AGE_20.getCode();
                } else if (diff > 10) {
                    return Code.AGE_10.getCode();
                } else if (diff >= 0) {
                    return Code.AGE_00.getCode();
                }

            }
        }
        return null;
    }

    private String getGenderCode(String gender) {
        if ("MALE".equalsIgnoreCase(gender)) {
            return Code.GENDER_MALE.getCode();
        } else if ("FEMALE".equalsIgnoreCase(gender)) {
            return Code.GENDER_FEMALE.getCode();
        }
        return null;
    }

    // 카드뉴스 이미지 변환
    private void convertCardNewsImage(BoardPost post) {
        if (post.newsType != null && post.newsType.equals(Code.NEWS_TYPE_CARD.getCode())) {
            List<String> tmpCardImageList = HtmlUtil.getImgSrc(post.content);
            if (tmpCardImageList != null) {
                List<String> cardImageList = new ArrayList<>();
                String newImageUrl = "";
                for (String oldImageUrl : tmpCardImageList) {
                    // 마을뉴스 image url만 추출
                    if (oldImageUrl.startsWith("/api/board/post/image")) {
                        newImageUrl = oldImageUrl.replace("/api/board/post/image", "/api/public/board/post/image");
                        cardImageList.add(newImageUrl);
                    }
                }
                post.cardImageCount = cardImageList.size();
                post.cardImageList = cardImageList;
                post.content = "";
            }
        }
    }

    // 마을뉴스 포스트
    @Override
    public ScrollPage<BoardPost> getTodayPosts(Long userId, Long categoryId, String tag, String sido, String sigungu, Long lastPostId, int count, String newsType, String newsCategory, String keyword,
            String slideYn, String hashtag, TodaySort todaySort) {
        if (lastPostId == null || lastPostId == 0l) {
            lastPostId = Long.MAX_VALUE;
        }

        User user = null;
        String categoryType = null;

        if (userId == null && "Y".equalsIgnoreCase(slideYn)) {
            // 비로그인 상태, 잠금화면에서 들어왔을 경우 기본 categoryType 은 today 로 설정
            categoryType = "today";
        } else {
            user = userRepository.findOne(userId);
            categoryType = boardCategoryRepository.findOne(categoryId).type;
        }

        List<Object> paramList = new ArrayList<>();
        paramList.add(categoryId);
        paramList.add(sido);
        paramList.add(sigungu);
        paramList.add(categoryType);
        paramList.add(lastPostId);

        String sqlTagFrom = "";
        String sqlTagSelect = "";
        if (tag != null) {
            // logger.info("### tag parma ###");
            sqlTagFrom = ", board_post_has_tag l ";
            // sqlTagSelect = " AND b.id = l.post_id AND l.tag = '" + tag + "' ";
            sqlTagSelect = " AND b.id = l.post_id AND l.tag = ? ";
            paramList.add(tag);
        }

        if (!Strings.isNullOrEmpty(hashtag)) {
            // logger.info("### tag parma ###");
            sqlTagFrom = ", board_post_hashtag l ";
            // sqlTagSelect = " AND b.id = l.post_id AND l.tag = '" + tag + "' ";
            sqlTagSelect = " AND b.id = l.post_id AND l.name = ? ";
            paramList.add(hashtag);
        }

        // =====================================================================
        // 마을뉴스 [S]

        String sqlNews = "";

        if ("today".equals(categoryType)) {
            /*
             * [2016.05.17] 뉴스 카테고리 검색조건 추가 게시판 출력 여부 조건 추가
             */
            if (!StringUtils.isEmpty(newsType)) {
                sqlNews += " AND b.news_type = ? ";
                paramList.add(newsType);
            }
            if (StringUtils.isEmpty(newsCategory)) {
                sqlNews += " AND b.news_category in (select code from common_code where code_group = ? and use_yn = 'Y') ";
                paramList.add(Code.CODE_GROUP_NEWS_CATEGORY.getCode());
            } else {
                sqlNews += " AND b.news_category = getUsedCode(?,?) ";
                paramList.add(Code.CODE_GROUP_NEWS_CATEGORY.getCode());
                paramList.add(newsCategory);
            }
            // 검색어
            if (!StringUtils.isEmpty(keyword)) {
                sqlNews += " AND (b.title like '%' ? '%' OR b.content like '%' ? '%')";
                paramList.add(keyword);
                paramList.add(keyword);
            }

            // 잠금화면 최신뉴스
            if ("Y".equalsIgnoreCase(slideYn)) {
                sqlNews += " AND b.slide_yn = 'Y' ";

                // 성별,연령은 잠금화면에서만 조회
                if (user != null) {
                    String userGenderCode = getGenderCode(user.gender);
                    String userAgeCode = getYearToAgeCode(user.birthYear);

                    // 성별
                    if (!StringUtils.isEmpty(userGenderCode)) {
                        sqlNews += " AND (b.gender is null OR " + "      b.gender = 'ALL' OR " + "      b.gender = ?) ";
                        paramList.add(userGenderCode);
                    }

                    // 연령
                    if (!StringUtils.isEmpty(userAgeCode)) {
                        sqlNews += " AND (b.age is null OR " + "      b.age = 'ALL' OR " + "      INSTR(b.age, ?) > 0) ";
                        paramList.add(userAgeCode);
                    }
                }
            }

            // 현재 출력여부는 news 에서만 사용
            sqlNews += " AND b.display_yn = 'Y' ";

        }

        String orderBy = StringUtils.EMPTY;

        if (todaySort == TodaySort.RECENT)
            orderBy = " ORDER BY b.reg_date DESC ";
        else if (todaySort == TodaySort.POPULAR)
            orderBy = " ORDER BY b.view_count DESC ";

        // 마을뉴스 [E]
        // =====================================================================

        ScrollPage<BoardPost> ret = new ScrollPage<>();
        List<BoardPost> list = jdbcTemplate.query("SELECT b.*, u.id as 'u_user_id', u.nickname, u.has_profile_image, u.full_name, h.dong, h.ho, c.name AS 'category_name', c.content_mode "
                + ", (SELECT COUNT(*) FROM board_empathy e WHERE e.post_id = b.id AND e.user_id = " + userId + ") AS 'empathy_check_yn' FROM board_post b, user u, board_category c, house h "
                + sqlTagFrom + " WHERE b.display_yn = 'Y' AND (b.category_id=? OR \n"
                // [START] 단체관리자 기능 추가 : 마을 뉴스 리스트 by PNS 2016.09.26
                // 마을뉴스 타겟주소 구조가 '경기도'의 경우 '수원시 권선구' 시/구가 같이 있는 구조라서 '수원시' 전체에 권한을 주기가 힘듬 ==> 문자열에 들어있는지만 체크하는 것으로 수정하고 range_all은 제일 먼제 체크하게 수정함
                + "(b.range_all=1 OR (b.range_sido=IF(b.range_sigungu='', ?, null) OR (b.range_sigungu!='' AND (b.range_sigungu!='' AND LOCATE(b.range_sigungu, ?) > 0))) AND c.type = ?)) "
                // [END]
                + "AND b.id < ? AND b.user_id = u.id AND b.category_id=c.id AND u.house_id = h.id " + sqlTagSelect + sqlNews + orderBy + " LIMIT " + count, paramList.toArray(),
                new RowMapper<BoardPost>() {
                    // new Object[]{categoryId, sido, sigungu, categoryType, lastPostId}, new RowMapper<BoardPost>() {
                    @Override
                    public BoardPost mapRow(ResultSet rs, int rowNum) throws SQLException {
                        BoardPost post = new BoardPost();
                        post.id = rs.getLong("id");
                        post.content = rs.getString("content");
                        post.imageCount = rs.getInt("image_count");
                        post.rangeSido = rs.getString("range_sido");
                        post.rangeSigungu = rs.getString("range_sigungu");
                        post.rangeAll = rs.getBoolean("range_all");
                        post.regDate = new Date(rs.getTimestamp("reg_date").getTime());
                        // post.title = rs.getString("title");
                        post.file1 = rs.getString("file1");
                        post.file2 = rs.getString("file2");
                        post.viewCount = rs.getLong("view_count");
                        post.countEmpathy = rs.getLong("count_empathy");
                        post.blocked = rs.getBoolean("blocked");
                        post.commentCount = rs.getLong("comment_count");
                        // 20160801, 공감확인여부 추가
                        post.empathyCheckYn = rs.getInt("empathy_check_yn");

                        // [2016.05.17] 뉴스구분, 뉴스카테고리 항목 추가
                        post.newsType = rs.getString("news_type");
                        post.newsCategory = rs.getString("news_category");

                        User newUser = new User();
                        UserNickname nickname = new UserNickname();
                        nickname.name = rs.getString("nickname");

                        newUser.id = rs.getLong("u_user_id");
                        newUser.setNickname(nickname.name == null ? null : nickname);
                        newUser.setFullNameRawData(rs.getString("full_name"));
                        newUser.hasProfileImage = rs.getBoolean("has_profile_image");
                        post.user = newUser;

                        BoardCategory category = new BoardCategory();
                        category.name = rs.getString("category_name");
                        category.contentMode = rs.getString("content_mode");
                        post.category = category;

                        // 게시판 카테고리 모드가 html이고 게시판 title이 비어있는 경우에만 게시판 내용을 변환하여 타이틀로 수정
                        if ("html".equals(post.category.contentMode) && StringUtils.isBlank(rs.getString("title"))) {
                            String tempContent = post.content;
                            String tempTitle = HtmlUtil.removeTag(tempContent);

                            if (tempTitle.length() > 300) {
                                post.title = tempTitle.substring(0, 300);
                            } else {
                                post.title = tempTitle;
                            }
                        } else {
                            post.title = rs.getString("title");
                        }

                        House house = new House();
                        house.dong = rs.getString("dong");
                        post.user.house = house;

                        // [2016.05.17] 카드뉴스 이미지 추출 추가
                        convertCardNewsImage(post);

                        return post;
                    }
                });

        ret.setContent(list);

        if (StringUtils.isNotEmpty(keyword) || StringUtils.isNotEmpty(hashtag)) {
            int totalCount = this.getTodayPostsCount(sqlTagFrom, sqlTagSelect, sqlNews, paramList);
            ret.setTotalCount(totalCount);
        }

        final int size = list.size();
        if (size >= count) {
            ret.setNextPageToken(String.valueOf(list.get(size - 1).id));
        }
        if (user != null) {
            for (BoardPost post : list) {
                post.isDeletable = user.type.jaha || user.id.equals(post.user == null ? 0l : post.user.id);
            }
        }

        return ret;
    }

    @Override
    public List<Hashtag> fetchHashtags(Long postId) {
        return hashtagRepository.findByPostId(postId);
    }

    private int getTodayPostsCount(String sqlTagFrom, String sqlTagSelect, String sqlNews, List<Object> paramList) {
        String query = "SELECT COUNT(*) FROM board_post b, user u, board_category c, house h " + sqlTagFrom + " WHERE b.display_yn = 'Y' AND (b.category_id=? OR \n"
        // [START] 단체관리자 기능 추가 : 마을 뉴스 리스트 by PNS 2016.09.26
        // 마을뉴스 타겟주소 구조가 '경기도'의 경우 '수원시 권선구' 시/구가 같이 있는 구조라서 '수원시' 전체에 권한을 주기가 힘듬 ==> 문자열에 들어있는지만 체크하는 것으로 수정하고 range_all은 제일 먼제 체크하게 수정함
                + "(b.range_all=1 OR (b.range_sido=IF(b.range_sigungu='', ?, null) OR (b.range_sigungu!='' AND LOCATE(b.range_sigungu, ?) > 0)) AND c.type = ?)) "
                // [END]

                + "AND b.id < ? AND b.user_id = u.id AND b.category_id=c.id AND u.house_id = h.id " + sqlTagSelect + sqlNews;

        int totalCount = jdbcTemplate.queryForObject(query, paramList.toArray(), Integer.class);

        return totalCount;
    }



    @Override
    public List<Map<String, Object>> getAlarmPosts(List<BoardCategory> categoryList, String type, String topFix) {

        StringBuffer sqlTagWhere = new StringBuffer();
        StringBuffer sqlTagOrderBy = new StringBuffer();

        if (AppMainAlarm.TOP.getValue().equals(type)) {
            // COMMUNITY_TOPFIX / 커뮤니티 상단 고정
            if (StringUtils.isNotEmpty(topFix)) {
                sqlTagWhere.append(" AND b.top_fix = " + ("Y".equals(topFix) ? "1" : "0"));
            }
        }

        sqlTagOrderBy.append((sqlTagOrderBy.length() > 0 ? "" : "ORDER BY ") + " b.reg_date DESC ");

        // TODO :jdbcTemplate
        if (categoryList != null && !categoryList.isEmpty()) {
            sqlTagWhere.append(" AND c.id IN (");
            for (int i = 0; i < categoryList.size(); i++) {
                BoardCategory bc = categoryList.get(i);
                sqlTagWhere.append(bc.id + ((i != categoryList.size() - 1) ? "," : ""));
            }
            sqlTagWhere.append(") ");
        }

        List<Object> paramList = new ArrayList<>();

        List<Map<String, Object>> list = jdbcTemplate.query("SELECT b.*, c.id AS 'category_id', c.name AS 'category_name', c.content_mode "
                + "FROM board_post b, board_category c  WHERE b.category_id = c.id AND b.display_yn = 'Y'  " + sqlTagWhere.toString() + sqlTagOrderBy.toString(), paramList.toArray(),
                new RowMapper<Map<String, Object>>() {
                    @Override
                    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {

                        Map<String, Object> post = new HashMap<String, Object>();
                        post.put("categoryType", type.toLowerCase());
                        post.put("categoryId", rs.getLong("category_id"));
                        post.put("categoryName", rs.getString("category_name"));
                        post.put("postId", rs.getLong("id"));

                        String tempContent = rs.getString("content");
                        // 게시판 카테고리 모드가 html이고 게시판 title이 비어있는 경우에만 게시판 내용을 변환하여 타이틀로 수정
                        if ("html".equals(rs.getString("content_mode")) && StringUtils.isEmpty(rs.getString("title"))) {
                            String tempTitle = HtmlUtil.removeTag(tempContent);
                            if (tempTitle.length() > 300) {
                                post.put("title", tempTitle.substring(0, 300));
                            } else {
                                post.put("title", tempTitle);
                            }
                        } else {
                            post.put("title", rs.getString("title"));
                        }
                        return post;
                    }
                });

        return list;
    }

}
