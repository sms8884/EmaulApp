/**
 * 
 */
package com.jaha.server.emaul.common.crypto;

import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;

/**
 * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 27.
 * @description
 */
public class RsaKey implements Serializable {

    /**  */
    private static final long serialVersionUID = 662493106725455785L;

    /**
     * 기본 생성자
     */
    public RsaKey() {

    }

    /**
     * @param keyLength
     * @param modulus
     * @param exponent
     */
    public RsaKey(int keyLength, String modulus, String exponent) {
        this();
        this.keyLength = keyLength;
        this.modulus = new String(Base64.encodeBase64(modulus.getBytes()));
        this.exponent = new String(Base64.encodeBase64(exponent.getBytes()));
    }

    /**
     * RSA 키 길이
     */
    private int keyLength = 2048;
    /**
     * 계수(16진수 문자열을 Base64 인코딩한 값)
     */
    private String modulus;
    /**
     * 공개 지수(16진수 문자열을 Base64 인코딩한 값)
     */
    private String exponent;

    /**
     * @return the keyLength
     */
    public int getKeyLength() {
        return keyLength;
    }

    /**
     * @param keyLength the keyLength to set
     */
    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    /**
     * @return the modulus
     */
    public String getModulus() {
        return modulus;
    }

    /**
     * @param modulus the modulus to set
     */
    public void setModulus(String modulus) {
        this.modulus = new String(Base64.encodeBase64(modulus.getBytes()));
    }

    /**
     * @return the exponent
     */
    public String getExponent() {
        return exponent;
    }

    /**
     * @param exponent the exponent to set
     */
    public void setExponent(String exponent) {
        this.exponent = new String(Base64.encodeBase64(exponent.getBytes()));
    }

    @Override
    public String toString() {
        return "RsaKey [keyLength=" + keyLength + ", modulus=" + modulus + ", exponent=" + exponent + "]";
    }

}
