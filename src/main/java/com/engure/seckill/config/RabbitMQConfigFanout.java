//package com.engure.seckill.config;
//
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.FanoutExchange;
//import org.springframework.amqp.core.Queue;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfigFanout {
//
//    /////////////  fanout exchange  //////////////////////
//
//    public static final String QUEUE01 = "fanout_queue01";
//    public static final String QUEUE02 = "fanout_queue02";
//    public static final String FANOUT_EXCHANGE = "fanout_exchange";
//
//    @Bean
//    public Queue fanout_queue01() {
//        return new Queue(QUEUE01, true);
//    }
//
//    @Bean
//    public Queue fanout_queue02() {
//        return new Queue(QUEUE02, true);
//    }
//
//    @Bean
//    public FanoutExchange fanoutExchange() {
//        return new FanoutExchange(FANOUT_EXCHANGE);
//    }
//
//    @Bean
//    public Binding fanout_binding01() {
//        return BindingBuilder.bind(fanout_queue01()).to(fanoutExchange());
//    }
//
//    @Bean
//    public Binding fanout_binding02() {
//        return BindingBuilder.bind(fanout_queue02()).to(fanoutExchange());
//    }
//
//
//
//    /////////////  default exchange  //////////////////////
//
//    public static final String DEFAULT_QUEUE = "queue";
//
//    @Bean
//    public Queue deault_queue() {
//        // durable=true 持久化，当 队列和消息 同时配置持久化时，可以持久化
//        return new Queue(DEFAULT_QUEUE, true);
//    }
//
//}
