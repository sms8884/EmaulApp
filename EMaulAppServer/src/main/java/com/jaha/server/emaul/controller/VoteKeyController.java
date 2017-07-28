package com.jaha.server.emaul.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.model.Vote;
import com.jaha.server.emaul.model.VoteKey;
import com.jaha.server.emaul.model.VoterSecurity;
import com.jaha.server.emaul.repo.VoteKeyRepository;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.service.VoteKeyService;
import com.jaha.server.emaul.service.VoteService;
import com.jaha.server.emaul.util.Responses;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.Util;

/**
 * Created by doring on 15. 2. 25..
 */
@Controller
public class VoteKeyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoteKeyController.class);

    @Autowired
    private VoteService voteService;
    @Autowired
    private UserService userService;

    @Autowired
    private VoteKeyRepository voteKeyRepository;

    @Autowired
    private VoteKeyService voteKeyService;



    @RequestMapping(value = "/api/vote/key/list", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<VoteKey> getVoteKeyList(HttpServletRequest req, Pageable pageable) {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        ScrollPage<VoteKey> sp = new ScrollPage<>();

        LOGGER.debug("getVoteKeyList Aptid:" + user.house.apt.id);

        List<VoteKey> voteKey = voteKeyService.getVoteKeyList(user.house.apt.id, pageable);

        sp.setContent(voteKey);
        sp.setPageNumber(pageable.getPageNumber());
        return sp;
    }

    @RequestMapping(value = "/api/vote/key/listByAdmin", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<VoteKey> getVoteKeyListByAdmin(HttpServletRequest req, @RequestParam(value = "adminName") String adminName, @RequestParam(value = "adminEmail") String adminEmail,
            Pageable pageable) throws JsonProcessingException {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        ScrollPage<VoteKey> sp = new ScrollPage<>();

        List<VoteKey> voteKey = voteKeyService.getVoteKeyList(user.house.apt.id, adminName, adminEmail, pageable);

        sp.setContent(voteKey);
        sp.setPageNumber(pageable.getPageNumber());
        return sp;
    }

    @RequestMapping(value = "/api/vote/key/voteList", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<Vote> getVoteList(HttpServletRequest req, @RequestParam(value = "vk_id") Long vkId, Pageable pageable) throws JsonProcessingException {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        ScrollPage<Vote> sp = new ScrollPage<>();

        List<Vote> vote = voteService.getVoteList(user.house.apt.id, vkId, pageable);

        sp.setContent(vote);
        sp.setPageNumber(pageable.getPageNumber());
        return sp;
    }

    @RequestMapping(value = "/api/vote/key/voteSecurityList", method = RequestMethod.GET)
    public @ResponseBody ScrollPage<VoterSecurity> getVoteSecuriList(HttpServletRequest req, @RequestParam(value = "vote_id") Long voteId, Pageable pageable) throws JsonProcessingException {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        ScrollPage<VoterSecurity> sp = new ScrollPage<>();

        List<VoterSecurity> voterSecurities = voteKeyService.getVoterSecurityList(voteId, pageable);

        sp.setContent(voterSecurities);
        sp.setPageNumber(pageable.getPageNumber());
        return sp;
    }

    @RequestMapping(value = "/api/vote/key/del", method = RequestMethod.GET)
    public @ResponseBody String deleteVoteKey(HttpServletRequest req, @RequestParam(value = "vkId") int vkId) {
        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        int key = voteKeyRepository.deleteVkId(vkId, user.house.apt.id);// , id);
        LOGGER.debug("deleteVoteKey :" + key);
        return "OK";
    }



    private String makeKeyCreateSignFileName(String userId) {
        String dt = new SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(new Date());
        String tmp = Util.getDateString();
        return String.format("key_%s_%s.jpg", dt, userId);
    }


    @Autowired
    VoteKeyRepository mVoteKeyRepository;

    @RequestMapping(value = "/api/vote/key/reg", method = RequestMethod.POST)
    public @ResponseBody String insertVoteKey(HttpServletRequest req, @RequestParam(value = "msg") String msg, @RequestParam(value = "signImage") MultipartFile signImage)
            throws JsonProcessingException {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        VoteKey voteKey = new Gson().fromJson(msg, VoteKey.class);

        voteKey.createSignFname = makeKeyCreateSignFileName(String.valueOf(user.id));
        voteKey.aptId = user.house.apt.id;
        voteKey.useYn = "Y";
        voteKey.uptDt = new Date();
        voteKey.checkSignFname = "";
        voteKey.grantSignFname = "";
        voteKey.keyGrantDec = "";
        voteKey.keyGrantYn = "N";
        voteKey.regDt = Util.getDateString();
        voteKey.regTm = Util.getTimeString();

        // LOGGER.debug("KeyBaseName"+voteKey.keyBase1Uname);


        try {
            File dir = new File(String.format("/nas/EMaul/vote/key/sign_image")); // 운영에 사용.
            // File dir = new File(String.format("C:\\project\\Jaha\\test")); // 테스트에만 사용함.
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, voteKey.createSignFname);
            dest.createNewFile();
            signImage.transferTo(dest);

            mVoteKeyRepository.saveAndFlush(voteKey);
            return "OK";
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        return "ERROR";
    }

    private String makeKeyGrantSignFileName(String userId) {
        String dt = new SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(new Date());
        String tmp = Util.getDateString();
        return String.format("key_grant_%s_%s.jpg", dt, userId);
    }

    private String makeKeyCheckSignFileName(String userId) {
        String dt = new SimpleDateFormat("yyMMddHHmmss", Locale.KOREA).format(new Date());
        String tmp = Util.getDateString();
        return String.format("key_check_%s_%s.jpg", dt, userId);
    }

    @RequestMapping(value = "/api/vote/key/grant", method = RequestMethod.POST)
    public @ResponseBody String grantVoteKey(HttpServletRequest req, @RequestParam(value = "keyGrantDec") String keyGrantDec, @RequestParam(value = "vkId") int vkId,
            @RequestParam(value = "signImage") MultipartFile signImage) throws JsonProcessingException {

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));

        LOGGER.debug("keyGrantDec : " + keyGrantDec);

        try {
            File dir = new File(String.format("/nas/EMaul/vote/key/sign_image")); // 운영에 사용.
            // File dir = new File(String.format("C:\\project\\Jaha\\test")); // 테스트에만 사용함.

            String grantSignFileName = makeKeyGrantSignFileName(String.valueOf(user.id));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dest = new File(dir, grantSignFileName);
            dest.createNewFile();
            signImage.transferTo(dest);

            mVoteKeyRepository.updateGrantKey(vkId, user.house.apt.id, keyGrantDec, grantSignFileName);
            return "OK";
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        return "ERROR";
    }

    @RequestMapping(value = "/api/vote/key/sign-image/{fileBaseName}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleSignImageRequest(@PathVariable("fileBaseName") String fileBaseName) {
        String filePath = String.format("/nas/EMaul/vote/key/sign_image/%s.jpg", fileBaseName);
        LOGGER.debug("filePath : " + filePath);
        File toServeUp = new File(filePath);
        return Responses.getFileEntity(toServeUp, fileBaseName + ".jpg");
    }

    @RequestMapping(value = "/api/vote/key/check-file/{voteId}/{fileBaseName}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleCheckFileRequest(@PathVariable("voteId") Long voteId, @PathVariable("fileBaseName") String fileBaseName) {

        String filePath = String.format("/nas/EMaul/vote/item_security/%d/%s.emaul", voteId, fileBaseName);
        LOGGER.debug("filePath : " + filePath);
        File toServeUp = new File(filePath);
        return Responses.getFileEntity(toServeUp, fileBaseName + ".emaul");
    }

}
