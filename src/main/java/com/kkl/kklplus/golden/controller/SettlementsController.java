package com.kkl.kklplus.golden.controller;

import com.github.pagehelper.Page;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.golden.entity.GoldenSettlements;
import com.kkl.kklplus.entity.golden.entity.SettlementsSearchModel;
import com.kkl.kklplus.golden.commonEnum.GoldenProcessFlag;

import com.kkl.kklplus.golden.commonEnum.SubStatusEnum;
import com.kkl.kklplus.golden.entity.GoldenProcessLog;

import com.kkl.kklplus.golden.http.command.OperationCommand;
import com.kkl.kklplus.golden.http.config.GoldenProperties;
import com.kkl.kklplus.golden.http.request.CommonParam;
import com.kkl.kklplus.golden.http.request.RefundBalance;
import com.kkl.kklplus.golden.http.response.ResponseBody;
import com.kkl.kklplus.golden.http.utils.OkHttpUtils;
import com.kkl.kklplus.golden.service.GoldenSettlementsService;
import com.kkl.kklplus.golden.service.GoldenSysLogService;
import com.kkl.kklplus.golden.service.ProcesslogService;
import com.kkl.kklplus.golden.utils.GsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

@Api(tags = "结算单操作接口")
@Slf4j
@RestController
@RequestMapping("/settlements/")
public class SettlementsController {

    @Autowired
    private GoldenSettlementsService settlementsService;

    @Autowired
    private GoldenSysLogService sysLogService;

    @Autowired
    private ProcesslogService processlogService;

    @Autowired
    private GoldenProperties goldenProperties;


