package com.kkl.kklplus.golden.mq.receiver;

import com.kkl.kklplus.entity.golden.common.MQConstant;
import com.kkl.kklplus.entity.golden.entity.GoldenSettlements;
import com.kkl.kklplus.entity.golden.mq.MQGoldenSettlementsMessage;
import com.kkl.kklplus.golden.entity.GoldenSysLog;
import com.kkl.kklplus.golden.service.GoldenSettlementsService;
import com.kkl.kklplus.golden.service.GoldenSysLogService;
import com.kkl.kklplus.golden.utils.GsonUtils;
import com.kkl.kklplus.golden.utils.QuarterUtils;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Date;

@Slf4j
@Component
public class GoldenSettlementsMessageReceiver {

    @Autowired
    private GoldenSettlementsService goldenSettlementsService;

    @Autowired
    private GoldenSysLogService goldenSysLogService;


    @RabbitListener(queues = MQConstant.MQ_GOLDEN_SETTLEMENTS_DELAY)
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            MQGoldenSettlementsMessage.GoldenSettlementsMessage  goldenSettlementsMessage =
                    MQGoldenSettlementsMessage.GoldenSettlementsMessage.parseFrom(message.getBody());
            log.error("接收消息体:{}", goldenSettlementsMessage.toString());

            GoldenSettlements goldenSettlements = new GoldenSettlements();
            goldenSettlements.setWithdrawId(goldenSettlementsMessage.getWithdrawId());
            goldenSettlements.setWithdrawNo(goldenSettlementsMessage.getWithdrawNo());
            goldenSettlements.setBank(goldenSettlementsMessage.getBank());
            goldenSettlements.setBankName(goldenSettlementsMessage.getBankName());
            goldenSettlements.setBankOwner(goldenSettlementsMessage.getBankOwner());
            goldenSettlements.setBankOwnerIdno(goldenSettlementsMessage.getBankOwnerIdno());
            goldenSettlements.setBankOwnerPhone(goldenSettlementsMessage.getBankOwnerPhone());
            goldenSettlements.setBankNo(goldenSettlementsMessage.getBankNo());
            goldenSettlements.setCertificateType(goldenSettlementsMessage.getCertificateType());
            goldenSettlements.setPaymentAccount(goldenSettlementsMessage.getPaymentAccount());
            goldenSettlements.setQuarter(goldenSettlementsMessage.getQuarter());
            goldenSettlements.setDiscountFlag(goldenSettlementsMessage.getDiscountFlag());
            goldenSettlements.setInvoiceFlag(goldenSettlementsMessage.getInvoiceFlag());
            goldenSettlements.setPaymentType(goldenSettlementsMessage.getPaymentType());
            goldenSettlements.setPayAmount(goldenSettlementsMessage.getPayAmount());
            goldenSettlements.setServicePointId(goldenSettlementsMessage.getServicePointId());
            goldenSettlements.setAlipayAccountid(goldenSettlementsMessage.getAlipayAccountId());
            goldenSettlements.setWxAppid(goldenSettlementsMessage.getWxAppid());
            goldenSettlements.setWxOpenId(goldenSettlementsMessage.getWxOpenId());
            goldenSettlements.setApplyAmount(goldenSettlementsMessage.getApplyAmount());
            goldenSettlements.setPaymentWay(goldenSettlementsMessage.getPaymentWay());
            goldenSettlements.setCreateDt(goldenSettlementsMessage.getCreateDate());
            goldenSettlements.setCreateBy(goldenSettlementsMessage.getCreateBy());
          //  BeanUtils.copyProperties(goldenOrderCenterMessage,goldenSettlement);

            goldenSettlementsService.checkSettlements(goldenSettlements);

        }catch (Exception e) {
            String strMessage = GsonUtils.getInstance().toGson(message);
            log.error("接收退款信息{}"+strMessage);
            GoldenSysLog goldenSysLog = new GoldenSysLog();
            goldenSysLog.setParams(strMessage);
            goldenSysLog.setQuarter(QuarterUtils.getQuarter(new Date()));
            goldenSysLog.setRequestUri("balance/CreateForBatch");
            goldenSysLog.setCreateDate(System.currentTimeMillis());
            goldenSysLog.setMethod("POST");
            goldenSysLog.setException(e.getMessage());
            goldenSysLog.setType(1);
            goldenSysLog.setCreateBy(1L);
            goldenSysLogService.insertModel(goldenSysLog);
        }finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
