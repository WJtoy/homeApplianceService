package com.kkl.kklplus.golden.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;

import com.kkl.kklplus.entity.golden.entity.GoldenSettlements;
import com.kkl.kklplus.entity.golden.entity.SettlementsSearchModel;
import com.kkl.kklplus.entity.golden.mq.MQGoldenSettlementsMessage;
import com.kkl.kklplus.entity.golden.mq.MQGoldenSettlementsStatusUpdateMessage;
import com.kkl.kklplus.golden.commonEnum.*;
import com.kkl.kklplus.golden.entity.GoldenCallbackLog;
import com.kkl.kklplus.golden.entity.GoldenProcessLog;
import com.kkl.kklplus.golden.entity.GoldenSysLog;
import com.kkl.kklplus.golden.entity.callback.PaymentParam;
import com.kkl.kklplus.golden.entity.callback.RequestCallbackParam;


import com.kkl.kklplus.golden.http.command.OperationCommand;
import com.kkl.kklplus.golden.http.config.GoldenProperties;
import com.kkl.kklplus.golden.http.request.CommonParam;
import com.kkl.kklplus.golden.http.request.CreateForBatchParam;
import com.kkl.kklplus.golden.http.response.ResponseBody;
import com.kkl.kklplus.golden.http.utils.OkHttpUtils;

import com.kkl.kklplus.golden.mapper.GoldenSettlementsMapper;

import com.kkl.kklplus.golden.mq.sender.GoldenSettlementMessageSender;
import com.kkl.kklplus.golden.utils.AesUtil;
import com.kkl.kklplus.golden.utils.DateUtils;
import com.kkl.kklplus.golden.utils.GsonUtils;

