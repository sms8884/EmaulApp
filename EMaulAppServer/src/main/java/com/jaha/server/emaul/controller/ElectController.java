package com.jaha.server.emaul.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jaha.server.emaul.model.JhElectLog;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.Vote;
import com.jaha.server.emaul.model.VoteGroup;
import com.jaha.server.emaul.model.Voter;
import com.jaha.server.emaul.model.VoterSecurity;
import com.jaha.server.emaul.repo.VoterSecurityRepository;
import com.jaha.server.emaul.service.HouseService;
import com.jaha.server.emaul.service.JhElectLogService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.service.VoteService;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.Util;

/**
 * Created by doring on 15. 2. 25..
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
public class ElectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElectController.class);

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserService userService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private JhElectLogService electService;


    /**
     * 선관의 로그인시 Sign 이미지 파일 이름 생성.
     *
     * @param userId
     * @param electTel
     * @return
     */
    private String makeElectSignFileName(String userId, String electTel) {
        String dt = new SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(new Date());
        String tmp = Util.getDateString();
        return String.format("ele_%s_%s_%s.jpg", dt, userId, electTel);
    }


    /**
     * 선관위 정보 저장(Sign 이미지 포함)
     *
     * @param req
     * @param name
     * @param tel
     * @param signImage
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/api/vote/elect/sign", method = RequestMethod.POST)
    public @ResponseBody String insertElectLog(HttpServletRequest req, @RequestParam(value = "name") String name, @RequestParam(value = "tel") String tel,
            @RequestParam(value = "signImage") MultipartFile signImage) throws JsonProcessingException {


        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        JhElectLog log = new JhElectLog();
        log.admId = user.getEmail();
        log.eleName = name;
        log.eleTel = tel;
        log.signImgFname = makeElectSignFileName(String.valueOf(user.id), tel);
        log.uptDt = new Date();

        try {
            File dir = new File(String.format("/nas/EMaul/vote/elect/sign_image")); // 운영에 사용.
            // File dir = new File(String.format("C:\\project\\Jaha\\test")); // 테스트에만 사용함.
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File dest = new File(dir, log.signImgFname);
            dest.createNewFile();
            signImage.transferTo(dest);

            electService.save(log);
            return "1";

        } catch (Exception e) {
            LOGGER.error("", e);
        }

        return "";
    }


    /**
     * 아파트 ID를 기준으로 동,호 정보을 가져온다.
     *
     * @param req
     * @param aptId
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/api/vote/elect/dongho", method = RequestMethod.POST)
    public @ResponseBody String getDongHo(HttpServletRequest req, @RequestParam(value = "apt_id") int aptId) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(electService.getDongHo(aptId));
    }

    /**
     * 투표 정보을 가져옴.
     *
     * @param req
     * @param main
     * @param ongoingOrDone
     * @param dong
     * @param ho
     * @param lastVoteId
     * @return
     */
    @RequestMapping(value = "/api/vote/elect/list/filter", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<Vote> listVoteByFilter(HttpServletRequest req, @RequestParam(value = "main") String main, @RequestParam(value = "ongoingOrDone") String ongoingOrDone,
            @RequestParam(value = "dong") String dong, @RequestParam(value = "ho") String ho, @RequestParam(value = "lastItemId", required = false, defaultValue = "0") Long lastVoteId) {

        LOGGER.debug("listVoteByFilter Start");
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        if ("vote".equalsIgnoreCase(main) || "poll".equalsIgnoreCase(main)) {
            // return electService.getVoteListInner(main,user,lastVoteId,ongoingOrDone,dong,ho);
            ScrollPage<Vote> scrollPage = electService.getVoteListInner(main, user, lastVoteId, ongoingOrDone, dong, ho);
            if ("vote".equalsIgnoreCase(main)) {
                List<Vote> voteList = scrollPage.getContent();
                for (Vote vote : voteList) {
                    vote.votedCnt = Long.valueOf(voteService.getVoters(vote.id).size() + voteService.getOfflineVoters(vote.id).size());
                }
                scrollPage.setContent(voteList);
            }
            return scrollPage;
        }
        return null;
    }


    /**
     * 투표 가능한지 체크.(동/호 기준)
     *
     * @param req
     * @param voteId
     * @param dong
     * @param ho
     * @return
     */
    @RequestMapping(value = "/api/vote/elect/available", method = RequestMethod.GET)
    public @ResponseBody String isVoteAvailable(HttpServletRequest req, @RequestParam(value = "voteId") Long voteId, @RequestParam(value = "dong") String dong, @RequestParam(value = "ho") String ho) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        Vote vote = voteService.getVote(voteId);

        // user.house.dong = dong;
        // user.house.ho= ho;

        String status = voteService.getVoteStatus(voteId);
        if (!"active".equalsIgnoreCase(status)) {
            return status.toUpperCase();
        }

        // if (electService.isAlreadyVotedHouse(user, voteId)) {
        if (electService.isAlreadyVotedHouse(user.house.apt.id, dong, ho, voteId)) {
            return "ALREADY_VOTED_HOUSE";
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
                targetHo = new Gson().fromJson(vote.jsonArrayTargetHo, new TypeToken<List<String>>() {}.getType());
            }

            /*
             * if (!vote.targetApt.equals(user.house.apt.id) || (vote.targetDong != null && !vote.targetDong.isEmpty() && !vote.targetDong.equals(user.house.dong)) || (targetHo != null &&
             * !targetHo.contains(user.house.ho))) { return "INVALID_AUTH_HOUSE"; }
             */
            if (!vote.targetApt.equals(user.house.apt.id) || (vote.targetDong != null && !vote.targetDong.isEmpty() && !vote.targetDong.equals(dong)) || (targetHo != null && !targetHo.contains(ho))) {
                return "INVALID_AUTH_HOUSE";
            }
        }

        if (!vote.rangeSido.isEmpty() || vote.targetApt != 1l) {
            if (!user.type.admin && !user.type.jaha && !user.type.user) {
                return "INVALID_AUTH";
            }
        }

        return "";
    }


    /**
     * 투표 저장.
     *
     * @param req
     * @param voteId
     * @param voteItemId
     * @param userContent
     * @param dong
     * @param ho
     * @param regType
     * @param voterName
     * @param mandateName
     * @param signImage
     * @return
     * @throws JsonProcessingException
     */
    @RequestMapping(value = "/api/vote/elect/item/mark", method = RequestMethod.POST)
    public @ResponseBody String doVote(HttpServletRequest req, @RequestParam(value = "voteId") Long voteId, @RequestParam(value = "voteItemId") Long voteItemId,
            @RequestParam(value = "userContent", required = false, defaultValue = "") String userContent,

            @RequestParam(value = "dong") String dong, @RequestParam(value = "ho") String ho, @RequestParam(value = "reg_type") String regType, @RequestParam(value = "voter_name") String voterName,
            @RequestParam(value = "mandate_name", required = false, defaultValue = "") String mandateName,

            @RequestParam(value = "signImage") MultipartFile signImage) throws JsonProcessingException {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        LOGGER.debug("elect mark step1 user.dong:" + user.house.dong + " user.ho:" + user.house.ho);
        String voteAvailable = isVoteAvailable(req, voteId, dong, ho);

        if (!"".equals(voteAvailable)) {
            return voteAvailable;
        }

        LOGGER.debug("elect mark step2 user.dong:" + user.house.dong + " user.ho:" + user.house.ho);
        Voter voter = new Voter();
        voter.user = user;
        voter.aptId = user.house.apt.id;
        voter.dong = dong;
        voter.ho = ho;
        voter.userContent = userContent;
        voter.voteDate = new Date();
        voter.vote = voteService.getVote(voteId);
        voter.voteItem = voteService.getVoteItem(voteItemId);

        voter.voterName = voterName;
        voter.registerType = regType;
        voter.mandateVoterName = mandateName;
        voter.mandated = !mandateName.isEmpty();

        LOGGER.debug("elect mark step3 user.dong:" + user.house.dong + " user.ho:" + user.house.ho);
        if (signImage == null) {
            return "ERROR-SIGN";
        }

        try {
            // File dir = new File(String.format("C:\\project\\Jaha\\test")); // 테스트에만 사용함.
            File dir = new File(String.format("/nas/EMaul/vote/sign-image/%s", voteId));
            // voter.signImageUri = String.format("/api/vote/sign-image/%s/%s", voteId, user.id);
            voter.signImageUri = String.format("/api/vote/app-sign-image/%s/%s_%s_%s", voteId, user.id, dong, ho);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, String.format("%s_%s_%s.jpg", user.id, dong, ho));
            dest.createNewFile();
            signImage.transferTo(dest);

            String json = new Gson().toJson(voter);
            LOGGER.debug("elect mark Exception:" + json);

            // if ( electService.save(user, voter) ){
            if (electService.save(user.house.apt.id, dong, ho, voter)) {
                LOGGER.debug("elect mark step4 user.dong:" + user.house.dong + " user.ho:" + user.house.ho);
                return "OK";
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return "ERROR-SAVE";
    }

    @Autowired
    PlatformTransactionManager mTransactionManager;

    @Autowired
    VoterSecurityRepository mVoterSecurityRepository;


    @RequestMapping(value = "/api/vote/elect/item/mark-sec", method = RequestMethod.POST)
    public @ResponseBody String doVoteSecurity(HttpServletRequest req, @RequestParam(value = "voteId") Long voteId, @RequestParam(value = "voteItemId") String voteItemId,
            @RequestParam(value = "userContent", required = false, defaultValue = "") String userContent,

            @RequestParam(value = "dong") String dong, @RequestParam(value = "ho") String ho, @RequestParam(value = "reg_type") String regType, @RequestParam(value = "voter_name") String voterName,
            @RequestParam(value = "mandate_name", required = false, defaultValue = "") String mandateName,

            @RequestParam(value = "signImage") MultipartFile signImage, @RequestParam(value = "chk_voter_item") MultipartFile chkVoterItem) throws JsonProcessingException {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        String voteAvailable = isVoteAvailable(req, voteId, dong, ho);

        if (!"".equals(voteAvailable)) {
            return voteAvailable;
        }

        Voter voter = new Voter();
        voter.user = user;
        voter.aptId = user.house.apt.id;
        voter.dong = dong;
        voter.ho = ho;
        voter.userContent = userContent;
        voter.voteDate = new Date();
        voter.vote = voteService.getVote(voteId);
        // voter.voteItem = voteService.getVoteItem(voteItemId);

        voter.voterName = voterName;
        voter.registerType = regType;
        voter.mandateVoterName = mandateName;
        voter.mandated = !mandateName.isEmpty();

        if (signImage == null) {
            return "ERROR-SIGN";
        }
        if (chkVoterItem == null) {
            return "ERROR-ITEM";
        }

        VoterSecurity voterSecurity = new VoterSecurity();
        voterSecurity.viId = UUID.randomUUID().toString();
        voterSecurity.itemIdChkFname = String.format("chk_voter_item_%d_%d_%s_%s_%s.emaul", voteId, user.id, dong, ho, Util.getRandomString(4));
        voterSecurity.itemIdEnc = voteItemId;
        voterSecurity.voteId = voteId;
        voterSecurity.itemId = 0L;

        TransactionStatus status = mTransactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            // File dir = new File(String.format("C:\\project\\Jaha\\test")); // 테스트에만 사용함.
            File dir = new File(String.format("/nas/EMaul/vote/sign-image/%s", voteId));
            // voter.signImageUri = String.format("/api/vote/sign-image/%s/%s", voteId, user.id);
            String randStr = Util.getRandomString(4);
            voter.signImageUri = String.format("/api/vote/app-sign-image/%s/%d_%d_%s_%s_%s", voteId, voteId, user.id, dong, ho, randStr);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            // File dest = new File(dir, String.format("%d_%d_%s_%s_%s.jpg", voteId,user.id,dong,ho, Util.getRandomString(4)));
            File dest = new File(dir, String.format("%d_%d_%s_%s_%s.jpg", voteId, user.id, dong, ho, randStr));
            dest.createNewFile();
            signImage.transferTo(dest);

            saveCheckVoteItem(chkVoterItem, voterSecurity.itemIdChkFname, voteId.intValue());

            // if (electService.save(user, voter) &&
            if (electService.save(user.house.apt.id, dong, ho, voter) && mVoterSecurityRepository.saveAndFlush(voterSecurity) != null) {
                mTransactionManager.commit(status);
                return "OK";
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        mTransactionManager.rollback(status);
        return "ERROR-SAVE";
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

    /**
     * 관리자 로그인
     *
     * @param req
     * @param email
     * @param password
     * @param uid
     * @param phoneNumber
     * @param gcmId
     * @return
     */
    @RequestMapping(value = "/api/public/user/elect/login", method = RequestMethod.POST)
    public @ResponseBody User login(HttpServletRequest req, @RequestParam(value = "email") String email, @RequestParam(value = "password") String password,
            @RequestParam(value = "uid", required = false) String uid, @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "gcmId", required = false) String gcmId, @RequestParam(value = "kind", required = false) String kind,
            @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam(value = "deviceId", required = false) String deviceId,
            @RequestParam(value = "osName", required = false) String osName, @RequestParam(value = "osVersion", required = false) String osVersion,
            @RequestParam(value = "maker", required = false) String maker, @RequestParam(value = "model", required = false) String model) {



        // req, email, password, kind, gcmId, appVersion
        User user = userService.login(req, email, password, "", gcmId, appVersion, deviceId, osName, osVersion, maker, model);
        if (user != null) {
            if (user.type.admin != true) {
                req.getSession().invalidate();
                user = null;
            }
        }

        return user;
    }



    /**
     * 세대주 이름을 가져옴.
     *
     * @param req
     * @param dong
     * @param ho
     * @return
     */
    @RequestMapping(value = "/api/vote/elect/househost", method = RequestMethod.GET)
    public @ResponseBody String getHouseHostFromDongHo(HttpServletRequest req, @RequestParam(value = "dong") String dong, @RequestParam(value = "ho") String ho) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        return electService.getHouseHostFromDongHo(user.house.apt.id, dong, ho);


    }

}
