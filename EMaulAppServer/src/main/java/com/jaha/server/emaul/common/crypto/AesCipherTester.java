/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 7. 13.
 */
package com.jaha.server.emaul.common.crypto;

import com.sybase.powerbuilder.cryptography.Base64;
import com.sybase.powerbuilder.cryptography.PBCrypto;

/**
 * <pre>
 * Class Name : AesCipherTester.java
 * Description : Description
 *
 * Modification Information
 *
 * Mod Date         Modifier    Description
 * -----------      --------    ---------------------------
 * 2016. 7. 13.     전강욱      Generation
 * </pre>
 *
 * @author 전강욱
 * @since 2016. 7. 13.
 * @version 1.0
 */
public class AesCipherTester {

    public void testAesWithPBCryto() {
        try {

            PBCrypto pbc = new PBCrypto();

            String uuid = "5c79f579-faef-4abf-990e-1a830dea61a0"; // 무인택배함 고유키
            String authKey = "rZdfElECxUEhzyXd"; // 보안인증키
            String date = "20170215";

            ///////////////////////////////////////////////////////////////////////// 무인택배함에서의 암호화 /////////////////////////////////////////////////////////////////////////
            // 1. 보안인증키를 BASE64 인코딩한다.
            String base64EncodedAuthKey = Base64.encodeString(authKey);
            System.out.println("0. base64EncodedAuthKey:\n" + base64EncodedAuthKey);
            System.out.println();

            StringBuilder json = new StringBuilder();

            // 8645 김현지 01093908485
            // 5955 김수연 01041294211
            // 8628 송명섭 01088842053
            // 2808 오진영 01041615545
            // 8641 윤형주 01042407708
            // 태영선임님 : 01097279985
            // 허부장님 : 01039260816
            // 8649 황소연 01082331398
            // 4842 이경훈 01099189492
            // 5442 강지만 01042607670
            // 6037 유빛나 01055978516

            // 8956 황소연 01082331398
            // 8723 이경훈 01099189492
            // 8940 전강욱 01057147515
            // 8987 최은혜 01031154786
            // 10841 김성아 01065137202

            // 8651 김세린 01080219909
            // 8653 최은혜 01031154787
            // 8652 최대현 01097556986
            // 8646 최태영 01097279985
            // 5442 강지만 01042607670

            String phoneNumber = "01057147515";
            int apiNumber = 1;

            if (apiNumber == 1) {
                // API 1번
                json.append("{");
                json.append("\"uuid\":\"").append(uuid).append("\"");
                json.append(",\"authKey\":\"").append(authKey).append("\"");
                json.append(",\"lockerNum\":\"1\"");
                json.append(",\"password\":\"12345\"");
                json.append(",\"dong\":100");
                json.append(",\"ho\":100");
                json.append(",\"parcelCompanyId\":\"33368\"");
                json.append(",\"phone\":\"").append(phoneNumber).append("\"");
                json.append(",\"parcelPhone\":\"01012345678\"");
                json.append(",\"date\":\"").append(date).append("\"");
                json.append("}");
            } else if (apiNumber == 2) {
                // API 2번
                json.append("{");
                json.append("\"uuid\":\"").append(uuid).append("\"");
                json.append(",\"authKey\":\"").append(authKey).append("\"");
                json.append(",\"lockerNum\":\"1\"");
                json.append(",\"dong\":100");
                json.append(",\"ho\":100");
                json.append(",\"phone\":\"").append(phoneNumber).append("\"");
                json.append(",\"date\":\"").append(date).append("\"");
                json.append("}");
            } else if (apiNumber == 3) {
                // API 3번
                json.append("{");
                json.append("\"uuid\":\"").append(uuid).append("\"");
                json.append(",\"authKey\":\"").append(authKey).append("\"");
                json.append(",\"lockerNum\":\"1\"");
                json.append(",\"dong\":100");
                json.append(",\"ho\":100");
                json.append(",\"phone\":\"").append(phoneNumber).append("\"");
                json.append(",\"parcelPhone\":\"01012345678\"");
                json.append(",\"date\":\"").append(date).append("\"");
                json.append("}");
            } else if (apiNumber == 4) {
                // API 4번
                json.append("{");
                json.append("\"uuid\":\"").append(uuid).append("\"");
                json.append(",\"authKey\":\"").append(authKey).append("\"");
                json.append(",\"lockerNum\":\"1\"");
                json.append(",\"dong\":100");
                json.append(",\"ho\":100");
                json.append(",\"phone\":\"").append(phoneNumber).append("\"");
                json.append(",\"parcelPhone\":\"01012345678\"");
                json.append(",\"date\":\"").append(date).append("\"");
                json.append("}");
            } else if (apiNumber == 5) {
                // API 5번
                json.append("{");
                json.append("\"uuid\":\"").append(uuid).append("\"");
                json.append(",\"authKey\":\"").append(authKey).append("\"");
                json.append(",\"lockerNum\":\"1\"");
                json.append(",\"dong\":100");
                json.append(",\"ho\":100");
                json.append(",\"phone\":\"").append(phoneNumber).append("\"");
                json.append(",\"parcelPhone\":\"01012345678\"");
                json.append(",\"date\":\"").append(date).append("\"");
                json.append("}");
            } else if (apiNumber == 6) {
                // API 6번
                json.append("{");
                json.append("\"uuid\":\"").append(uuid).append("\"");
                json.append(",\"authKey\":\"").append(authKey).append("\"");
                json.append(",\"lockerNum\":\"1\"");
                json.append(",\"password\":\"12345\"");
                json.append(",\"dong\":100");
                json.append(",\"ho\":100");
                json.append(",\"phone\":\"").append(phoneNumber).append("\"");
                json.append(",\"parcelPhone\":\"01012345678\"");
                json.append(",\"date\":\"").append(date).append("\"");
                json.append("}");
            }

            // 2. json 데이터를 AES 암호화한다.
            String encData = pbc.encryptPlainTextUsingBlockCipher("AES", base64EncodedAuthKey, json.toString());
            System.out.println("1. 암호화된 json:\n" + encData);
            System.out.println();


            ///////////////////////////////////////////////////////////////////////// 자하스마트에서의 복호화 /////////////////////////////////////////////////////////////////////////
            // encData = "W/Jl4X2GSVajIc61Qw2yvkKn4nVRamc54rRnLbvOvH/yz+ZUh+vBy4En4e+O3BI4T1o8U/pqWNgZ" + "\n" + "nUEPK/6vyMnOJqmufMCmeqfSgd2ncXbu9JWxDIOF4xSN9m7cnVnQgn1pAILlkuxLOmdpau8uAQ6a" + "\n"
            // + "bNC4SKWTy/X9cTNEuaQTwILuluACldfD7xczApQAnVic3YuY56fL5a5LDbHaFWuyFuz3FeqbMAte" + "\n" + "UUod4VHvHx77ISr0DxsH8xeq8JfHADH1E2AYKqPG3WDRwKOi5WDklDICDupQqtyT+elQU7vFu/qy" + "\n"
            // + "3ezYu8JwfZ3TLSYc";
            // System.out.println("1. 암호화된 json:\n" + encData);
            // System.out.println();

            String decData = pbc.decryptCipherTextUsingBlockCipher("AES", base64EncodedAuthKey, encData);
            System.out.println("2. 복호화된 json:\n" + decData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // public static void main(String[] args) {
    // AesCipherTester act = new AesCipherTester();
    // act.testAesWithPBCryto();
    // }

}
