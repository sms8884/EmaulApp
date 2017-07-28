/**
 * 
 */
package com.jaha.server.emaul.common.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

/**
 * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 27.
 * @description
 */
@Component
public class RsaCipherTemplate {
    public static final String ALGO_RSA = "RSA";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final int RSA_KEY_LENGTH = 2048;
    public static final String PRIKEY_SAVING_DIR = "C:\\Temp";
    public static final String PRIKEY_SAVING_EXT = "pse";

    private int keyLength;

    /**
     * @param keyLength the keyLength to set
     */
    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    /**
     * RSA 공개키와 개인키를 생성한 후 개인키를 파일로 저장한다.
     * 
     * @param keyLength
     * @param uuid
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public RsaKey generateRsaKey(int keyLength, String uuid) throws NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException, IOException {
        SecureRandom sr = new SecureRandom();
        sr.nextInt();

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGO_RSA);
        kpg.initialize(keyLength, sr);

        KeyPair kp = kpg.genKeyPair();
        KeyFactory kf = KeyFactory.getInstance(ALGO_RSA);

        // RSA에서 PublicKey의 byte[]은 modulus와 exponent의 조합으로 이루어진 ASN.1 포맷(publicKey.getEncoded())
        PublicKey publicKey = kp.getPublic(); // 공개키(사용자에게 발급)
        PrivateKey privateKey = kp.getPrivate(); // 개인키(서버에 저장)

        // System.out.println(publicKey.getAlgorithm());
        // System.out.println(publicKey.getFormat());
        // System.out.println(publicKey.toString());
        // System.out.println("* 공개키: " + new String(Base64.encodeBase64(publicKey.getEncoded())));

        RSAPublicKeySpec rpks = kf.getKeySpec(publicKey, RSAPublicKeySpec.class);
        String publicKeyModulus = rpks.getModulus().toString(16); // 계수, 16진수, BigInteger의 바이트 배열에서 2의 보수를 제거한 후 16진수 문자열을 반환
        String publicKeyExponent = rpks.getPublicExponent().toString(16); // 공개 지수, 16진수, 여기서는 65537로 항상 동일

        saveRsaPrivateKey(uuid, privateKey);

        return new RsaKey(keyLength, publicKeyModulus, publicKeyExponent);
    }

    /**
     * RSA 공개키와 개인키를 생성한 후 개인키를 파일로 저장한다.
     * 
     * @param uuid
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public RsaKey generateRsaKey(String uuid) throws NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException, IOException {
        if (this.keyLength < RSA_KEY_LENGTH) {
            this.keyLength = RSA_KEY_LENGTH;
        }
        return generateRsaKey(this.keyLength, uuid);
    }

    /**
     * RSA 개인키를 파일로 저장한다.
     * 
     * @param uuid
     * @param privateKey
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void saveRsaPrivateKey(String uuid, PrivateKey privateKey) throws FileNotFoundException, IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PRIKEY_SAVING_DIR + File.separator + uuid + "." + PRIKEY_SAVING_EXT));
        oos.writeObject(privateKey);
        oos.close();
    }

    /**
     * 파일로 저장한 RSA 개인키를 읽어 반환한다. 별도 개인키 파일 삭제 스케줄링 기능 필요
     * 
     * @param uuid
     * @return
     * @throws Exception
     */
    public static PrivateKey readRsaPrivateKey(String uuid) throws Exception {
        long cdt = new Date().getTime();

        File f = null;
        PrivateKey privateKey = null;

        try {
            f = new File(PRIKEY_SAVING_DIR + File.separator + uuid + "." + PRIKEY_SAVING_EXT);
            long fcdt = f.lastModified();

            if ((cdt - fcdt) > 10000) { // 생성된지 10초가 지난 개인키라면 파기후 예외 처리!
                throw new Exception("* PrivateKey is timeover!");
            }

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            privateKey = (PrivateKey) ois.readObject();
            ois.close();
        } catch (Exception e) {
            throw e;
        } finally {
            if (f != null && f.exists()) {
                f.delete();
            }
        }

        return privateKey;
    }

    /**
     * Base64로 인코딩된 RSA로 암호화된 문자열을 복호화한다.
     * 
     * @param uuid UUID
     * @param encStr Base64로 인코딩된 암호화된 문자열
     * @return
     * @throws Exception
     */
    public String decrypt(String uuid, String encStr) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGO_RSA);
        PrivateKey privateKey = readRsaPrivateKey(uuid);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedBytes = cipher.doFinal(Base64.decodeBase64(encStr.getBytes()));
        String decStr = new String(decryptedBytes, CHARSET_UTF8);

        return decStr;
    }

    /**
     * 문자열을 RSA로 암호화한 후 BASE64 인코딩된 문자열을 반환한다.
     * 
     * @param modulus 계수의 16진수 문자열을 Base64 인코딩한 값
     * @param exponent 공개 지수의 16진수 문자열을 Base64 인코딩한 값
     * @param plainText 암호화할 평문
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws UnsupportedEncodingException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public String encrypt(String modulus, String plainText)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        String exponent = "MTAwMDE=";

        return this.encrypt(modulus, exponent, plainText);
    }

    /**
     * 문자열을 RSA로 암호화한 후 BASE64 인코딩된 문자열을 반환한다.
     * 
     * @param modulus 계수의 16진수 문자열을 Base64 인코딩한 값
     * @param exponent 공개 지수의 16진수 문자열을 Base64 인코딩한 값
     * @param plainText 암호화할 평문
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws UnsupportedEncodingException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public String encrypt(String modulus, String exponent, String plainText)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        String modulus16 = new String(Base64.decodeBase64(modulus.getBytes()));
        String exponent16 = new String(Base64.decodeBase64(exponent.getBytes()));

        KeyFactory kf = KeyFactory.getInstance(ALGO_RSA);
        PublicKey publicKey = kf.generatePublic(new RSAPublicKeySpec(new BigInteger(modulus16, 16), new BigInteger(exponent16, 16)));

        Cipher cipher = Cipher.getInstance(ALGO_RSA);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytesOfPlainText = new String(plainText).getBytes(CHARSET_UTF8);
        byte[] encryptedBytes = cipher.doFinal(bytesOfPlainText);

        return new String(Base64.encodeBase64(encryptedBytes));
    }



    /**
     * public static void main(String[] args) throws Exception { Token token = new Token(); RsaCipherTemplate rct = new RsaCipherTemplate();
     * 
     * String uuid = token.getUuid(); // System.out.println("* uuid: " + uuid);
     * 
     * RsaKey rki = rct.generateRsaKey(uuid); System.out.println(rki.toString());
     * 
     * String plainText = "안녕하세요? 전강욱입니다."; // String encStr = rct.encrypt(rki.getModulus(), rki.getExponent(), plainText); String encStr = rct.encrypt(rki.getModulus(), plainText);
     * System.out.println("* RSA 암호화로 암호화된 평문: " + encStr);
     * 
     * String decStr = rct.decrypt(uuid, encStr); System.out.println("* RSA 복호화로 복호화된 암호문: " + decStr); }
     */

}
