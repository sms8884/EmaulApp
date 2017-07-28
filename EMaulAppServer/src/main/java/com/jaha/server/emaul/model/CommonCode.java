package com.jaha.server.emaul.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class CommonCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JsonIgnore
    private CommonCodeId commonCodeId;

    @JsonIgnore
    @Column(insertable = false, updatable = false)
    private String codeGroup;

    @Column(insertable = false, updatable = false)
    private String code;

    /** 코드명 */
    private String name;

    /** 깊이 */
    @JsonIgnore
    private int depth;

    /** 순서 */
    private int sortOrder;

    /** 사용여부 */
    @JsonIgnore
    private String useYn;

    public CommonCodeId getCommonCodeId() {
        return commonCodeId;
    }

    public void setCommonCodeId(CommonCodeId commonCodeId) {
        this.commonCodeId = commonCodeId;
    }

    public String getCodeGroup() {
        return codeGroup;
    }

    public void setCodeGroup(String codeGroup) {
        this.codeGroup = codeGroup;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getUseYn() {
        return useYn;
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }

}


@Embeddable
class CommonCodeId implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 그룹코드 */
    private String codeGroup;

    /** 코드 */
    private String code;

    public String getCodeGroup() {
        return codeGroup;
    }

    public void setCodeGroup(String codeGroup) {
        this.codeGroup = codeGroup;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
