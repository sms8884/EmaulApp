/**
 * 
 */
package com.jaha.server.emaul.common.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 27.
 * @description
 */
@Component
public class BlockCipherTemplate {

    public static final String DEFAULT_BLOCK_ALGO = "AES";
    public static final int DEFAULT_ITERATION_COUNT = 1024;
    public static final int DEFAULT_KEY_LENGTH = 128;

    /**
     * 비밀키를 생성하는 데 쓰일 salt(32bytes)를 생성한다.
     * 
     * @return
     * @throws NoSuchAlgorithmException
     */
    public String generateSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salts = new byte[32];
        sr.nextBytes(salts);
        return new String(Base64.encodeBase64(salts));
    }

    /**
     * PBKDF2 방식의 비밀키를 생성한다.
     * 
     * @param iterationCount
     * @param keyLength
     * @param password
     * @param salt
     * @return
     * @throws NoSuchAlgorithmException
     * @throws DecoderException
     * @throws InvalidKeySpecException
     */
    public String generateSecretKey(String authKey) throws NoSuchAlgorithmException, DecoderException, InvalidKeySpecException {
        int iterationCount = DEFAULT_ITERATION_COUNT;
        int keyLength = DEFAULT_KEY_LENGTH;
        String salt = StringUtils.rightPad(authKey, 32);

        return this.generateSecretKey(iterationCount, keyLength, authKey, salt);
    }

    /**
     * PBKDF2 방식의 비밀키를 생성한다.
     * 
     * @param iterationCount
     * @param keyLength
     * @param password
     * @param salt
     * @return
     * @throws NoSuchAlgorithmException
     * @throws DecoderException
     * @throws InvalidKeySpecException
     */
    public String generateSecretKey(String authKey, String salt) throws NoSuchAlgorithmException, DecoderException, InvalidKeySpecException {
        int iterationCount = DEFAULT_ITERATION_COUNT;
        int keyLength = DEFAULT_KEY_LENGTH;

        return this.generateSecretKey(iterationCount, keyLength, authKey, salt);
    }

    /**
     * PBKDF2 방식의 비밀키를 생성한다.
     * 
     * @param iterationCount
     * @param keyLength
     * @param password
     * @param salt
     * @return
     * @throws NoSuchAlgorithmException
     * @throws DecoderException
     * @throws InvalidKeySpecException
     */
    private String generateSecretKey(int iterationCount, int keyLength, String password, String salt) throws NoSuchAlgorithmException, DecoderException, InvalidKeySpecException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), iterationCount, keyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return new String(Base64.encodeBase64(secretKey.getEncoded()));
    }

    /**
     * 암호화된 평문을 복호화한다.
     * 
     * @param secretKey
     * @param encStr(Base64)
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws DecoderException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public String decrypt(String secretKey, String encStr) throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            DecoderException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        String algo = DEFAULT_BLOCK_ALGO;
        return this.decrypt(secretKey, algo, encStr);
    }

    /**
     * 암호화된 평문을 복호화한다.
     * 
     * @param secretKey
     * @param algo
     * @param encStr(Base64)
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws DecoderException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public String decrypt(String secretKey, String algo, String encStr) throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, DecoderException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        String iv = secretKey.substring(0, 16);
        return this.decrypt(secretKey, algo, iv, encStr);
    }

    /**
     * 암호화된 평문을 복호화한다.
     * 
     * @param secretKey
     * @param algo
     * @param iv(Hex)
     * @param encStr(Base64)
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws DecoderException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    private String decrypt(String secretKey, String algo, String iv, String encStr) throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, DecoderException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        SecretKey sk = new SecretKeySpec(Base64.decodeBase64(secretKey.getBytes()), algo);
        Cipher cipher = Cipher.getInstance(algo + "/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(iv.getBytes()));
        byte[] decryptedBytes = cipher.doFinal(Base64.decodeBase64(encStr.getBytes()));
        return new String(decryptedBytes, "UTF-8");
    }

    /**
     * 평문을 암호화한다.
     * 
     * @param secretKey
     * @param algo
     * @param plainText
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidParameterSpecException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public String encrypt(String secretKey, String plainText) throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String algo = DEFAULT_BLOCK_ALGO;
        return this.encrypt(secretKey, algo, plainText);
    }

    /**
     * 평문을 암호화한다.
     * 
     * @param secretKey
     * @param algo
     * @param plainText
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidParameterSpecException
     * @throws UnsupportedEncodingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidAlgorithmParameterException
     */
    public String encrypt(String secretKey, String algo, String plainText) throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidParameterSpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        SecretKey sk = new SecretKeySpec(Base64.decodeBase64(secretKey.getBytes()), algo);
        String iv = secretKey.substring(0, 16);
        Cipher c = Cipher.getInstance(algo + "/CBC/PKCS5Padding"); // AES/CBC/PKCS7Padding
        c.init(Cipher.ENCRYPT_MODE, sk, new IvParameterSpec(iv.getBytes()));

        byte[] bytesOfPlainText = new String(plainText).getBytes("UTF-8");
        byte[] encryptedBytes = c.doFinal(bytesOfPlainText);

        return new String(Base64.encodeBase64(encryptedBytes));
    }

    /**
     * public static void main(String[] args) throws Exception { String plainText = "안녕하세요? 전강욱입니다!";
     * 
     * BlockCipherTemplate bct = new BlockCipherTemplate();
     * 
     * // String salt = bct.generateSalt(); // System.out.println("* salt: " + salt); // String secretKey = bct.generateSecretKey("!@#s1982761", salt); // System.out.println("* secretKey: " +
     * secretKey);
     * 
     * String secretKey = bct.generateSecretKey("!@#s1982761"); System.out.println("* secretKey: " + secretKey);
     * 
     * String encStr = bct.encrypt(secretKey, plainText); System.out.println("* encStr(Base64): " + encStr);
     * 
     * plainText = bct.decrypt(secretKey, encStr); System.out.println("* plainText: " + plainText); }
     */

}
