package com.kkl.kklplus.golden.controller;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkl.kklplus.entity.golden.entity.GoldenSettlements;
import com.kkl.kklplus.golden.commonEnum.GoldenProcessFlag;
import com.kkl.kklplus.golden.commonEnum.SubStatusEnum;
import com.kkl.kklplus.golden.entity.GoldenCallbackLog;

import com.kkl.kklplus.golden.entity.GoldenSysLog;
import com.kkl.kklplus.golden.entity.callback.RefundParam;
import com.kkl.kklplus.golden.entity.callback.RequestCallbackParam;
import com.kkl.kklplus.golden.http.config.GoldenProperties;

import com.kkl.kklplus.golden.service.CallbackLogService;
import com.kkl.kklplus.golden.service.GoldenSettlementsService;
import com.kkl.kklplus.golden.service.GoldenSysLogService;
import com.kkl.kklplus.golden.utils.AesUtil;
import com.kkl.kklplus.golden.utils.GsonUtils;
import com.kkl.kklplus.golden.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;


@Slf4j
@RestController
@RequestMapping("/api/callback/")
public class SettlementsCallbackController {

    @Autowired
    private GoldenSettlementsService goldenSettlementsService;

    @Autowired
    private GoldenProperties goldenProperties;

    @Autowired
    private GoldenSysLogService goldenSysLogService;

    @Autowired
    private CallbackLogService callbackLogService;

    /**
     *支付回调接口
     * @param req
     * @return
     * @throws IOException
     */
    @PostMapping(value = "payment")
    public String payment(HttpServletRequest req) throws IOException{
        String json = readReq(req);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        Gson gson = gsonBuilder.create();
        try {
            RequestCallbackParam requestCallbackParam = gson.fromJson(json,RequestCallbackParam.class);
            if (requestCallbackParam.getCode()==0) {
                goldenSettlementsService.goldenSettlement(requestCallbackParam);
            }else {
                GoldenSysLog goldenSysLog = new GoldenSysLog();
                goldenSysLog.setTitle("退款回调失败");
                goldenSysLog.setParams(json);
                goldenSysLog.setCreateDate(System.currentTimeMillis());
                goldenSysLog.setCreateBy(1L);
                goldenSysLog.setRequestUri("/callback/payment");
                goldenSysLog.setQuarter(QuarterUtils.getQuarter(new Date()));
                goldenSysLog.setType(1);
                goldenSysLogService.insertModel(goldenSysLog);
            }
        }catch (Exception e){
            GoldenSysLog goldenSysLog = new GoldenSysLog();
            goldenSysLog.setTitle("参数解析失败");
            goldenSysLog.setParams(json);
            goldenSysLog.setCreateDate(System.currentTimeMillis());
            goldenSysLog.setCreateBy(1L);
            goldenSysLog.setRequestUri("/callback/payment");
            goldenSysLog.setQuarter(QuarterUtils.getQuarter(new Date()));
            goldenSysLog.setType(1);
            goldenSysLogService.insertModel(goldenSysLog);
        }
        return "success";
    }

