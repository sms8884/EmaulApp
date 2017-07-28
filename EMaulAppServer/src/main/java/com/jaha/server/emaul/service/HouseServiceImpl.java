package com.jaha.server.emaul.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.jaha.server.emaul.mapper.AptSchedulerMapper;
import com.jaha.server.emaul.mapper.HouseMapper;
import com.jaha.server.emaul.model.Address;
import com.jaha.server.emaul.model.Apt;
import com.jaha.server.emaul.model.AptFee;
import com.jaha.server.emaul.model.AptFeeAvr;
import com.jaha.server.emaul.model.AptScheduler;
import com.jaha.server.emaul.model.GasCheck;
import com.jaha.server.emaul.model.House;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.repo.AddressRepository;
import com.jaha.server.emaul.repo.AptFeeAvrRepository;
import com.jaha.server.emaul.repo.AptFeeRepository;
import com.jaha.server.emaul.repo.AptRepository;
import com.jaha.server.emaul.repo.AptSchedulerRepository;
import com.jaha.server.emaul.repo.GasCheckRepository;
import com.jaha.server.emaul.repo.HouseRepository;
import com.jaha.server.emaul.repo.UserRepository;
import com.jaha.server.emaul.util.AddressConverter;
import com.jaha.server.emaul.util.Orms;
import com.jaha.server.emaul.util.StringUtil;

/**
 * Created by doring on 15. 3. 31..
 */
