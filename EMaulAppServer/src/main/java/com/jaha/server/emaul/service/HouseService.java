package com.jaha.server.emaul.service;


import java.util.List;
import java.util.Map;

import com.jaha.server.emaul.model.Address;
import com.jaha.server.emaul.model.Apt;
import com.jaha.server.emaul.model.AptFee;
import com.jaha.server.emaul.model.AptFeeAvr;
import com.jaha.server.emaul.model.AptScheduler;
import com.jaha.server.emaul.model.GasCheck;
import com.jaha.server.emaul.model.House;
import com.jaha.server.emaul.model.User;

/**
 * Created by doring on 15. 3. 31..
 */
public interface HouseService {
    List<Apt> searchApt(String name);

    List<Apt> searchRegisteredApt(String name);

    /**
     * 전국아파트조회 ( 계약된 아파트 표시, 지번주소, 도로명주소 표기 )
     *
     * @params sidoNm, sggNm 필수
     * @params emdNm, aptNm, aptId 옵션
     */
    List<Map<String, Object>> searchAptAll(Map<String, Object> params);

    Apt getApt(Long aptId);

    House saveAndFlush(House house);

    House save(House house);

    House getHouse(Long aptId, String dong, String ho);

    int getUserCountIn(Long aptId, String dong);

    int getUserCountIn(Long aptId);

    AptFee saveAndFlush(AptFee aptFee);

    AptFee save(AptFee aptFee);

    AptFeeAvr save(AptFeeAvr aptFeeAvr);

    AptFeeAvr getAptFeeAvr(Long aptId, String date, String houseSize);

    void deleteAptFee(Long aptId, String date);

    AptFee getAptFee(Long houseId, String yyyyMM);

    AptFee getLastAptFee(Long houseId);

    List<AptFee> getAptFeeList(Long houseId);

    GasCheck saveAndFlush(GasCheck gas);

    List<GasCheck> getGasCheckList(Long userId);

    List<String> getSidoNames();

    List<String> getSigunguNames(String sidoName);

    List<String> getEupMyunDongNames(String sigunguName);

    List<String> getEupMyunDongNames(String sidoName, String sigunguName);

    List<Address> searchBuilding(String sidoName, String sigunguName, String eupmyundongName, String buildingName);

    List<House> getHouses(Long aptId);

    List<AptScheduler> apiAptSchedulerData(User user, String startDate, String endDate);

    AptScheduler apiAptSchedulerDetailData(User user, Long id);

    Apt getAptByAddressCode(String addressCode);

}
