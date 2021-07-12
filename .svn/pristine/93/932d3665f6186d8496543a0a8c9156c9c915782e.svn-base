package com.kkl.kklplus.golden.service;

import com.kkl.kklplus.golden.entity.GoldenProcessLog;
import com.kkl.kklplus.golden.mapper.GoldenProcesslogMapper;
import com.kkl.kklplus.golden.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ProcesslogService {

    @Autowired
    private GoldenProcesslogMapper goldenProcesslogMapper;

    /**
     * 添加原始数据
     * @param processLog
     */
    public Integer insert(GoldenProcessLog processLog){
        return goldenProcesslogMapper.insert(processLog);
    }

    public void updateProcessFlag(GoldenProcessLog processLog) {
        try{
            processLog.setUpdateDate(System.currentTimeMillis());
            goldenProcesslogMapper.updateProcessFlag(processLog);
        }catch (Exception e) {
            String json = GsonUtils.getInstance().toGson(processLog);
            log.error("原始数据结果修改错误:{}", json,e);
        }
    }
}
