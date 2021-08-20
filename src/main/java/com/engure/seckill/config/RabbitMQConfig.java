package com.engure.seckill.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue queue() {
        // durable=true 持久化，当 队列和消息 同时配置持久化时，可以持久化
        return new Queue("queue", true);
    }

}
