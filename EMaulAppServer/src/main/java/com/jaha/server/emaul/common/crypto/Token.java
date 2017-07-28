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
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @author 전강욱(realsnake@jahasmart.com), 2016. 6. 27.
 * @description
 *
 */
public class Token implements Serializable {

    /**  */
    private static final long serialVersionUID = 5930460573242674164L;

    /** 토큰 객체를 저장할 디렉토리 */
    public static final String TOKEN_SAVING_DIR = "C:\\Temp";
    /** 토큰 객체를 파일로 저장시 확장자 */
    public static final String TOKEN_SAVING_EXT = "tkn";

    /**
     * 기본 생성자
     */
    public Token() {
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     * @param keyLength
     * @param iterationCount
     * @param algo
     * @param uuid
     */
    public Token(int keyLength, int iterationCount, String algo) {
        this();
        this.keyLength = keyLength;
        this.iterationCount = iterationCount;
        this.algo = algo;
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     * @param keyLength
     * @param iterationCount
     * @param algo
     * @param uuid
     */
    public Token(int keyLength, int iterationCount, String algo, String uuid) {
        this();
        this.keyLength = keyLength;
        this.iterationCount = iterationCount;
        this.algo = algo;
        this.uuid = uuid;
    }

    /** 블록암호화 비밀키의 길이 */
    private int keyLength;

    private int iterationCount;

    /** 블록암호화 알고리즘 */
    private String algo;

    private String uuid;

    private String savingDir;

    private String savingExt;

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
     * @return the iterationCount
     */
    public int getIterationCount() {
        return iterationCount;
    }

    /**
     * @param iterationCount the iterationCount to set
     */
    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    /**
     * @return the algo
     */
    public String getAlgo() {
        return algo;
    }

    /**
     * @param algo the algo to set
     */
    public void setAlgo(String algo) {
        this.algo = algo;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @param savingDir the savingDir to set
     */
    public void setSavingDir(String savingDir) {
        this.savingDir = savingDir;
    }

    /**
     * @param savingExt the savingExt to set
     */
    public void setSavingExt(String savingExt) {
        this.savingExt = savingExt;
    }

    /**
     * UUID를 생성한다.
     * 
     * @return
     */
    public String generateUuid() {
        return UUID.randomUUID().toString();
    }


    /**
     * Token 객체를 파일로 저장한다.
     * 
     * @param token
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void saveToken(Token token) throws FileNotFoundException, IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TOKEN_SAVING_DIR + File.separator + token.getUuid() + "." + TOKEN_SAVING_EXT));
        oos.writeObject(token);
        oos.close();
    }

    /**
     * 파일로 저장한 Token를 읽어 반환한다. 별도 Token 파일 삭제 스케줄링 기능 필요
     * 
     * @param uuid
     * @return
     * @throws Exception
     */
    public Token readToken(String uuid) throws Exception {
        long cdt = new Date().getTime();

        File f = null;
        Token token = null;

        try {
            f = new File(TOKEN_SAVING_DIR + File.separator + uuid + "." + TOKEN_SAVING_EXT);
            long fcdt = f.lastModified();

            if ((cdt - fcdt) > 10000) { // 생성된지 10초가 지난 token라면 파기후 예외 처리!
                throw new Exception("* Token is timeover!");
            }

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            token = (Token) ois.readObject();
            ois.close();
        } catch (Exception e) {
            throw e;
        } finally {
            if (f != null && f.exists()) {
                f.delete();
            }
        }

        return token;
    }

    @Override
    public String toString() {
        return "Token [keyLength=" + keyLength + ", iterationCount=" + iterationCount + ", algo=" + algo + ", uuid=" + uuid + ", savingDir=" + savingDir + ", savingDir=" + savingExt + "]";
    }

}
