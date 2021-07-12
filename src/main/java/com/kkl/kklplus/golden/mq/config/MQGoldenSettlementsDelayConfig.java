package com.kkl.kklplus.golden.mq.config;


import com.kkl.kklplus.entity.golden.common.MQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * mq发送成功消息
 * @author chenxj
 * @date 2020/05/29
 */
@EnableRabbit
@Configuration
public class MQGoldenSettlementsDelayConfig {

    @Bean
    public Queue goldenSettlementsDelayQueue() {
        return new Queue(MQConstant.MQ_GOLDEN_SETTLEMENTS_DELAY, true);
    }

    @Bean
    DirectExchange goldenSettlementsDelayExchange() {
        return new DirectExchange(MQConstant.MQ_GOLDEN_SETTLEMENTS_DELAY);
    }

    @Bean
    Binding bindingGoldenSettlementsDelayMessage(Queue goldenSettlementsDelayQueue, DirectExchange goldenSettlementsDelayExchange) {
        return BindingBuilder.bind(goldenSettlementsDelayQueue)
                .to(goldenSettlementsDelayExchange)
                .with(MQConstant.MQ_GOLDEN_SETTLEMENTS_DELAY);
    }

}
