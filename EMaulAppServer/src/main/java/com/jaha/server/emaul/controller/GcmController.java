package com.jaha.server.emaul.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jaha.server.emaul.model.GcmSendForm;
import com.jaha.server.emaul.model.House;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.service.GcmService;
import com.jaha.server.emaul.service.HouseService;
import com.jaha.server.emaul.service.UserService;
import com.jaha.server.emaul.util.RandomKeys;
import com.jaha.server.emaul.util.Responses;
import com.jaha.server.emaul.util.SessionAttrs;
import com.jaha.server.emaul.util.StringUtil;
import com.jaha.server.emaul.util.Thumbnails;

/**
 * Created by doring on 15. 6. 23..
 */
@Controller
public class GcmController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcmController.class);

    @Autowired
    private GcmService gcmService;
    @Autowired
    private UserService userService;
    @Autowired
    private HouseService houseService;

    @RequestMapping(method = RequestMethod.POST, value = "/api/gcm/send")
    public @ResponseBody String handleGcmSend(HttpServletRequest req, @RequestParam(value = "title") String title, @RequestParam(value = "message") String message,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        Map<String, String> msg = Maps.newHashMap();
        msg.put("type", "notification");
        msg.put("title", StringUtil.nvl(title, ""));
        msg.put("value", message);
        msg.put("action", "emaul://message-box");

        User user = userService.getUser(SessionAttrs.getUserId(req.getSession()));
        if (!user.type.admin && !user.type.jaha) {
            return "1";
        }
        List<House> houses = houseService.getHouses(user.house.apt.id);
        List<Long> houseIds = Lists.transform(houses, input -> input.id);
        List<User> users = userService.getUsersByHouseIn(houseIds);

        if (image != null) {
            String randomKey = System.currentTimeMillis() + RandomKeys.make(6);
            try {
                File dir = new File(String.format("/nas/EMaul/gcm/image/%s/%s", user.house.apt.id, randomKey));
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File dest = new File(dir, String.format("%s.jpg", 0));
                dest.createNewFile();
                image.transferTo(dest);
                Thumbnails.create(dest, 540, 300);

                msg.put("image", "/api/public/gcm/image/" + user.house.apt.id + "/" + randomKey + "/0-thumb.jpg");
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }

        GcmSendForm form = new GcmSendForm();
        form.setMessage(msg);
        if (user.type.jaha) {
            form.setUserIds(Lists.newArrayList(user.id));
        } else {
            form.setUserIds(Lists.transform(users, input -> input.id));
        }
        gcmService.sendGcm(form);

        return "0";
    }

    @RequestMapping(value = "/api/public/gcm/image/{aptId}/{key}/{fileName:.+}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> handleImageRequest(@PathVariable("aptId") String aptId, @PathVariable("key") String key, @PathVariable("fileName") String fileName) {

        File toServeUp = new File("/nas/EMaul/gcm/image", String.format("/%s/%s/%s", aptId, key, fileName));

        return Responses.getFileEntity(toServeUp, key + "-" + fileName);
    }
}