    /**
     * 退款回调接口
     * @param req
     * @return
     * @throws IOException
     */
    @PostMapping (value = "refund")
    public String refund(HttpServletRequest req) throws IOException {
        String json = readReq(req);
        try {
            RequestCallbackParam requestParam = GsonUtils.getInstance().fromJson(json, RequestCallbackParam.class);
            GoldenProperties.GoldenDataConfig goldenDataConfig = goldenProperties.getGoldenDataConfig();
                String data = requestParam.getData();
                if (StringUtils.isNotBlank(data)) {
                    String aesStr = AesUtil.decrypt(data, goldenDataConfig.getAppSecret()); // 解密
                    if (aesStr.isEmpty()){
                        GoldenSysLog goldenSysLog = new GoldenSysLog();
                        goldenSysLog.setCreateBy(1L);
                        goldenSysLog.setTitle("解密失败");
                        goldenSysLog.setType(1);
                        goldenSysLog.setParams(requestParam.getData());
                        goldenSysLog.setCreateDate(System.currentTimeMillis());
                        goldenSysLog.setQuarter(QuarterUtils.getQuarter(new Date()));
                        goldenSysLog.setRequestUri("/callback/payment");
                        goldenSysLogService.insertModel(goldenSysLog);
                        return "success";
                    }
                    Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                    RefundParam refundParam = gson.fromJson(aesStr,RefundParam.class);
                    GoldenSettlements goldenSettlements = goldenSettlementsService.selectByWithdrawNo(refundParam.getOrderRandomCode());
                    if (goldenSettlements != null) {
                        GoldenCallbackLog goldenCallbackLog = new GoldenCallbackLog();
                        goldenCallbackLog.setCreateBy(1L);
                        goldenCallbackLog.setCreateDate(System.currentTimeMillis());
                        goldenCallbackLog.setQuarter(goldenSettlements.getQuarter());
                        goldenCallbackLog.setSettlementId(goldenSettlements.getId());
                        goldenCallbackLog.setInfoJson(json);
                        goldenCallbackLog.setWithdrawNo(goldenSettlements.getWithdrawNo());
                        goldenCallbackLog.setInterfaceName("refund");
                        goldenCallbackLog.setProcessFlag(GoldenProcessFlag.PROCESS_FLAG_PROCESSING.value);
                        goldenCallbackLog.setProcessTime(1);
                        callbackLogService.insert(goldenCallbackLog);
                        if (requestParam.getCode() == 0) {  //  退款成功回调
                            try {
                                goldenCallbackLog.setProcessFlag(GoldenProcessFlag.PROCESS_FLAG_SUCCESS.value);
                                callbackLogService.updateCallbackLog(goldenCallbackLog);
                                goldenSettlements.setSubStatus(SubStatusEnum.SUCCESS_REFUND.getValue());
                                goldenSettlements.setUpdateDt(System.currentTimeMillis());
                                goldenSettlements.setRefundMerchantAmount(refundParam.getRefundMerchantAmount());
                                goldenSettlements.setRefundServiceAmount(refundParam.getRefundServiceAmount());
                                goldenSettlements.setChangeCode(refundParam.getChangeCode());
                                goldenSettlements.setChangeMerchantAmount(refundParam.getChangeMerchantAmount());
                                goldenSettlements.setChangeServiceAmount(refundParam.getChangeServiceAmount());
                                goldenSettlements.setSettlementCode(refundParam.getSettlementCode());
                                goldenSettlements.setProcessTime(1);
                                goldenSettlementsService.updateBySubStatus(goldenSettlements);
                            }catch (Exception e){
                                log.error("refund:数据插入异常:{}",e);
                            }
                        }else {
                            GoldenSysLog goldenSysLog = new GoldenSysLog();
                            goldenSysLog.setTitle("退款回调失败");
                            goldenSysLog.setParams(json);
                            goldenSysLog.setCreateDate(System.currentTimeMillis());
                            goldenSysLog.setCreateBy(1L);
                            goldenSysLog.setRequestUri("/callback/payment");
                            goldenSysLog.setQuarter(QuarterUtils.getQuarter(new Date()));
                            goldenSysLog.setType(1);
                            goldenSysLogService.insertModel(goldenSysLog);
                        }
                    }else {
                        GoldenSysLog goldenSysLog = new GoldenSysLog();
                        goldenSysLog.setCreateBy(1L);
                        goldenSysLog.setTitle("未找到结算单");
                        goldenSysLog.setType(1);
                        goldenSysLog.setParams(requestParam.getData());
                        goldenSysLog.setCreateDate(System.currentTimeMillis());
                        goldenSysLog.setQuarter(QuarterUtils.getQuarter(new Date()));
                        goldenSysLog.setRequestUri("/callback/payment");
                        goldenSysLogService.insertModel(goldenSysLog);
                    }
            }
        }catch (Exception e){
            log.error("refund:json转化异常:{}",json,e);
        }
        return "success";
    }

    public String readReq(HttpServletRequest req) throws IOException {
        // 读取参数
        InputStream inputStream;
        StringBuffer sb = new StringBuffer();
        inputStream = req.getInputStream();
        String s;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((s = in.readLine()) != null) {
            sb.append(s);
        }
        in.close();
        inputStream.close();
        String json = sb.toString();
        return json;
    }
}
