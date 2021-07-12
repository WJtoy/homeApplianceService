package com.kkl.kklplus.golden.mq.sender;

import com.google.gson.Gson;
import com.googlecode.protobuf.format.JsonFormat;
import com.kkl.kklplus.entity.golden.common.MQConstant;
import com.kkl.kklplus.entity.golden.mq.MQGoldenSettlementsMessage;
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
public class GoldenSettlementsCoreMessageSender implements RabbitTemplate.ConfirmCallback {

    private RabbitTemplate rabbitTemplate;

    private RetryTemplate retryTemplate;

    @Autowired
    private GoldenSysLogService sysLogService;

    @Autowired
    public GoldenSettlementsCoreMessageSender(RabbitTemplate kklRabbitTemplate, RetryTemplate kklRabbitRetryTemplate) {
        this.rabbitTemplate = kklRabbitTemplate;
        //this.rabbitTemplate.setConfirmCallback(this);
        this.retryTemplate = kklRabbitRetryTemplate;
    }

    /**
     * 正常发送消息
     */
    public void send(MQGoldenSettlementsMessage.GoldenSettlementsMessage message) {
        try{
            retryTemplate.execute((RetryCallback<Object, Exception>) context -> {
                context.setAttribute(MQConstant.MQ_GOLDEN_SETTLEMENTS_DELAY, message);
                rabbitTemplate.convertAndSend(
                        MQConstant.MQ_GOLDEN_SETTLEMENTS_DELAY,
                        MQConstant.MQ_GOLDEN_SETTLEMENTS_DELAY,
                        message.toByteArray(),
                        new CorrelationData());
                return null;
            }, context -> {
                Object msgObj = context.getAttribute(MQConstant.MQ_GOLDEN_SETTLEMENTS_DELAY);
                MQGoldenSettlementsMessage.GoldenSettlementsMessage msg =
                        MQGoldenSettlementsMessage.GoldenSettlementsMessage.parseFrom((byte[])msgObj);
                Throwable throwable = context.getLastThrowable();
                String msgJson = new JsonFormat().printToString(msg);
                log.error("消息队列发送失败:{}", throwable.getLocalizedMessage(), msg);
                sysLogService.insert(1L,new Gson().toJson(msg),
                        "消息队列发送失败：" + throwable.getLocalizedMessage() +"；错误原因：" + throwable.getCause().toString(),
                        "消息队列发送失败", MQConstant.MQ_GOLDEN_SETTLEMENTS_DELAY, null);
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
