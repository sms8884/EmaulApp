package com.jaha.server.emaul.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jaha.server.emaul.mapper.SystemFaqMapper;
import com.jaha.server.emaul.mapper.SystemNoticeMapper;
import com.jaha.server.emaul.model.Provision;
import com.jaha.server.emaul.model.SystemFaq;
import com.jaha.server.emaul.model.SystemNotice;
import com.jaha.server.emaul.model.User;
import com.jaha.server.emaul.repo.SystemFaqRepository;
import com.jaha.server.emaul.repo.SystemNoticeRepository;
import com.jaha.server.emaul.repo.SystemProvisionRepository;
import com.jaha.server.emaul.util.ScrollPage;
import com.jaha.server.emaul.util.StringUtil;

/**
 * Created by shavrani on 16. 06. 10..
 */
@Service
@Transactional
public class SystemServiceImpl implements SystemService {

    @Autowired
    private SystemNoticeMapper systemNoticeMapper;
    @Autowired
    private SystemNoticeRepository systemNoticeRepository;
    @Autowired
    private SystemFaqMapper systemFaqMapper;
    @Autowired
    private SystemFaqRepository systemFaqRepository;
    @Autowired
    private SystemProvisionRepository systemProvisionRepository;

    /**
     * @author shavrani 2016.06.13
     */
    @Override
    public ScrollPage<SystemNotice> getSystemNoticeList(User user, Map<String, Object> params) {

        ScrollPage<SystemNotice> scrollPage = new ScrollPage<SystemNotice>();

        int originPageSize = StringUtil.nvlInt(params.get("pageSize"));
        int pageSize = 11;// 기본 pageSize는 10 다음 페이지가 있는지 체크하기위해 +1개를 더 조회
        if (originPageSize > 0) {
            pageSize = originPageSize + 1;
        }

        params.put("pageSize", pageSize);

        List<SystemNotice> dataList = systemNoticeMapper.selectSystemNoticeList(params);

        int listSize = dataList.size();

        if (listSize > 0) {
            scrollPage.setFirstPageToken(String.valueOf(dataList.get(0).id));
        }

        if (listSize == pageSize) {

            dataList.remove(listSize - 1);// +1개의 row를 더 조회한 size를 구한후 마지막 row는 삭제하여 +1하기전의 list 상태로 만듬.

            // +1한 상태였을때 저장해논 listSize가 +1한 pageSize와 같으면 다음 페이지가 존재하는것임으로 nextPageToken을 입력한다.
            scrollPage.setNextPageToken(String.valueOf(dataList.get(dataList.size() - 1).id));

        }

        scrollPage.setContent(dataList);

        return scrollPage;

    }

    /**
     * @author shavrani 2016.09.12
     */
    @Override
    public int getSystemNoticeListCount(User user, Map<String, Object> params) {
        int count = systemNoticeMapper.selectSystemNoticeListCount(params);
        return count;
    }

    /**
     * @author shavrani 2016.06.10
     */
    @Override
    public SystemNotice getSystemNotice(User user, Map<String, Object> params) {
        return systemNoticeMapper.selectSystemNotice(params);
    }

    /**
     * @author shavrani 2016.06.10
     */
    @Override
    public ScrollPage<SystemFaq> getSystemFaqList(User user, Map<String, Object> params) {

        ScrollPage<SystemFaq> scrollPage = new ScrollPage<SystemFaq>();

        int originPageSize = StringUtil.nvlInt(params.get("pageSize"));
        int pageSize = 11;// 기본 pageSize는 10 다음 페이지가 있는지 체크하기위해 +1개를 더 조회
        if (originPageSize > 0) {
            pageSize = originPageSize + 1;
        }

        params.put("pageSize", pageSize);

        List<SystemFaq> dataList = systemFaqMapper.selectSystemFaqList(params);

        int listSize = dataList.size();

        if (listSize == pageSize) {

            dataList.remove(listSize - 1);// +1개의 row를 더 조회한 size를 구한후 마지막 row는 삭제하여 +1하기전의 list 상태로 만듬.

            // +1한 상태였을때 저장해논 listSize가 +1한 pageSize와 같으면 다음 페이지가 존재하는것임으로 nextPageToken을 입력한다.
            scrollPage.setNextPageToken(String.valueOf(dataList.get(dataList.size() - 1).id));
        }

        scrollPage.setContent(dataList);

        return scrollPage;

    }

    /**
     * @author shavrani 2016.06.10
     */
    @Override
    public SystemFaq getSystemFaq(User user, Map<String, Object> params) {
        return systemFaqMapper.selectSystemFaq(params);
    }

    @Override
    public Provision getSystemProvisionUseStatus(Long id, String status) {
        return systemProvisionRepository.findByIdAndStatus(id, status);
    }

}
