/**
 * Copyright (c) 2016 JAHA SMART CORP., LTD ALL RIGHT RESERVED
 *
 * 2016. 7. 13.
 */
package com.jaha.server.emaul.common.crypto;

import java.security.NoSuchAlgorithmException;

import com.sybase.powerbuilder.cryptography.Base64;
import com.sybase.powerbuilder.cryptography.PBCrypto;

/**
 * <pre>
 * Class Name : RsaCipherTester.java
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
public class RsaCipherTester {

    public void testRsaKeyCreator() {
        PBCrypto pbc = new PBCrypto();
        try {
            String[] rsaKeys = pbc.createRSAKeyPair();
            System.out.println("Public key : \n" + rsaKeys[0]);
            System.out.println("Private Key : \n" + rsaKeys[1]);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void testRsaWithPBCrypto() {
        try {
            PBCrypto pbc = new PBCrypto();

            StringBuilder sb1 = new StringBuilder();
            sb1.append("MIGdMA0GCSqGSIb3DQEBAQUAA4GLADCBhwKBgQCX1M+GFTOhJl9oErk1Rak87pRuatB7N1DG3WeE");
            sb1.append("\n").append("2i6uvNe5s7vegeTucLFUt57ToEIoA1TFbtKxE0GqdHurB6N4BnU/85LNYvCOB8ZABKKgOff2Xv4G");
            sb1.append("\n").append("abkNY2nuPOWSvbgPd+u3FL+xIC81r5/tCy3tPYKEAkK/mi/tl154OsOuqQIBEQ==");

            String publicKey = sb1.toString();

            String uuid = "5c79f579-faef-4abf-990e-1a830dea61a0";
            String authToken = uuid + "||" + "20170215175959";

            ///////////////////////////////////////////////////////////////////////// 무인택배함에서의 암호화 /////////////////////////////////////////////////////////////////////////

            // 1. authToken 값을 Base64 인코딩
            String base64EncodedAuthToken = Base64.encodeString(authToken);
            System.out.println("base64EncodedAuthToken:\n" + base64EncodedAuthToken);
            System.out.println();

            // 2. RSA 암호화
            String encAuthToken = pbc.encryptSecretKeyUsingRsaPublicKey(base64EncodedAuthToken, publicKey);
            System.out.println("encAuthToken:\n" + encAuthToken);
            System.out.println();

            ///////////////////////////////////////////////////////////////////////// 자하스마트에서의 복호화 /////////////////////////////////////////////////////////////////////////
            StringBuilder sb2 = new StringBuilder();
            sb2.append("MIICdAIBADANBgkqhkiG9w0BAQEFAASCAl4wggJaAgEAAoGBAJfUz4YVM6EmX2gSuTVFqTzulG5q");
            sb2.append("\n").append("0Hs3UMbdZ4TaLq6817mzu96B5O5wsVS3ntOgQigDVMVu0rETQap0e6sHo3gGdT/zks1i8I4HxkAE");
            sb2.append("\n").append("oqA59/Ze/gZpuQ1jae485ZK9uA9367cUv7EgLzWvn+0LLe09goQCQr+aL+2XXng6w66pAgERAoGA");

            sb2.append("\n").append("EdzNHtVRXkC/7iBSBkRuQ2dcwbI2pRWRCFZIafuNBX+g6Ku7v9ML39EFzbs/3KlxMeJGNVhVBcYH");
            sb2.append("\n").append("ubNZ1+LH79IauNogQdHauYGELVTEl4gzrlqW2Sr6zXBFLg4MIQT+YMuNirLkQ+bBICjJQrF4Dz0L");
            sb2.append("\n").append("LjNUTVsBYRSlxs6Vo/ECQQCxHN4wuDiX+EHJ4Apq7oKZzLGhmH+X0SCDlr26Q3sSJAaSrigpJls7");

            sb2.append("\n").append("SS90uvov1iQnWT3de+CgIEms1gJrKwQRAkEA23U+hMhlUzUjcQK0SS0VmnP6u2NQ5JQapYcpCzr/");
            sb2.append("\n").append("ASHSIwpz+wUUu3x04DY79ttHj0o9cHpNdIPquwdb86C5GQJBAKaxwg+8cX/4tmOlr3OzTb3t1Fva");
            sb2.append("\n").append("0nDE006r/dx7vyAh6BGU2oEVCpImw0Cv+odgIgbqlJQ4WvEPVGZvEVXOIfECQBnRjuJx7c2Nx+8t");

            sb2.append("\n").append("fp8yewMctBYLrynzTm3TqnnKtJa4r09qpDumPqylOu0zjpWDNZhjFkl3zOCH/X9qRw2acCECQEhk");
            sb2.append("\n").append("9yS5dg4q2er+a5aS7I4o99G3ek41nqzawM/jJhjlFfkLvVd3cuL6plTuTqA/j6RTpSq+i0B3Oadi");
            sb2.append("\n").append("LKwuWw4=");

            String privateKey = sb2.toString();

            String decAuthToken = pbc.decryptSecretKeyUsingRsaPrivateKey(encAuthToken, privateKey);
            System.out.println("decAuthToken:\n" + decAuthToken);
            System.out.println();

            String base64DecodedAuthToken = Base64.decodeToString(decAuthToken);
            System.out.println("base64DecodedAuthToken:\n" + base64DecodedAuthToken);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // public static void main(String[] args) {
    // RsaCipherTester rct = new RsaCipherTester();
    // rct.testRsaWithPBCrypto();
    // }

}
