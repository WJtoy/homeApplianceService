package com.kkl.kklplus.golden.mq.config;

import com.kkl.kklplus.entity.golden.common.MQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class MQGoldenSettlementsStatusUpdateDelayConfig {

    @Bean
    public Queue goldenSettlementsStatusUpdateDelayQueue() {
        return new Queue(MQConstant.MQ_GOLDEN_SETTLEMENTS_STATUS_UPDATE_DELAY, true);
    }

    @Bean
    DirectExchange goldenSettlementsStatusUpdateDelayExchange() {
        return (DirectExchange) ExchangeBuilder.directExchange(MQConstant.MQ_GOLDEN_SETTLEMENTS_STATUS_UPDATE_DELAY)
                .delayed().withArgument("x-delayed-type", "direct").build();
    }

    @Bean
    Binding bindingGoldenSettlementsStatusUpdateDelayMessage(Queue goldenSettlementsStatusUpdateDelayQueue, DirectExchange goldenSettlementsStatusUpdateDelayExchange) {
        return BindingBuilder.bind(goldenSettlementsStatusUpdateDelayQueue)
                .to(goldenSettlementsStatusUpdateDelayExchange)
                .with(MQConstant.MQ_GOLDEN_SETTLEMENTS_STATUS_UPDATE_DELAY);
    }

}
