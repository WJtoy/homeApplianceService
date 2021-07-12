package com.kkl.kklplus.golden.mq.sender;

import com.google.gson.Gson;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.golden.common.MQConstant;


import com.kkl.kklplus.entity.golden.mq.MQGoldenSettlementsStatusUpdateMessage;
import com.kkl.kklplus.golden.service.GoldenSysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoldenSettlementMessageSender implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private GoldenSysLogService goldenSysLogService;

    private RabbitTemplate rabbitTemplate;

    private RetryTemplate retryTemplate;


    @Autowired
    public GoldenSettlementMessageSender(RabbitTemplate kklRabbitTemplate, RetryTemplate kklRabbitRetryTemplate) {
        this.rabbitTemplate = kklRabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.retryTemplate = kklRabbitRetryTemplate;
    }

    /**
     * 正常发送消息
     */
    public void send(MQGoldenSettlementsStatusUpdateMessage.GoldenSettlementsStatusUpdateMessage message) {
       try{
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(MQConstant.MQ_GOLDEN_SETTLEMENTS_STATUS_UPDATE_DELAY, message);
                rabbitTemplate.convertAndSend(
                        MQConstant.MQ_GOLDEN_SETTLEMENTS_STATUS_UPDATE_DELAY,
                        MQConstant.MQ_GOLDEN_SETTLEMENTS_STATUS_UPDATE_DELAY,
                        message.toByteArray(),
                        messageProcessor -> {
                            messageProcessor.getMessageProperties().setDelay(0);
                            return messageProcessor;
                        },
                        new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(MQConstant.MQ_GOLDEN_SETTLEMENTS_STATUS_UPDATE_DELAY);
                MQGoldenSettlementsStatusUpdateMessage.GoldenSettlementsStatusUpdateMessage msg =
                        MQGoldenSettlementsStatusUpdateMessage.GoldenSettlementsStatusUpdateMessage.parseFrom((byte[])msgObj);
                Throwable throwable = context.getLastThrowable();
                String msgJson = new JsonFormat().printToString(msg);
                log.error("消息队列发送失败:{}", throwable.getLocalizedMessage(), msg);
                goldenSysLogService.insert(1L,new Gson().toJson(msg),
                        "消息队列发送失败：" + throwable.getLocalizedMessage() +"；错误原因：" + throwable.getCause().toString(),
                        "消息队列发送失败", MQConstant.MQ_GOLDEN_SETTLEMENTS_STATUS_UPDATE_DELAY, null);
                return null;
            });
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }
    }


    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {

    }
}
