package com.engure.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

//@Configuration
public class RabbitMQConfigHeaders {

    public static final String HEADERS_QUEUE01 = "headers_queue01";
    public static final String HEADERS_QUEUE02 = "headers_queue02";
    public static final String HEADERS_EXCHANGE = "headers_exchange";

    @Bean
    public Queue headers_queue01() {
        return new Queue(HEADERS_QUEUE01);
    }

    @Bean
    public Queue headers_queue02() {
        return new Queue(HEADERS_QUEUE02);
    }

    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(HEADERS_EXCHANGE);
    }

    /**
     * 给 queue1 进行条件绑定
     *
     * @return
     */
    @Bean
    public Binding Binding_headers_for_queue01() {
        Map<String, Object> headerValues = new HashMap<>();
        headerValues.put("color", "red");
        headerValues.put("speed", "slow");
        return BindingBuilder.bind(headers_queue01()).to(headersExchange()).whereAny(headerValues).match();
    }

    @Bean
    public Binding Binding_headers_for_queue02() {
        Map<String, Object> headerValues = new HashMap<>();
        headerValues.put("color", "red");
        headerValues.put("speed", "fast");
        return BindingBuilder.bind(headers_queue02()).to(headersExchange()).whereAll(headerValues).match();
    }

}