    private static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    @ApiOperation("关闭退款")
    @GetMapping("closeRefund/{id}/{updateById}")
    public MSResponse<Integer> closeRefund(@PathVariable("id")Long id,@PathVariable("updateById")Long updateById) {
        try {
            Integer count = settlementsService.closeRefund(id, updateById, System.currentTimeMillis());
            return new MSResponse(MSErrorCode.SUCCESS,count);
        }catch (Exception e){
            log.error("关闭退款异常:{}",id,e);
            sysLogService.insert(updateById,id.toString(), e.getMessage(),
                    "关闭退款异常","settlements/closeRefund", "GET");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)),0);
        }
    }

    @ApiOperation("批量查询付款单对应的结算单的状态及其异常原因")
    @PostMapping("batchSearchSettlementsByWithdrawIds")
    public MSResponse<Map<Long, GoldenSettlements>> batchSearchSettlementsByWithdrawIds(@RequestBody List<Long> withdrawIds) {
        try {
            Map<Long,GoldenSettlements> settlementsMap =
                    settlementsService.batchSearchSettlementsByWithdrawIds(withdrawIds);
            return new MSResponse(MSErrorCode.SUCCESS,settlementsMap);
        }catch (Exception e){
            String gson = GsonUtils.getInstance().toGson(withdrawIds);
            log.error("批量查询付款单对应的结算单异常:{}",gson,e);
            sysLogService.insert(1L,gson, e.getMessage(),
                    "批量查询付款单对应的结算单异常","settlements/batchSearchSettlementsByWithdrawIds", "POST");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));
        }
    }

    @ApiOperation("结算单重发接口")
    @PostMapping("settlementsRetry")
    public MSResponse<Integer> settlementsRetry(@RequestBody GoldenSettlements settlements) {
        try {
            return settlementsService.settlementsRetry(settlements);
        }catch (Exception e){
            String gson = GsonUtils.getInstance().toGson(settlements);
            log.error("结算单重发异常:{}",gson,e);
            sysLogService.insert(1L,gson, e.getMessage(),
                    "结算单重发异常","settlements/settlementsRetry", "POST");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));
        }
    }


    @ApiOperation("手动退款")
    @PostMapping("manualRefund/{id}/{updateById}")
    public MSResponse<Integer> manualRefund(@PathVariable("id")Long id,@PathVariable("updateById")Long updateById) {
        try {

            GoldenSettlements goldenSettlements = settlementsService.selectById(id);
            if (goldenSettlements != null){
                RefundBalance refundBalance = new RefundBalance();
                List refund = new ArrayList();
                refund.add(goldenSettlements.getWithdrawNo());
                refundBalance.setOrderRandomCode(refund);
                String infoJson = gson.toJson(refundBalance);
                goldenSettlements.setSubStatus(SubStatusEnum.REFUNDING.getValue());
                goldenSettlements.setUpdateDt(System.currentTimeMillis());
                settlementsService.updateResult(goldenSettlements);
                GoldenProcessLog processlog = new GoldenProcessLog();
                processlog.setQuarter(goldenSettlements.getQuarter());
                processlog.setProcessTime(1);
                processlog.setProcessFlag(1);
                processlog.setCreateBy(1L);
                processlog.setCreateDate(System.currentTimeMillis());
                processlog.setUpdateDate(System.currentTimeMillis());
                processlog.setUpdateBy(updateById);
                processlog.setSettlementId(goldenSettlements.getId());
                processlog.setInfoJson(infoJson);
                processlog.setInterfaceName("/settlements/manualRefund");
                processlog.setWithdrawNo(goldenSettlements.getWithdrawNo());
                processlogService.insert(processlog);
                CommonParam commonParam = new CommonParam();
                commonParam.setRequestId(String.valueOf(processlog.getId()));
                commonParam.setCallBackUrl(goldenProperties.getCallbackUrl()+"api/callback/refund");
                commonParam.setTime(processlog.getCreateDate()/1000);
                //发送请求
                OperationCommand command = OperationCommand.newInstance(OperationCommand.OperationCode.REFUNDBALANCE, refundBalance);
                ResponseBody<ResponseBody> resBody = OkHttpUtils.postSyncGenericNew(command, ResponseBody.class, commonParam);
                ResponseBody data = resBody.getData();
                GoldenProcessLog goldenProcessLog = new GoldenProcessLog();
                goldenProcessLog.setId(processlog.getId());

                if (resBody.getCode() == 0 && data != null && data.getCode() == 0) {
                    goldenProcessLog.setResultJson(resBody.getOriginalJson());
                    goldenProcessLog.setProcessFlag(GoldenProcessFlag.PROCESS_FLAG_SUCCESS.value);
                    goldenProcessLog.setProcessComment(resBody.getMsg());
                    goldenProcessLog.setUpdateDate(System.currentTimeMillis());
                    processlogService.updateProcessFlag(goldenProcessLog);
                } else {
                    String errorMsg = resBody.getMsg();
                    if (data != null) {
                        errorMsg = data.getMsg();
                    }
                    errorMsg = StringUtils.left(errorMsg, 200);
                    goldenSettlements.setProcessComment(errorMsg);
                    goldenSettlements.setUpdateDt(System.currentTimeMillis());
                    goldenSettlements.setSubStatus(SubStatusEnum.REFUNDED.getValue());
                    settlementsService.updateResult(goldenSettlements);
                    goldenProcessLog.setProcessFlag(GoldenProcessFlag.PROCESS_FLAG_FAILURE.value);
                    goldenProcessLog.setProcessComment(errorMsg);
                    goldenProcessLog.setResultJson(resBody.getOriginalJson());
                    goldenProcessLog.setUpdateDate(System.currentTimeMillis());
                    processlogService.updateProcessFlag(goldenProcessLog);
                    return new MSResponse(new MSErrorCode(MSErrorCode.FAILURE.getCode(), StringUtils.left(errorMsg,200)));
                }
            }else{
                return new MSResponse(new MSErrorCode(1000, StringUtils.left("该结算单不存在",200)));
            }
            return new MSResponse(MSErrorCode.SUCCESS, "");
        }catch (Exception e){
            String gson1 = GsonUtils.getInstance().toGson(id);
            log.error("结算单退款异常:{}",gson1,e);
            sysLogService.insert(1L,gson1, e.getMessage(),
                    "结算单退款异常","settlements/manualRefund", "POST");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));
        }
    }


    @ApiOperation("结算单付款失败列表")
    @PostMapping("getPayFailList")
    public MSResponse<MSPage<GoldenSettlements>> getPayFailList(@RequestBody SettlementsSearchModel settlementsSearchModel) {
        try {
            Page<GoldenSettlements> goldenSettlementsPage = settlementsService.getPayFailList(settlementsSearchModel);
            MSPage<GoldenSettlements> returnPage = new MSPage<>();
            returnPage.setPageNo(goldenSettlementsPage.getPageNum());
            returnPage.setPageSize(goldenSettlementsPage.getPageSize());
            returnPage.setPageCount(goldenSettlementsPage.getPages());
            returnPage.setRowCount((int)goldenSettlementsPage.getTotal());
            returnPage.setList(goldenSettlementsPage);
            return new MSResponse<>(MSErrorCode.SUCCESS, returnPage);
        }catch (Exception e){
            log.error("查询结算单付款失败列表异常:{}",e);
            sysLogService.insert(1L,"", e.getMessage(),
                    "查询结算单付款失败列表异常","settlements/getPayFailList", "POST");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));
        }
    }

    @ApiOperation("结算单处理中的列表")
    @PostMapping("getPayProcessList")
    public MSResponse<MSPage<GoldenSettlements>> getPayProcessList(@RequestBody SettlementsSearchModel settlementsSearchModel) {
        try {
            Page<GoldenSettlements> goldenSettlementsPage = settlementsService.getProcessList(settlementsSearchModel);
            MSPage<GoldenSettlements> returnPage = new MSPage<>();
            returnPage.setPageNo(goldenSettlementsPage.getPageNum());
            returnPage.setPageSize(goldenSettlementsPage.getPageSize());
            returnPage.setPageCount(goldenSettlementsPage.getPages());
            returnPage.setRowCount((int)goldenSettlementsPage.getTotal());
            returnPage.setList(goldenSettlementsPage);
            return new MSResponse<>(MSErrorCode.SUCCESS, returnPage);
        }catch (Exception e){
            log.error("查询结算单处理中列表异常:{}",e);
            sysLogService.insert(1L,"", e.getMessage(),
                    "查询结算单处理中列表异常","settlements/getPayProcessList", "POST");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));
        }
    }

    @ApiOperation("结算单成功的列表")
    @PostMapping("getPaySuccessList")
    public MSResponse<MSPage<GoldenSettlements>> getPaySuccessList(@RequestBody SettlementsSearchModel settlementsSearchModel) {
        try {
            Page<GoldenSettlements> goldenSettlementsPage = settlementsService.getSuccessList(settlementsSearchModel);
            MSPage<GoldenSettlements> returnPage = new MSPage<>();
            returnPage.setPageNo(goldenSettlementsPage.getPageNum());
            returnPage.setPageSize(goldenSettlementsPage.getPageSize());
            returnPage.setPageCount(goldenSettlementsPage.getPages());
            returnPage.setRowCount((int)goldenSettlementsPage.getTotal());
            returnPage.setList(goldenSettlementsPage);
            return new MSResponse<>(MSErrorCode.SUCCESS, returnPage);
        }catch (Exception e){
            log.error("查询结算单处理中列表异常:{}",e);
            sysLogService.insert(1L,"", e.getMessage(),
                    "查询结算单处理中列表异常","settlements/getPaySuccessList", "POST");
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),200)));
        }
    }

}
