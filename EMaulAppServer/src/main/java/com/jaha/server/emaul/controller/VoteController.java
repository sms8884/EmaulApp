package com.jaha.server.emaul.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jaha.server.emaul.model.Apt;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.Vote;
import com.jaha.server.emaul.model.VoteGroup;
import com.jaha.server.emaul.model.VoteItem;
import com.jaha.server.emaul.model.Voter;
import com.jaha.server.emaul.model.VoterSecurity;
import com.jaha.server.emaul.model.json.VoteResult;
import com.jaha.server.emaul.repo.VoteRepository;
import com.jaha.server.emaul.repo.VoterSecurityRepository;
import com.jaha.server.emaul.service.HouseService;
import com.jaha.server.emaul.service.PhoneAuthService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.service.VoteService;
import com.jaha.server.emaul.util.Responses;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.Util;

/**
 * Created by doring on 15. 2. 25..
 */
@Controller
public class VoteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoteController.class);

    @Autowired
    private VoteService voteService;
    @Autowired
    private UserService userService;
    @Autowired
    private HouseService houseService;
    @Autowired
    private PhoneAuthService phoneAuthService;
    @Autowired
    VoterSecurityRepository mVoterSecurityRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @RequestMapping(value = "/api/{type}/list", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<Vote> listVote(HttpServletRequest req, @PathVariable(value = "type") String type,
            @RequestParam(value = "lastItemId", required = false, defaultValue = "0") Long lastVoteId) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        user.kind = SessionAttrs.getKind(req.getSession());

        if ("vote".equals(type)) {
            return voteService.getVotes(user, lastVoteId);
        } else if ("poll".equals(type)) {
            return voteService.getPolls(user, lastVoteId);
        }
        return null;
    }

    @RequestMapping(value = "/api/vote/{voteId}", method = RequestMethod.GET)
    public @ResponseBody Vote getVote(HttpServletRequest req, @PathVariable(value = "voteId") Long voteId) {
        return voteService.getVote(voteId);
    }

    @RequestMapping(value = "/api/vote/list/filter", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<Vote> listVoteByFilter(HttpServletRequest req, @RequestParam(value = "main") String main, @RequestParam(value = "ongoingOrDone") String ongoingOrDone,
            @RequestParam(value = "lastItemId", required = false, defaultValue = "0") Long lastVoteId) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        user.kind = "old version";

        if ("vote".equalsIgnoreCase(main)) {
            sendSecuritySms();
            return voteService.getVotes(user, lastVoteId, ongoingOrDone);
        } else if ("poll".equalsIgnoreCase(main)) {
            return voteService.getPolls(user, lastVoteId, ongoingOrDone);
        }
        return null;
    }

    @RequestMapping(value = "/api2/vote/list/filter", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<Vote> listVoteByFilterOld(HttpServletRequest req, @RequestParam(value = "main") String main, @RequestParam(value = "ongoingOrDone") String ongoingOrDone,
            @RequestParam(value = "lastItemId", required = false, defaultValue = "0") Long lastVoteId) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        user.kind = "old version";

        if ("vote".equalsIgnoreCase(main)) {
            sendSecuritySms();
            return voteService.getVotes(user, lastVoteId, ongoingOrDone);
        } else if ("poll".equalsIgnoreCase(main)) {
            return voteService.getPolls(user, lastVoteId, ongoingOrDone);
        }
        return null;
    }

    @RequestMapping(value = "/api/vote/list/filter-new", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<Vote> listVoteByFilterNew(HttpServletRequest req, @RequestParam(value = "main") String main, @RequestParam(value = "ongoingOrDone") String ongoingOrDone,
            @RequestParam(value = "lastItemId", required = false, defaultValue = "0") Long lastVoteId) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        user.kind = SessionAttrs.getKind(req.getSession());

        if ("vote".equalsIgnoreCase(main)) {
            sendSecuritySms();
            return voteService.getVotes(user, lastVoteId, ongoingOrDone);
        } else if ("poll".equalsIgnoreCase(main)) {
            return voteService.getPolls(user, lastVoteId, ongoingOrDone);
        }
        return null;
    }

    /**
     * 서명 팝업이 뜨기전 투표가능여부 체크
     *
     * @param req
     * @param voteId
     * @return
     */
    @SuppressWarnings("serial")
    @RequestMapping(value = "/api/vote/available", method = RequestMethod.GET)
    public @ResponseBody String isVoteAvailable(HttpServletRequest req, @RequestParam(value = "voteId") Long voteId) {
        try {
            User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
            Vote vote = voteService.getVote(voteId);

            String status = voteService.getVoteStatus(voteId);
            if (!"active".equalsIgnoreCase(status)) {
                return status.toUpperCase();
            }

            Long jahaAptId = 1L;

            if (!user.house.apt.id.equals(jahaAptId)) {
                if (voteService.isAlreadyVoted(user, voteId)) {
                    return "ALREADY_VOTED";
                }

                if (voteService.isAlreadyVotedHouse(user, voteId)) {
                    return "ALREADY_VOTED_HOUSE";
                }
            }

            List<String> userTypeList = new Gson().fromJson(vote.jsonArrayTargetUserTypes, new TypeToken<List<String>>() {}.getType());
            List<String> userTrueTypes = user.type.getTrueTypes();

            boolean userTypeMatched = false;
            for (String userTrueType : userTrueTypes) {
                if (userTypeList.contains(userTrueType)) {
                    userTypeMatched = true;
                    break;
                }
            }

            if (!userTypeMatched) {
                return "INVALID_AUTH";
            }

            if (!vote.rangeAll && vote.rangeSigungu.isEmpty() && vote.rangeSido.isEmpty()) {

                List<String> targetHo = null;

                // 선거구 체크 추가 : 2016.11.16 cyt
                if (vote.jsonArrayTargetGroup != null && !vote.jsonArrayTargetGroup.isEmpty()) {
                    // 선거구 start
                    List<Long> targetGroupList = new Gson().fromJson(vote.jsonArrayTargetGroup, new TypeToken<List<Long>>() {}.getType());

                    boolean isGroup = false; // 선거구 정보내 일치 여부
                    List<VoteGroup> groupList = voteService.getVoteGroupList(vote.targetApt, targetGroupList);
                    for (VoteGroup a : groupList) {

                        LOGGER.debug(">>> group type [" + a.groupType + "] json [" + a.jsonArrayTarget + "]");

                        if ("all".equals(a.groupType)) {
                            // 전체
                            isGroup = true;
                            break;
                        } else if ("dong".equals(a.groupType)) {
                            // 동
                            List<String> dongList = new Gson().fromJson(a.jsonArrayTarget, new TypeToken<List<String>>() {}.getType());
                            for (String d : dongList) {
                                LOGGER.debug(">>> dong : " + d + " :: " + user.house.dong);
                                if (d.equals(user.house.dong)) {
                                    LOGGER.debug(">>> 같은동");
                                    isGroup = true;
                                    break;
                                }
                            }

                        } else if ("ho".equals(a.groupType)) {
                            // 동/호 별
                            List<JsonObject> dongList = new Gson().fromJson(a.jsonArrayTarget, new TypeToken<List<JsonObject>>() {}.getType());
                            for (JsonObject d : dongList) {

                                if (d.get("dong").getAsString().equals(user.house.dong)) {
                                    LOGGER.debug(">>> 같은동");
                                    List<String> hoList = new Gson().fromJson(d.get("ho"), new TypeToken<List<String>>() {}.getType());
                                    for (String h : hoList) {
                                        LOGGER.debug(">>> ho : " + h + " :: " + user.house.dong + "동 " + user.house.ho + "호");
                                        if (h.equals(user.house.ho)) {
                                            LOGGER.debug(">>> 같은호");
                                            isGroup = true;
                                            break;
                                        }
                                    }
                                }

                            }
                        }
                    }

                    LOGGER.debug(">>> user dong : " + user.house.dong + " / ho : " + user.house.ho);
                    LOGGER.debug(">>> isGroup : " + isGroup);
                    if (!isGroup) {
                        return "INVALID_AUTH_HOUSE";
                    }
                    // 선거구 end
                } else if (vote.jsonArrayTargetHo != null && !vote.jsonArrayTargetHo.isEmpty()) {
                    // 동
                    targetHo = new Gson().fromJson(vote.jsonArrayTargetHo, new TypeToken<List<String>>() {}.getType());
                }

                if (!vote.targetApt.equals(user.house.apt.id) || (vote.targetDong != null && !vote.targetDong.isEmpty() && !vote.targetDong.equals(user.house.dong))
                        || (targetHo != null && !targetHo.contains(user.house.ho))) {
                    return "INVALID_AUTH_HOUSE";
                }
            }

            if (!vote.rangeSido.isEmpty() || vote.targetApt != 1l) {
                if (!user.type.admin && !user.type.jaha && !user.type.user) {
                    return "INVALID_AUTH";
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        return "";
    }

    @RequestMapping(value = "/api/vote/result/{voteId}", method = RequestMethod.GET)
    public @ResponseBody String getVoteResult(HttpServletRequest req, @PathVariable(value = "voteId") Long voteId) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        Vote vote = voteService.getVote(voteId);

        VoteResult ret = new VoteResult();
        ret.vote = vote;

        if (vote.targetApt != 1l && (!user.type.admin && !user.type.jaha && !user.type.user)) {
            return null;
        }

        if (!vote.voteResultAvailable) {
            return mapper.writeValueAsString(ret);
        }

        ret.onlineResultMap = Maps.newHashMap();
        ret.offlineResultMap = Maps.newHashMap();
        for (VoteItem item : vote.items) {
            int onlineVoterCount = 0;
            if (vote.enableSecurity.equals("Y")) {
                onlineVoterCount = voteService.getVoterSecurityByItem(vote.id, item.id).size();
            } else {
                onlineVoterCount = voteService.getVotersByItem(vote.id, item.id).size();
            }
            Long offlineVoterCount = voteService.getOfflineVoteResultCount(vote.id, item.id);
            ret.onlineResultMap.put(item.id, onlineVoterCount);
            ret.offlineResultMap.put(item.id, offlineVoterCount == null ? 0 : offlineVoterCount.intValue());
        }
        Long abstention = voteService.getOfflineVoteResultCount(vote.id, 0l);
        if (abstention != null) {
            ret.offlineResultMap.put(0l, abstention.intValue());
        }

        ret.resultText = voteService.getOfflineVoteResult(vote.id).resultText;

        return mapper.writeValueAsString(ret);
    }

    /**
     * 투표완료(일반투표)
     *
     * @param req
     * @param voteId
     * @param voteItemId
     * @param userContent
     * @param signImage
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/api/vote/item/mark", method = RequestMethod.POST)
    public @ResponseBody String doVote(HttpServletRequest req, @RequestParam(value = "voteId") Long voteId, @RequestParam(value = "voteItemId") Long voteItemId,
            @RequestParam(value = "userContent", required = false, defaultValue = "") String userContent, @RequestParam(value = "signImage", required = false) MultipartFile signImage)
            throws JsonProcessingException {

        LOGGER.info(">>> api/vote/item/mark : voteId : [" + voteId + "] voteItemId : [" + voteItemId + "]");

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String voteAvailable = isVoteAvailable(req, voteId);
        if (!"".equals(voteAvailable)) {
            return voteAvailable;
        }

        Voter voter = new Voter();
        voter.user = user;
        voter.aptId = user.house.apt.id;
        voter.dong = user.house.dong;
        voter.ho = user.house.ho;
        voter.userContent = userContent;
        voter.voteDate = new Date();
        voter.vote = voteService.getVote(voteId);
        voter.voteItem = voteService.getVoteItem(voteItemId);
        if (signImage != null) {
            voter.signImageUri = String.format("/api/vote/sign-image/%s/%s", voteId, user.id);
            try {
                File dir = new File(String.format("/nas/EMaul/vote/sign-image/%s", voteId));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File dest = new File(dir, String.format("%s.jpg", user.id));
                dest.createNewFile();
                signImage.transferTo(dest);
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        Voter voteResult = voteService.save(user, voter);

        return voteResult == null ? "" : mapper.writeValueAsString(voteResult);
    }

    /**
     * 투표완료(보안투표)
     *
     * @param req
     * @param voteId
     * @param voteItemId
     * @param userContent
     * @param signImage
     * @param signVideo
     * @param chkVoterItem
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/api/vote/item/mark-sec", method = RequestMethod.POST)
    public @ResponseBody String doVoteSecurity(HttpServletRequest req, @RequestParam(value = "voteId") Long voteId, @RequestParam(value = "voteItemId") String voteItemId,
            @RequestParam(value = "userContent", required = false, defaultValue = "") String userContent, @RequestParam(value = "signImage", required = false) MultipartFile signImage,
            @RequestParam(value = "signVideo", required = false) MultipartFile signVideo, @RequestParam(value = "chk_voter_item") MultipartFile chkVoterItem) throws JsonProcessingException {


        LOGGER.info(">>> 보안투표 마킹 : voteId [" + voteId + "] voteItemId [" + voteItemId + "]");

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String voteAvailable = isVoteAvailable(req, voteId);
        if (!"".equals(voteAvailable)) {
            return voteAvailable;
        }

        Voter voter = new Voter();
        voter.user = user;
        voter.aptId = user.house.apt.id;
        voter.dong = user.house.dong;
        voter.ho = user.house.ho;
        voter.userContent = userContent;
        voter.voteDate = new Date();
        voter.vote = voteService.getVote(voteId);
        // voter.voteItem = voteService.getVoteItem(voteItemId);

        VoterSecurity voterSecurity = new VoterSecurity();
        voterSecurity.viId = UUID.randomUUID().toString();
        voterSecurity.itemIdChkFname = String.format("chk_voter_item_%d_%d_%s_%s_%s.emaul", voteId, user.id, user.house.dong, user.house.ho, Util.getRandomString(4));
        voterSecurity.itemIdEnc = voteItemId;
        voterSecurity.voteId = voteId;
        voterSecurity.itemId = 0L;

        if (signImage != null) {
            voter.signImageUri = String.format("/api/vote/sign-image/%s/%s", voteId, user.id);
            try {
                File dir = new File(String.format("/nas/EMaul/vote/sign-image/%s", voteId));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File dest = new File(dir, String.format("%s.jpg", user.id));
                dest.createNewFile();
                signImage.transferTo(dest);

                saveCheckVoteItem(chkVoterItem, voterSecurity.itemIdChkFname, voteId.intValue());

            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }

        if (signVideo != null) {
            try {
                File dir = new File(String.format("/nas/EMaul/vote/sign-video/%s", voteId));
                // File dir = new File(String.format("C:\\nas\\EMaul\\vote\\sign-video\\%s", voteId));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File dest = new File(dir, String.format("%s.mp4", user.id));
                dest.createNewFile();
                signVideo.transferTo(dest);

            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        Voter voteResult = voteService.save(user, voter);
        mVoterSecurityRepository.saveAndFlush(voterSecurity);

        return voteResult == null ? "" : mapper.writeValueAsString(voteResult);
    }

    void saveCheckVoteItem(MultipartFile chkVoterItem, String savefileName, int voteId) throws IOException {
        // File dir = new File(String.format("C:\\project\\Jaha\\test")); // 테스트에만 사용함.
        File dir = new File(String.format("/nas/EMaul/vote/item_security/%d", voteId));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, savefileName);
        dest.createNewFile();
        chkVoterItem.transferTo(dest);
    }

    @RequestMapping(value = "/api/vote/sign-image/{voteId}/{userId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleSignImageRequest(@PathVariable("voteId") Long voteId, @PathVariable("userId") Long userId) {

        File toServeUp = new File("/nas/EMaul/vote/sign-image", String.format("%s/%s.jpg", voteId, userId));

        return Responses.getFileEntity(toServeUp, String.valueOf(userId) + ".jpg");
    }

    /**
     * @author shavrani 2016-06-28
     */
    @RequestMapping(value = "/api/vote/sign-video/{voteId}/{userId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleSignVideoRequest(@PathVariable("voteId") Long voteId, @PathVariable("userId") Long userId) {

        File toServeUp = new File("/nas/EMaul/vote/sign-video", String.format("%s/%s.mp4", voteId, userId));
        // File toServeUp = new File("C:\\nas\\Emaul", String.format("%s\\%s.mp4", voteId, userId));

        return Responses.getFileEntity(toServeUp, String.valueOf(userId) + ".mp4");
    }

    @RequestMapping(value = "/api/vote/image/{voteId}/{fileBaseName}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleVoteImageRequest(@PathVariable("voteId") Long voteId, @PathVariable("fileBaseName") String fileBaseName) {

        File toServeUp = new File("/nas/EMaul/vote/vote-image", String.format("%s/%s.jpg", voteId, fileBaseName));

        return Responses.getFileEntity(toServeUp, fileBaseName + ".jpg");
    }

    @RequestMapping(value = "/api/vote/item-image/{voteId}/{fileBaseName}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleVoteItemImageRequest(@PathVariable("voteId") Long voteId, @PathVariable("fileBaseName") Long fileBaseName) {

        File toServeUp = new File("/nas/EMaul/vote/vote-item-image", String.format("%s/%s.jpg", voteId, fileBaseName));

        return Responses.getFileEntity(toServeUp, fileBaseName + ".jpg");
    }

    @RequestMapping(value = "/api/vote/file/{voteId}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleFileRequest(@PathVariable("voteId") String voteId, @PathVariable("fileName") String fileName) {

        try {
            String decFilename = URLDecoder.decode(fileName, "utf-8");
            File toServeUp = new File("/nas/EMaul/vote/file", String.format("/%s/%s/%s", Long.valueOf(voteId) / 1000l, voteId, decFilename));

            return Responses.getFileEntity(toServeUp, voteId + "-" + fileName);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("", e);
        }
        return null;
    }

    // 비정상 보안 투표 문자 발송
    public void sendSecuritySms() {
        List<Vote> voteList = voteRepository.findBySecurityCheckStateGreaterThanAndSecurityNoticeState(0, false);
        LOGGER.debug("sms vote cnt : " + voteList.size());

        if (voteList.size() > 0) {
            String sql = "SELECT a.* FROM user a, user_type b WHERE a.id = b.user_id and b.jaha = b'1'";
            List<User> jahaUser = jdbcTemplate.query(sql, new Object[] {}, (rs, rowNum) -> {
                User user = new User();
                user.id = rs.getLong("id");
                user.setPhone(rs.getString("phone"));
                return user;
            });

            for (Vote vote : voteList) {
                String errorStr = "";
                if (vote.securityCheckState == 1) {
                    errorStr = "[Error: 투표자와 기표건수 오류]\n";
                } else if (vote.securityCheckState == 2) {
                    errorStr = "[Error: 기표정보 길이 불일치]\n";
                }
                Apt apt = houseService.getApt(vote.targetApt);

                for (User user : jahaUser) {
                    phoneAuthService.sendMsgNow(user.getPhone(), "028670816", errorStr + String.format("%s(%s), 투표ID:%s", apt.name, apt.id, vote.id), "", "");
                }
                vote.securityNoticeState = true;
                voteRepository.saveAndFlush(vote);
            }
        }
    }



    /**
     * 투표완료(일반투표) Ver.2
     *
     * @TODO : voteItemIds 만 복수로 처리
     * @param req
     * @param voteId
     * @param voteItemId
     * @param userContent
     * @param signImage
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/v2/api/vote/item/mark", method = RequestMethod.POST)
    public @ResponseBody String doVote(HttpServletRequest req, @RequestParam(value = "voteId") Long voteId, @RequestParam(value = "voteItemIds") Long[] voteItemIds,
            @RequestParam(value = "userContent", required = false, defaultValue = "") String userContent, @RequestParam(value = "signImage", required = false) MultipartFile signImage)
            throws JsonProcessingException {

        LOGGER.info(">>> api/vote/item/mark : voteId : [" + voteId + "] voteItemIds : [" + voteItemIds + "]");

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String voteAvailable = isVoteAvailable(req, voteId);
        if (!"".equals(voteAvailable)) {
            return voteAvailable;
        }

        if (voteItemIds == null || voteItemIds.length == 0) {
            return StringUtils.EMPTY;
        }

        List<Voter> voterResultList = Lists.newArrayList();

        for (Long voteItemId : voteItemIds) {
            Voter voter = new Voter();
            voter.user = user;
            voter.aptId = user.house.apt.id;
            voter.dong = user.house.dong;
            voter.ho = user.house.ho;
            voter.userContent = userContent;
            voter.voteDate = new Date();
            voter.vote = voteService.getVote(voteId);
            voter.voteItem = voteService.getVoteItem(voteItemId);

            if (signImage != null) {
                voter.signImageUri = String.format("/api/vote/sign-image/%s/%s", voteId, user.id);
                try {
                    File dir = new File(String.format("/nas/EMaul/vote/sign-image/%s", voteId));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File dest = new File(dir, String.format("%s.jpg", user.id));
                    dest.createNewFile();
                    signImage.transferTo(dest);
                } catch (IOException e) {
                    LOGGER.error("<<투표완료(일반투표) Ver.2 사인 이미지 저장 중 오류>>", e.getMessage());
                }
            }

            voterResultList.add(voteService.save(user, voter));
        }

        ObjectMapper mapper = new ObjectMapper();

        return (voterResultList == null || voterResultList.isEmpty()) ? StringUtils.EMPTY : mapper.writeValueAsString(voterResultList);
    }

    /**
     * 투표완료(보안투표) Ver.2
     *
     * @TODO : voteItemIds 만 복수로 처리
     * @param req
     * @param voteId
     * @param voteItemId
     * @param userContent
     * @param signImage
     * @param signVideo
     * @param chkVoterItem
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/v2/api/vote/item/mark-sec", method = RequestMethod.POST)
    public @ResponseBody String doVoteSecurity(HttpServletRequest req, @RequestParam(value = "voteId") Long voteId, @RequestParam(value = "voteItemIds") String[] voteItemIds,
            @RequestParam(value = "userContent", required = false, defaultValue = "") String userContent, @RequestParam(value = "signImage", required = false) MultipartFile signImage,
            @RequestParam(value = "signVideo", required = false) MultipartFile signVideo, @RequestParam(value = "chk_voter_item") MultipartFile chkVoterItem) throws JsonProcessingException {


        LOGGER.info(">>> 보안투표 마킹 : voteId [" + voteId + "] voteItemId [" + voteItemIds + "]");

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        String voteAvailable = isVoteAvailable(req, voteId);
        if (!"".equals(voteAvailable)) {
            return voteAvailable;
        }

        if (voteItemIds == null || voteItemIds.length == 0) {
            return StringUtils.EMPTY;
        }

        List<Voter> voterResultList = Lists.newArrayList();

        for (String voteItemId : voteItemIds) {
            Voter voter = new Voter();
            voter.user = user;
            voter.aptId = user.house.apt.id;
            voter.dong = user.house.dong;
            voter.ho = user.house.ho;
            voter.userContent = userContent;
            voter.voteDate = new Date();
            voter.vote = voteService.getVote(voteId);
            // voter.voteItem = voteService.getVoteItem(voteItemId);

            VoterSecurity voterSecurity = new VoterSecurity();
            voterSecurity.viId = UUID.randomUUID().toString();
            voterSecurity.itemIdChkFname = String.format("chk_voter_item_%d_%d_%s_%s_%s.emaul", voteId, user.id, user.house.dong, user.house.ho, Util.getRandomString(4));
            voterSecurity.itemIdEnc = voteItemId;
            voterSecurity.voteId = voteId;
            voterSecurity.itemId = 0L;

            if (signImage != null) {
                voter.signImageUri = String.format("/api/vote/sign-image/%s/%s", voteId, user.id);
                try {
                    File dir = new File(String.format("/nas/EMaul/vote/sign-image/%s", voteId));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File dest = new File(dir, String.format("%s.jpg", user.id));
                    dest.createNewFile();
                    signImage.transferTo(dest);

                    saveCheckVoteItem(chkVoterItem, voterSecurity.itemIdChkFname, voteId.intValue());

                } catch (IOException e) {
                    LOGGER.error("<<투표완료(보안투표) Ver.2 사인 이미지 저장 중 오류>>", e.getMessage());
                }
            }

            if (signVideo != null) {
                try {
                    File dir = new File(String.format("/nas/EMaul/vote/sign-video/%s", voteId));
                    // File dir = new File(String.format("C:\\nas\\EMaul\\vote\\sign-video\\%s", voteId));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File dest = new File(dir, String.format("%s.mp4", user.id));
                    dest.createNewFile();
                    signVideo.transferTo(dest);

                } catch (IOException e) {
                    LOGGER.error("<<투표완료(보안투표) Ver.2 사인 동영상 저장 중 오류>>", e.getMessage());
                }
            }


            voterResultList.add(voteService.save(user, voter));
            mVoterSecurityRepository.saveAndFlush(voterSecurity);
        }

        ObjectMapper mapper = new ObjectMapper();

        return (voterResultList == null || voterResultList.isEmpty()) ? StringUtils.EMPTY : mapper.writeValueAsString(voterResultList);
    }

}
