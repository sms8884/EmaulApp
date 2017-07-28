/**
 *
 */
package com.jaha.server.emaul.v2.mapper.group;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.jaha.server.emaul.v2.model.group.GroupAdminVo;

/**
 * @author 조영태(cyt@jahasmart.com) <br />
 *         This Mapper class mapped db-table called groupadmin_target_area
 */
@Mapper
public interface GroupAdminMapper {

    /**
     * 그룹 관리자 정보 조회
     *
     * @param groupAdmin
     * @return
     */
    public List<GroupAdminVo> selectGroupAdminByArea(GroupAdminVo groupAdmin);

}
