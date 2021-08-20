package com.engure.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class RabbitMQConfigDirect {

    public static final String DIRECT_QUEUE01 = "direct_queue01";
    public static final String DIRECT_QUEUE02 = "direct_queue02";
    public static final String DIRECT_EXCHANGE = "direct_exchange";
    public static final String BINDING_KEY_TYPE_A = "binding_key_type_A";
    public static final String BINDING_KEY_TYPE_B = "binding_key_type_B";

    @Bean
    public Queue direct_queue01() {
        return new Queue(DIRECT_QUEUE01, true);
    }

    @Bean
    public Queue direct_queue02() {
        return new Queue(DIRECT_QUEUE02, true);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    /*
                          direct exchange
    queue01 <---  A,B  ----------|
    queue02 <---   A   ----------

     */

    @Bean
    public Binding binding_queue01_01() {
        return BindingBuilder.bind(direct_queue01()).to(directExchange()).with(BINDING_KEY_TYPE_A);
    }

    @Bean
    public Binding binding_queue01_02() {
        return BindingBuilder.bind(direct_queue01()).to(directExchange()).with(BINDING_KEY_TYPE_B);
    }

    @Bean
    public Binding binding_queue02() {
        return BindingBuilder.bind(direct_queue02()).to(directExchange()).with(BINDING_KEY_TYPE_A);
    }

}
