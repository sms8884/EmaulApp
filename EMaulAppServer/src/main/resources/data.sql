# 문자 전송 테이블
CREATE TABLE IF NOT EXISTS `uds_msg` (
  `CMID`         VARCHAR(32) NOT NULL,
  `UMID`         VARCHAR(32)         DEFAULT NULL,
  `MSG_TYPE`     INT(11)             DEFAULT '0',
  `STATUS`       INT(11)             DEFAULT '0',
  `REQUEST_TIME` DATETIME    NOT NULL,
  `SEND_TIME`    DATETIME    NOT NULL,
  `REPORT_TIME`  DATETIME            DEFAULT NULL,
  `DEST_PHONE`   VARCHAR(16) NOT NULL,
  `DEST_NAME`    VARCHAR(32)
                 CHARACTER SET euckr DEFAULT NULL,
  `SEND_PHONE`   VARCHAR(16)         DEFAULT NULL,
  `SEND_NAME`    VARCHAR(32)
                 CHARACTER SET euckr DEFAULT NULL,
  `SUBJECT`      VARCHAR(60)
                 CHARACTER SET euckr DEFAULT NULL,
  `MSG_BODY`     VARCHAR(2000)
                 CHARACTER SET euckr DEFAULT NULL,
  `WAP_URL`      VARCHAR(80)         DEFAULT NULL,
  `COVER_FLAG`   INT(11)             DEFAULT '0',
  `SMS_FLAG`     INT(11)             DEFAULT '0',
  `REPLY_FLAG`   INT(11)             DEFAULT '0',
  `RETRY_CNT`    INT(11)             DEFAULT '1',
  `FAX_FILE`     VARCHAR(128)        DEFAULT NULL,
  `VXML_FILE`    VARCHAR(64)         DEFAULT NULL,
  `CALL_STATUS`  INT(11)             DEFAULT '9',
  `USE_PAGE`     INT(11)             DEFAULT '0',
  `USE_TIME`     INT(11)             DEFAULT '0',
  `SN_RESULT`    INT(11)             DEFAULT '0',
  `WAP_INFO`     VARCHAR(10)         DEFAULT '-',
  `CINFO`        VARCHAR(32)         DEFAULT NULL,
  `ETC1`         VARCHAR(16)         DEFAULT NULL,
  `ETC2`         VARCHAR(120)        DEFAULT NULL,
  PRIMARY KEY (`CMID`),
  KEY `idx_search_uds_msg` (`STATUS`, `CALL_STATUS`, `REQUEST_TIME`, `DEST_PHONE`, `ETC1`, `ETC2`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


INSERT IGNORE INTO `emaul`.`vote_type` (`id`, `main`, `sub`) VALUES ('1', 'vote', 'default');
INSERT IGNORE INTO `emaul`.`vote_type` (`id`, `main`, `sub`) VALUES ('2', 'vote', 'candidate');
INSERT IGNORE INTO `emaul`.`vote_type` (`id`, `main`, `sub`) VALUES ('3', 'vote', 'yn');
INSERT IGNORE INTO `emaul`.`vote_type` (`id`, `main`, `sub`) VALUES ('4', 'poll', 'default');

# 자하 아파트
INSERT IGNORE INTO `emaul`.`apt` (`id`, `name`, `address_code`, `latitude`, `longitude`, `registered_apt`)
VALUES ('1', '자하아파트', '1153010200101970013022875', '37.485532', '126.892485', '1');

INSERT IGNORE INTO `emaul`.`house` (`id`, `apt_id`, `dong`, `ho`) VALUES ('1', '1', '101', '101');

# 커뮤니티
INSERT IGNORE INTO `emaul`.`board_category` (`id`, `name`, `ord`, `type`, `apt_id`, `json_array_readable_user_type`, `json_array_writable_user_type`)
VALUES ('1', '오늘', '1', 'today', '1', '["jaha","admin","user","gasChecker","anonymous"]', '["jaha"]');
INSERT IGNORE INTO `emaul`.`board_category` (`id`, `name`, `ord`, `type`, `apt_id`, `json_array_readable_user_type`, `json_array_writable_user_type`)
VALUES ('2', '공지사항', '1', 'notice', '1', '["jaha","admin","user","gasChecker","anonymous"]', '["jaha","admin"]');
INSERT IGNORE INTO `emaul`.`board_category` (`id`, `name`, `ord`, `type`, `apt_id`, `json_array_readable_user_type`, `json_array_writable_user_type`)
VALUES ('3', '주민 게시판', '2', 'community', '1', '["jaha","admin","user","gasChecker","anonymous"]',
        '["jaha","admin","user","anonymous"]');

# 버전 관리
INSERT IGNORE INTO `emaul`.`app_version` (`id`, `kind`, `version_code`, `version_name`, force_version_code)
VALUES ('1', 'android', '1', '1.0', '0');

# 투표, 설문
INSERT IGNORE INTO `emaul`.`vote` (`id`, `description`, `end_date`, `image_count`, `multiple_choice`, `number_enabled`, `question`, `reg_date`, `start_date`, `status`, `target_apt`, `target_dong`, `title`, `visible`, `type_id`)
VALUES ('1', '투표 테스트', now(), '0', 0, 1, '질문', now(), now(), 'ready', '1', '', '제목', 1, '1');
INSERT IGNORE INTO `emaul`.`vote` (`id`, `description`, `end_date`, `image_count`, `multiple_choice`, `number_enabled`, `question`, `reg_date`, `start_date`, `status`, `target_apt`, `target_dong`, `title`, `visible`, `type_id`)
VALUES ('2', '설문 테스트', now(), '0', 0, 1, '질문', now(), now(), 'ready', '1', '', '제목', 1, '4');

INSERT IGNORE INTO `emaul`.`vote_item` (`id`, `image_count`, `is_subjective`, `parent_id`, `title`)
VALUES ('1', '0', 0, '1', '투표 항목 1');
INSERT IGNORE INTO `emaul`.`vote_item` (`id`, `image_count`, `is_subjective`, `parent_id`, `title`)
VALUES ('2', '0', 0, '1', '투표 항목 2');
INSERT IGNORE INTO `emaul`.`vote_item` (`id`, `image_count`, `is_subjective`, `parent_id`, `title`)
VALUES ('3', '0', 0, '1', '투표 항목 3');
INSERT IGNORE INTO `emaul`.`vote_item` (`id`, `image_count`, `is_subjective`, `parent_id`, `title`)
VALUES ('4', '0', 0, '1', '투표 항목 4');
INSERT IGNORE INTO `emaul`.`vote_item` (`id`, `image_count`, `is_subjective`, `parent_id`, `title`)
VALUES ('5', '0', 0, '2', '설문 항목 1');
INSERT IGNORE INTO `emaul`.`vote_item` (`id`, `image_count`, `is_subjective`, `parent_id`, `title`)
VALUES ('6', '0', 0, '2', '설문 항목 2');
INSERT IGNORE INTO `emaul`.`vote_item` (`id`, `image_count`, `is_subjective`, `parent_id`, `title`)
VALUES ('7', '0', 0, '2', '설문 항목 3');
INSERT IGNORE INTO `emaul`.`vote_item` (`id`, `image_count`, `is_subjective`, `parent_id`, `title`)
VALUES ('8', '0', 1, '2', '기타');



# DB 테이블 생성시 한 번만 실행되어야 함.
# ALTER TABLE `emaul`.`board_comment`
# DROP FOREIGN KEY `FK_board_post`;
# ALTER TABLE `emaul`.`board_comment`
# ADD CONSTRAINT `FK_board_post`
# FOREIGN KEY (`post_id`)
# REFERENCES `emaul`.`board_post` (`id`)
#   ON DELETE CASCADE
#   ON UPDATE CASCADE;


# DB 테이블 생성 후
# voter 테이블의 vote, vote_item fk를 cascade로 바꿔줘야 함.










# 여기 이하는 디비 최초 생성시 실행되어야 함
#SET GLOBAL event_scheduler = ON;

# DELIMITER |
#
# CREATE EVENT event_vote_updater
#   ON SCHEDULE
#     EVERY 1 MINUTE
# DO
#   BEGIN
#     DECLARE time_now DATETIME;
#     SET time_now = now();
#
#     UPDATE emaul.vote v
#     SET v.status = CASE WHEN v.start_date <= time_now AND v.end_date >= time_now
#       THEN 'active'
#                    ELSE
#                      CASE WHEN v.start_date >= time_now
#                        THEN 'ready'
#                      ELSE 'done' END END
#     WHERE v.status != 'done';
#   END |
#
# DELIMITER ;



# DELIMITER |
#
# CREATE EVENT event_traffic_cache_updater
#   ON SCHEDULE
#     EVERY 1 MINUTE
# DO
#   BEGIN
#     DELETE FROM emaul.traffic_cache WHERE cache_date < DATE_SUB(NOW(), INTERVAL expire_minutes MINUTE);
#   END |
#
# DELIMITER ;



# DELIMITER $$
# CREATE TRIGGER inc_empathy AFTER INSERT ON board_empathy
# FOR EACH ROW
#   BEGIN
#     UPDATE board_post SET count_empathy = count_empathy + 1 WHERE id=NEW.post_id;
#   END $$
# DELIMITER ;
#
# DELIMITER $$
# CREATE TRIGGER dec_empathy AFTER DELETE ON board_empathy
# FOR EACH ROW
#   BEGIN
#     UPDATE board_post SET count_empathy = count_empathy - 1 WHERE id=OLD.post_id;
#   END $$
# DELIMITER ;


# CREATE DEFINER=`root`@`%` TRIGGER inc_post_comment_count AFTER INSERT ON board_comment
# FOR EACH ROW
#   BEGIN
#     UPDATE board_post SET comment_count = comment_count + 1 WHERE id=NEW.post_id;
#   END
# CREATE DEFINER=`root`@`%` TRIGGER dec_post_comment_count AFTER DELETE ON board_comment
# FOR EACH ROW
#   BEGIN
#     UPDATE board_post SET comment_count = comment_count - 1 WHERE id=OLD.post_id;
#   END


# CREATE DEFINER=`root`@`%` TRIGGER inc_comment_reply_count AFTER INSERT ON board_comment_reply
# FOR EACH ROW
#   BEGIN
#     UPDATE board_comment SET reply_count = reply_count + 1 WHERE id=NEW.comment_id;
#   END
# CREATE DEFINER=`root`@`%` TRIGGER dec_comment_reply_count AFTER DELETE ON board_comment_reply
# FOR EACH ROW
#   BEGIN
#     UPDATE board_comment SET reply_count = reply_count - 1 WHERE id=OLD.comment_id;
#   END