@Service
public class HouseServiceImpl implements HouseService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AptRepository aptRepository;
    @Autowired
    private AptFeeRepository aptFeeRepository;
    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private GasCheckRepository gasCheckRepository;
    @Autowired
    private AptFeeAvrRepository aptFeeAvrRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AptSchedulerRepository aptSchedulerRepository;
    @Autowired
    private AptSchedulerMapper aptSchedulerMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private GcmService gcmService;
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private HouseMapper houseMapper;

    @Override
    public List<Apt> searchApt(String name) {
        List<Apt> list = aptRepository.findByNameContainingAndVirtualFalse(name);

        for (Apt apt : list) {
            apt.strAddress = AddressConverter.toStringAddress(apt.address);
            apt.strAddressOld = AddressConverter.toStringAddressOld(apt.address);
        }

        return list;
    }

    @Override
    public List<Apt> searchRegisteredApt(String name) {
        List<Apt> list = aptRepository.findByNameContainingAndRegisteredAptIsTrueAndVirtualFalse(name);

        for (Apt apt : list) {
            apt.strAddress = AddressConverter.toStringAddress(apt.address);
            apt.strAddressOld = AddressConverter.toStringAddressOld(apt.address);
        }

        return list;
    }

    @Override
    public List<Map<String, Object>> searchAptAll(Map<String, Object> params) {

        // 한글이 앱의 인코딩문제로 한칸공백이 '+' 로 찍히는 경우가 있어서 서버에서 일단처리.
        String sidoNm = StringUtil.nvl(params.get("sidoNm"));
        params.put("sidoNm", sidoNm.replaceAll("[+]", " "));

        String sggNm = StringUtil.nvl(params.get("sggNm"));
        params.put("sggNm", sggNm.replaceAll("[+]", " "));

        return houseMapper.selectAddressAptList(params);
    }

    @Override
    public Apt getApt(Long aptId) {
        return aptRepository.findOne(aptId);
    }

    @Override
    public House saveAndFlush(House house) {
        return houseRepository.saveAndFlush(house);
    }

    @Override
    public House save(House house) {
        return houseRepository.save(house);
    }

    @Override
    public House getHouse(Long aptId, String dong, String ho) {
        return houseRepository.findOneByAptIdAndDongAndHo(aptId, dong, ho);
    }

    @Override
    public int getUserCountIn(Long aptId, String dong) {
        List<House> houses = houseRepository.findByAptIdAndDong(aptId, dong);
        List<Long> houseIds = Lists.transform(houses, input -> input.id);
        return userRepository.findByHouseIdIn(houseIds).size();
    }

    @Override
    public int getUserCountIn(Long aptId) {
        List<House> houses = houseRepository.findByAptId(aptId);
        List<Long> houseIds = Lists.transform(houses, input -> input.id);
        return userRepository.findByHouseIdIn(houseIds).size();
    }

    @Override
    public AptFee saveAndFlush(AptFee aptFee) {
        return aptFeeRepository.saveAndFlush(aptFee);
    }

    @Override
    public AptFee save(AptFee aptFee) {
        return aptFeeRepository.save(aptFee);
    }

    @Override
    public AptFeeAvr save(AptFeeAvr aptFeeAvr) {
        return aptFeeAvrRepository.save(aptFeeAvr);
    }

    @Override
    public AptFeeAvr getAptFeeAvr(Long aptId, String date, String houseSize) {
        return aptFeeAvrRepository.findOneByAptIdAndDateAndHouseSize(aptId, date, houseSize);
    }

    @Override
    public void deleteAptFee(Long aptId, String date) {
        List<House> houseList = houseRepository.findByAptId(aptId);
        List<Long> ids = Lists.transform(houseList, input -> input.id);
        aptFeeRepository.deleteByHouseIdInAndDate(ids, date);
    }

    @Override
    public AptFee getAptFee(Long houseId, String yyyyMM) {
        return aptFeeRepository.findOneByHouseIdAndDate(houseId, yyyyMM);
    }

    @Override
    public AptFee getLastAptFee(Long houseId) {
        return aptFeeRepository.findOneByHouseId(houseId, new Sort(Sort.Direction.DESC, "date"));
    }

    @Override
    public List<AptFee> getAptFeeList(Long houseId) {
        return aptFeeRepository.findByHouseId(houseId, new Sort(Sort.Direction.DESC, "date"));
    }

    @Override
    public GasCheck saveAndFlush(GasCheck gas) {
        return gasCheckRepository.saveAndFlush(gas);
    }

    @Override
    public List<GasCheck> getGasCheckList(Long userId) {
        return gasCheckRepository.findByUserId(userId, new Sort(Sort.Direction.DESC, "date"));
    }

    @Override
    public List<String> getSidoNames() {
        String sql = "SELECT DISTINCT 시도명 FROM address;";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("시도명");
        });
    }

    @Override
    public List<String> getSigunguNames(String sidoName) {
        String sql = "SELECT DISTINCT 시군구명 FROM address WHERE 시도명=?;";

        // 한글이 앱의 인코딩문제로 한칸공백이 '+' 로 찍히는 경우가 있어서 서버에서 일단처리.
        if (!StringUtil.isBlank(sidoName)) {
            sidoName = sidoName.replaceAll("[+]", " ");
        }

        return jdbcTemplate.query(sql, new Object[] {sidoName}, (rs, rowNum) -> {
            return rs.getString("시군구명");
        });
    }

    @Override
    public List<String> getEupMyunDongNames(String sigunguName) {
        String sql = "SELECT DISTINCT 법정읍면동명 FROM address WHERE 시군구명=?;";

        // 한글이 앱의 인코딩문제로 한칸공백이 '+' 로 찍히는 경우가 있어서 서버에서 일단처리.
        if (!StringUtil.isBlank(sigunguName)) {
            sigunguName = sigunguName.replaceAll("[+]", " ");
        }

        return jdbcTemplate.query(sql, new Object[] {sigunguName}, (rs, rowNum) -> {
            return rs.getString("법정읍면동명");
        });
    }

    @Override
    public List<String> getEupMyunDongNames(String sidoName, String sigunguName) {
        String sql = "SELECT DISTINCT 법정읍면동명 FROM address WHERE 시도명=? AND 시군구명=?;";

        // 한글이 앱의 인코딩문제로 한칸공백이 '+' 로 찍히는 경우가 있어서 서버에서 일단처리.
        if (!StringUtil.isBlank(sidoName)) {
            sidoName = sidoName.replaceAll("[+]", " ");
        }
        if (!StringUtil.isBlank(sigunguName)) {
            sigunguName = sigunguName.replaceAll("[+]", " ");
        }

        return jdbcTemplate.query(sql, new Object[] {sidoName, sigunguName}, (rs, rowNum) -> {
            return rs.getString("법정읍면동명");
        });
    }

    @Override
    public List<Address> searchBuilding(String sidoName, String sigunguName, String eupmyundongName, String buildingName) {
        String sql;
        if (eupmyundongName != null && !eupmyundongName.isEmpty()) {
            sql = "SELECT * FROM address WHERE 시도명=? AND 시군구명=? AND 법정읍면동명=? AND 시군구용건물명 like ? AND 비고1 != 'virtual' GROUP BY 건물본번, 건물부번";
            return jdbcTemplate.query(sql, new Object[] {sidoName, sigunguName, eupmyundongName, "%" + buildingName + "%"}, (rs, rowNum) -> {
                try {
                    return Orms.mapping(rs, Address.class);
                } catch (Exception e) {
                    logger.error("", e);
                }
                return null;
            });
        } else {
            sql = "SELECT * FROM address WHERE 시도명=? AND 시군구명=? AND 시군구용건물명 like ? AND 비고1 != 'virtual' GROUP BY 건물본번, 건물부번";
            return jdbcTemplate.query(sql, new Object[] {sidoName, sigunguName, "%" + buildingName + "%"}, (rs, rowNum) -> {
                try {
                    return Orms.mapping(rs, Address.class);
                } catch (Exception e) {
                    logger.error("", e);
                }
                return null;
            });
        }
    }

    @Override
    public List<House> getHouses(Long aptId) {
        return houseRepository.findByAptId(aptId);
    }

    @Override
    public List<AptScheduler> apiAptSchedulerData(User user, String startDate, String endDate) {

        List<AptScheduler> result = null;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("aptId", user.house.apt.id);

        if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
            startDate = startDate.length() == 6 ? startDate + "01" : startDate;
            endDate = endDate.length() == 6 ? endDate + "01" : endDate;
            params.put("startDate", startDate);
            params.put("endDate", endDate);
        }

        if (user.type.admin) {
            params.put("searchGubun", "1");// 전체랑 관리자인것
            params.put("noticeTarget1", "1");
            params.put("noticeTarget2", "2");
            result = aptSchedulerMapper.selectAptSchedulerList(params);
        } else if (user.type.user || user.type.houseHost) {
            params.put("searchGubun", "2");// 전체랑 동이같거나 동호가 같은것
            params.put("noticeTarget1", "1");
            params.put("noticeTarget2", "3");
            params.put("noticeTargetDong", user.house.dong);
            params.put("noticeTargetHo", user.house.ho);
            result = aptSchedulerMapper.selectAptSchedulerList(params);
        }

        return result;
    }

    @Override
    public AptScheduler apiAptSchedulerDetailData(User user, Long id) {
        AptScheduler aptScheduler = aptSchedulerRepository.findByIdAndAptId(id, user.house.apt.id);
        return aptScheduler;
    }

    @Override
    public Apt getAptByAddressCode(String addressCode) {
        Address address = addressRepository.findOne(addressCode);
        return aptRepository.findByAddress(address);

    }

}
