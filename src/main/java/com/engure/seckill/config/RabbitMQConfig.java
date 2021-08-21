package com.engure.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String Seckill_Queue = "seckill_queue";
    public static final String Seckill_Exchange = "seckill_exchange";

    @Bean
    public Queue seckillQueue() {
        return new Queue(Seckill_Queue);
    }

    @Bean
    public TopicExchange seckillExchange() {
        return new TopicExchange(Seckill_Exchange);
    }

    @Bean
    public Binding binding_routing_key() {
        return BindingBuilder.bind(seckillQueue()).to(seckillExchange()).with("seckill.#");
    }

}
