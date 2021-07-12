package com.kkl.kklplus.golden.service;

import com.kkl.kklplus.golden.entity.GoldenCallbackLog;
import com.kkl.kklplus.golden.mapper.GoldenCallbackLogMapper;
import com.kkl.kklplus.golden.utils.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CallbackLogService {

    @Autowired
    private GoldenCallbackLogMapper goldenCallbackLogMapper;

    /**
     * 添加原始数据
     * @param callbackLog
     */
    public Integer insert(GoldenCallbackLog callbackLog){
        return goldenCallbackLogMapper.insert(callbackLog);
    }

    public void updateCallbackLog(GoldenCallbackLog callbackLog) {
        try{
            callbackLog.setUpdateDate(System.currentTimeMillis());
            goldenCallbackLogMapper.update(callbackLog);
        }catch (Exception e) {
            String json = GsonUtils.getInstance().toGson(callbackLog);
            log.error("原始数据结果修改错误:{}", json,e);
        }
    }

    public GoldenCallbackLog selectBySettlementId(Long id){
        return goldenCallbackLogMapper.selectOne(id);
    }


}