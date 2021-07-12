package com.kkl.kklplus.golden.service;


import com.kkl.kklplus.golden.entity.GoldenSysLog;
import com.kkl.kklplus.golden.mapper.GoldenSysLogMapper;
import com.kkl.kklplus.golden.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class GoldenSysLogService {

    private static final int DICT_LOCK_EXPIRED  = 60;

    @Resource
    private GoldenSysLogMapper goldensysLogMapper;


    /**
     * 添加异常日志
     */
    public void insert(Integer type,Long createId, String params,String exception, String title,String uri,String method){
        GoldenSysLog goldenSysLog = new GoldenSysLog();
        goldenSysLog.setType(type);
        goldenSysLog.setException(exception);
        goldenSysLog.setTitle(title);
        goldenSysLog.setParams(params);
        goldenSysLog.setCreateBy(createId);

        goldenSysLog.setQuarter(QuarterUtils.getQuarter(new Date(goldenSysLog.getCreateDate())));
        try {
            goldensysLogMapper.insert(goldenSysLog);
        }catch (Exception e){
            log.error("报错信息记录失败:{}",goldenSysLog.toString(),e);
        }
    }

    public void insert(Long createId, String params,String exception, String title,String uri,String method){
        GoldenSysLog sysLog = new GoldenSysLog();
        sysLog.setCreateDate(System.currentTimeMillis());
        sysLog.setType(1);
        sysLog.setCreateBy(createId);
        sysLog.setRequestUri(uri);
        sysLog.setMethod(method);
        sysLog.setParams(params);
        sysLog.setException(exception);
        sysLog.setTitle(title);
        sysLog.setQuarter(QuarterUtils.getQuarter(new Date(sysLog.getCreateDate())));
        try {
            goldensysLogMapper.insert(sysLog);
        }catch (Exception e){
            log.error("报错信息记录失败:{}",sysLog.toString(),e);
        }
    }

    public void insertModel(GoldenSysLog goldenSysLog){
        try {
            goldensysLogMapper.insert(goldenSysLog);
        }catch (Exception e){
            log.error("报错信息记录失败:{}",goldenSysLog.toString(),e);
        }

    }
}