import com.kkl.kklplus.golden.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GoldenSettlementsService {

    @Autowired
    private GoldenSettlementsMapper goldenSettlementsMapper;

    @Autowired
    private GoldenSettlementMessageSender goldenSettlementMessageSender;

    @Autowired
    private GoldenSysLogService sysLogService;

    @Autowired
    private ProcesslogService processlogService;

    @Autowired
    private GoldenProperties goldenProperties;

    @Autowired
    private CallbackLogService callbackLogService;

    private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();


    public void checkSettlements(GoldenSettlements goldenSettlements) {
        try {
            if (goldenSettlements.getWithdrawNo() == null ){
                return ;
            }
            if (goldenSettlements.getCertificateType() == null){
                return ;
            }
            if (goldenSettlements.getPaymentWay() == null){
                return ;
            }
            if (goldenSettlements.getBankName() == null){
                return ;
            }
            if (goldenSettlements.getPayAmount() == null || goldenSettlements.getPayAmount().intValue() < 1){
                return ;
            }
            if (goldenSettlements.getBankOwnerIdno() == null){
                return ;
            }
            if (goldenSettlements.getBankOwnerPhone() == null){
                return ;
            }
            goldenSettlements.setPayBy(1L);
            goldenSettlements.setServiceStatus(1);
            goldenSettlements.setUpdateBy(1L);
            goldenSettlements.setUpdateDt(System.currentTimeMillis());
            goldenSettlements.setPayDate(System.currentTimeMillis());
            goldenSettlements.setStatus(StatusEnum.NEW_REQUEST.getValue());
            goldenSettlements.setSubStatus(SubStatusEnum.PROCESS.getValue());
            goldenSettlementsMapper.insert(goldenSettlements);

            settlementNotify(goldenSettlements);    //发送结算单请求业务
        } catch (Exception e) {
            log.error(e.getMessage());
            GoldenSysLog goldenSysLog = new GoldenSysLog();
            goldenSysLog.setCreateBy(1L);
            goldenSysLog.setCreateDate(System.currentTimeMillis());
            goldenSysLog.setMethod("POST");
            goldenSysLog.setRequestUri("/balance/CreateForBatch");
            goldenSysLog.setType(1);
            goldenSysLog.setException(e.getMessage());
            goldenSysLog.setTitle("插入异常");
            goldenSysLog.setQuarter(goldenSettlements.getQuarter());
            if (StringUtils.contains(e.getMessage(), "Duplicate")) {
                goldenSysLog.setException(e.getMessage());
                goldenSysLog.setTitle("数据库中数据重复定义，请确认");
            }
            sysLogService.insertModel(goldenSysLog);
        }
    }

    public MSResponse settlementNotify(GoldenSettlements goldenSettlements){
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        try {
            CreateForBatchParam createForBatchParam = new CreateForBatchParam();
            createForBatchParam.setName(goldenSettlements.getBankOwner());
            createForBatchParam.setSettleAmount(goldenSettlements.getPayAmount());
            createForBatchParam.setPaymentWay(goldenSettlements.getPaymentWay());
            createForBatchParam.setOrderRandomCode(String.valueOf(goldenSettlements.getWithdrawId()));
            createForBatchParam.setPhoneNum(goldenSettlements.getBankOwnerPhone());
            createForBatchParam.setBankName(goldenSettlements.getBankName());
            createForBatchParam.setCertificateType(goldenSettlements.getCertificateType());
            createForBatchParam.setCertificateNum(goldenSettlements.getBankOwnerIdno());
            createForBatchParam.setBankcardNum(goldenSettlements.getBankNo());
            //发送请求
            OperationCommand command = OperationCommand.newInstance
                    (OperationCommand.OperationCode.CREATEFORBATCH, createForBatchParam);
            String jsonString = OkHttpUtils.toJsonString(command);
            GoldenProcessLog goldenProcesslog = new GoldenProcessLog();
            goldenProcesslog.setInfoJson(jsonString);
            goldenProcesslog.setInterfaceName(OperationCommand.OperationCode.CREATEFORBATCH.apiUrl);
            goldenProcesslog.setProcessFlag(GoldenProcessFlag.PROCESS_FLAG_ACCEPT.value);
            goldenProcesslog.setCreateBy(goldenSettlements.getCreateBy());
            goldenProcesslog.setCreateDate(System.currentTimeMillis());
            goldenProcesslog.setUpdateBy(goldenSettlements.getCreateBy());
            goldenProcesslog.setWithdrawNo(goldenSettlements.getWithdrawNo());
            goldenProcesslog.setSettlementId(goldenSettlements.getId());
            goldenProcesslog.setUpdateDate(System.currentTimeMillis());
            goldenProcesslog.setQuarter(goldenSettlements.getQuarter());
            processlogService.insert(goldenProcesslog);
            CommonParam commonParam = new CommonParam();
            commonParam.setRequestId(String.valueOf(goldenProcesslog.getId()));
            commonParam.setTime(goldenProcesslog.getCreateDate()/1000);
            commonParam.setCallBackUrl(goldenProperties.getCallbackUrl() + "/api/callback/payment");
            ResponseBody<ResponseBody> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseBody.class, commonParam);

            goldenProcesslog.setResultJson(resBody.getOriginalJson());
            ResponseBody data = resBody.getData();
            if (resBody.getCode() == 0 && data != null && data.getCode() == 0) {
                goldenSettlements.setStatus(StatusEnum.PROCESS.getValue());
                goldenSettlements.setSubStatus(SubStatusEnum.PROCESS.getValue());
                goldenSettlements.setUpdateDt(System.currentTimeMillis());
                goldenSettlementsMapper.updateReqResult(goldenSettlements);
                goldenProcesslog.setProcessFlag(GoldenProcessFlag.PROCESS_FLAG_SUCCESS.value);
                processlogService.updateProcessFlag(goldenProcesslog);
            } else {
                String errorMsg = resBody.getMsg();
                if (data != null) {
                    errorMsg = data.getMsg();
                }
                errorMsg = StringUtils.left(errorMsg, 200);
                goldenSettlements.setProcessComment(errorMsg);
                goldenSettlements.setUpdateDt(System.currentTimeMillis());
                goldenSettlements.setSubStatus(SubStatusEnum.REQ_EXCEPTION.getValue());
                goldenSettlementsMapper.updateReqResult(goldenSettlements);
                goldenProcesslog.setProcessFlag(GoldenProcessFlag.PROCESS_FLAG_FAILURE.value);
                goldenProcesslog.setProcessComment(errorMsg);
                processlogService.updateProcessFlag(goldenProcesslog);
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg(errorMsg);
            }
        }catch (Exception e){
            String gson = GsonUtils.getInstance().toGson(goldenSettlements);
            log.error("结算单请求发送异常:{}",gson,e);
            sysLogService.insert(1L,gson, e.getMessage(),
                    "结算单请求发送异常",OperationCommand.OperationCode.CREATEFORBATCH.apiUrl, "POST");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));
        }
        return  msResponse;
    }






    /**
     * 支付回调业务
     * @param requestCallbackParam
     * @return
     */
    public void goldenSettlement(RequestCallbackParam requestCallbackParam) {
        if (requestCallbackParam.getData() != null) {
            String aesStr = AesUtil.decrypt(requestCallbackParam.getData(), goldenProperties.getGoldenDataConfig().getAppSecret());
            if (aesStr.isEmpty()) {  //解密失败
                GoldenSysLog goldenSysLog = new GoldenSysLog();
                goldenSysLog.setCreateBy(1L);
                goldenSysLog.setTitle("解密失败");
                goldenSysLog.setType(1);
                goldenSysLog.setParams(requestCallbackParam.getData());
                goldenSysLog.setCreateDate(System.currentTimeMillis());
                goldenSysLog.setQuarter(QuarterUtils.getQuarter(new Date()));
                goldenSysLog.setRequestUri("/callback/payment");
                sysLogService.insertModel(goldenSysLog);
                return;
            }
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
            Gson gson = gsonBuilder.create();
            try {
                List<PaymentParam> paymentParamList = gson.fromJson(aesStr, new TypeToken<List<PaymentParam>>() {
                }.getType());
                payReason(paymentParamList);    //打款原因
            }catch (Exception e){
                GoldenSysLog goldenSysLog = new GoldenSysLog();
                goldenSysLog.setQuarter(QuarterUtils.getQuarter(new Date()));
                goldenSysLog.setParams(aesStr);
                goldenSysLog.setException(e.getMessage());
                goldenSysLog.setCreateBy(1L);
                goldenSysLog.setCreateDate(System.currentTimeMillis());
                goldenSysLog.setTitle("json格式化失败");
                goldenSysLog.setType(1);
                goldenSysLog.setRequestUri("");
                sysLogService.insertModel(goldenSysLog);
            }
        }
    }


        public void payReason(List<PaymentParam> paymentParamList){
                for (PaymentParam paymentParam : paymentParamList) {
                    if (paymentParam.getOrderRandomCode() != null) {
                        String paramJson = gson.toJson(paymentParam);
                        GoldenSettlements goldenSettlements = goldenSettlementsMapper.selectOne(paymentParam.getOrderRandomCode());
                        if (goldenSettlements != null) {
                            GoldenCallbackLog goldenCallbackLog = new GoldenCallbackLog();
                            goldenCallbackLog.setSettlementId(goldenSettlements.getId());
                            goldenCallbackLog.setCreateBy(1L);
                            goldenCallbackLog.setCreateDate(System.currentTimeMillis());
                            goldenCallbackLog.setProcessFlag(GoldenProcessFlag.PROCESS_FLAG_PROCESSING.value);
                            goldenCallbackLog.setInfoJson(paramJson);
                            goldenCallbackLog.setProcessTime(1);
                            goldenCallbackLog.setInterfaceName("/callback/payment");
                            goldenCallbackLog.setQuarter(goldenSettlements.getQuarter());
                            goldenCallbackLog.setWithdrawNo(goldenSettlements.getWithdrawNo());
                            callbackLogService.insert(goldenCallbackLog);
                            try{
                                if (goldenSettlements.getStatus() <= StatusEnum.HONG_UP.getValue()) {
                                    if (paymentParam.getStatus() == CreateForBatchEnum.PAID.getValue()) {
                                        Date date  = DateUtils.StringDateTimeToDate(paymentParam.getPayTime());
                                        goldenSettlements.setPayDate(date.getTime());
                                        goldenSettlements.setPayForYear(DateUtils.getYear(date));
                                        goldenSettlements.setPayForMonth(DateUtils.getMonth(date));
                                        goldenSettlements.setSettlementCode(paymentParam.getSettlementCode());
                                        resSettlements(goldenSettlements, StatusEnum.PAY_SUCCESS.getValue(),goldenCallbackLog);
                                    } else if (paymentParam.getStatus() == CreateForBatchEnum.PAY_FAIL.getValue()) {
                                        goldenSettlements.setProcessComment(paymentParam.getFailReason());
                                        goldenSettlements.setProcessTime(goldenSettlements.getProcessTime()+1);
                                        goldenSettlements.setFailReason(paymentParam.getFailReason());
                                        if (paymentParam.getVerificationErrorCode() !=null ) {
                                            List<String> paramList = gson.fromJson(paymentParam.getVerificationErrorCode(), new TypeToken<List<String>>() {
                                            }.getType());
                                            goldenSettlements.setVerificationErrorCode(Integer.valueOf(paymentParam.getVerificationErrorCode()));
                                        }
                                        goldenSettlements.setSubStatus(SubStatusEnum.REFUNDED.getValue());
                                        resSettlements(goldenSettlements, StatusEnum.PAY_FAIL.getValue(),goldenCallbackLog);
                                    } else {
                                        goldenSettlements.setHangupMsg(paymentParam.getHangupMsg());
                                        goldenSettlements.setProcessTime(goldenSettlements.getProcessTime()+1);
                                        if (paymentParam.getHangupFlag()) {
                                            resSettlements(goldenSettlements, StatusEnum.HONG_UP.getValue(),goldenCallbackLog);
                                        }
                                    }
                                }
                            }catch (Exception e){
                                log.error("数据错误异常:{}",e);
                            }
                        } else {
                            GoldenSysLog goldenSysLog = new GoldenSysLog();
                            goldenSysLog.setCreateBy(1L);
                            goldenSysLog.setCreateDate(System.currentTimeMillis());
                            goldenSysLog.setTitle("商户订单号不存在");
                            goldenSysLog.setType(1);
                            goldenSysLog.setParams(paramJson);
                            goldenSysLog.setException("商户订单不存在");
                            goldenSysLog.setQuarter(QuarterUtils.getQuarter(new Date()));
                            sysLogService.insertModel(goldenSysLog);
                        }
                    }
                }
        }

    /**
     * 更新打款状态-->发送mq通知
     * @param goldenSettlements
     * @param status
     */
    public void resSettlements(GoldenSettlements goldenSettlements ,Integer status ,GoldenCallbackLog goldenCallbackLog){
            goldenSettlements.setStatus(status);
            goldenSettlements.setUpdateDt(System.currentTimeMillis());
            goldenSettlements.setUpdateBy(1L);
            if (goldenSettlements.getPayDate() != null){
                goldenSettlements.setPayDate(goldenSettlements.getPayDate());
            }
            Integer count = goldenSettlementsMapper.updateByStatus(goldenSettlements);
            String remarks = "";
            if (goldenSettlements.getRemarks() != null){
                remarks = goldenSettlements.getRemarks();
            }
            if (count>0) {
                //发送mq
                MQGoldenSettlementsStatusUpdateMessage.GoldenSettlementsStatusUpdateMessage goldenSettlementMessage =
                        MQGoldenSettlementsStatusUpdateMessage.GoldenSettlementsStatusUpdateMessage.newBuilder()
                                .setCreateBy(goldenSettlements.getCreateBy())
                                .setPayTime(goldenSettlements.getPayDate())
                                .setStatus(goldenSettlements.getStatus())
                                .setQuarter(goldenSettlements.getQuarter())
                                .setRemarks(remarks)
                                .setWithdrawId(goldenSettlements.getWithdrawId())
                                .setPayAmount(goldenSettlements.getPayAmount())
                                .setId(goldenCallbackLog.getId())
                                .build();
                goldenSettlementMessageSender.send(goldenSettlementMessage);
            }

            goldenCallbackLog.setProcessFlag(GoldenProcessFlag.PROCESS_FLAG_SUCCESS.value);
            goldenCallbackLog.setUpdateBy(1L);
            goldenCallbackLog.setUpdateDate(System.currentTimeMillis());
            callbackLogService.updateCallbackLog(goldenCallbackLog);
    }

    /**
     * 手动关闭退款
     * @param id
     * @param updateById
     * @param updateDt
     * @return
     */
    public Integer closeRefund(Long id, Long updateById, long updateDt) {
        return goldenSettlementsMapper.closeRefund(id,updateById,updateDt);
    }

    /**
     * 批量查询付款单对应的结算单的状态及其异常原因
     * @param withdrawIds
     * @return
     */
    public Map<Long, GoldenSettlements> batchSearchSettlementsByWithdrawIds(List<Long> withdrawIds) {
        return goldenSettlementsMapper.batchSearchSettlementsByWithdrawIds(withdrawIds);
    }

    public MSResponse<Integer> settlementsRetry(GoldenSettlements settlements) {
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        Long id = settlements.getId();
        try {
            Integer count = 0;
            if (id != null && id > 0) {
                count = goldenSettlementsMapper.updateStatusToExecute(id);
                if(count == 0){
                    msResponse.setErrorCode(MSErrorCode.FAILURE);
                    msResponse.setMsg("数据正在处理,请刷新!");
                    return msResponse;
                }
            } else {
                count = goldenSettlementsMapper.insert(settlements);
            }
            return settlementNotify(settlements);
        }catch (Exception e){
            String gson = GsonUtils.getInstance().toGson(settlements);
            log.error("结算单重发异常:{}",gson,e);
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            if (StringUtils.contains(e.getMessage(), "Duplicate")) {
                msResponse.setMsg("数据正在处理,请刷新!");
            }else{
                msResponse.setMsg(StringUtils.left(e.getMessage(),200));
            }
            return msResponse;
        }
    }

    /**
     * @param id
     * @return
     */
    public GoldenSettlements selectById(Long id) {
        return goldenSettlementsMapper.selectById(id);
    }




    /**
     * @param goldenSettlements
     * @return
     */
    public Integer updateResult(GoldenSettlements goldenSettlements){
       return goldenSettlementsMapper.updateResult(goldenSettlements);
    }

    /**
     * @param goldenSettlements
     * @return
     */
    public Integer updateReqResult(GoldenSettlements goldenSettlements){
        return goldenSettlementsMapper.updateReqResult(goldenSettlements);
    }

    /**
     * @param withdrawNo
     * @return
     */
    public GoldenSettlements selectByWithdrawNo(String withdrawNo){
        return goldenSettlementsMapper.selectOne(withdrawNo);
    }

    /**
     * 退款成功操作
     * @param goldenSettlements
     * @return
     */
    public Integer updateBySubStatus(GoldenSettlements goldenSettlements){
        return goldenSettlementsMapper.updateBySubStatus(goldenSettlements);
    }




    /**
     * 支付款失败列表查询
     * @param
     * @return
     */
    public Page<GoldenSettlements> getPayFailList(SettlementsSearchModel settlementsSearchModel) {
        if (settlementsSearchModel != null) {
            PageHelper.startPage(settlementsSearchModel.getPageNo(), settlementsSearchModel.getPageSize());
            return goldenSettlementsMapper.selectFailListByStatus(settlementsSearchModel);
        }else {
            return null;
        }
    }

    /***
     * 查询处理中的列表
     * @param
     * @return
     */
    public Page<GoldenSettlements> getProcessList(SettlementsSearchModel settlementsSearchModel) {
        if (settlementsSearchModel != null) {
            PageHelper.startPage(settlementsSearchModel.getPageNo(), settlementsSearchModel.getPageSize());
            return goldenSettlementsMapper.selectProcessListByStatus(settlementsSearchModel);
        }else {
            return null;
        }
    }

    /**
     * 查询付款成功的列表
     * @param settlementsSearchModel
     * @return
     */
    public Page<GoldenSettlements> getSuccessList(SettlementsSearchModel settlementsSearchModel) {
        if (settlementsSearchModel != null) {
            PageHelper.startPage(settlementsSearchModel.getPageNo(), settlementsSearchModel.getPageSize());
            return goldenSettlementsMapper.selectSuccessListByStatus(settlementsSearchModel);
        }else {
            return null;
        }
    }
}
