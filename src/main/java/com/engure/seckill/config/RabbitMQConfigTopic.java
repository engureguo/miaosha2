//package com.engure.seckill.config;
//
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.core.TopicExchange;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfigTopic {
//
//    public static final String TOPIC_QUEUE01 = "topic_queue01";
//    public static final String TOPIC_QUEUE02 = "topic_queue02";
//    public static final String TOPIC_EXCHANGE = "topic_exchange";
//    public static final String ROUTING_KEY01_QUEUE01 = "a.#";
//    public static final String ROUTING_KEY02_QUEUE02 = "#.b.#";
//    public static final String ROUTING_KEY03_QUEUE02 = "c.*";
//
//    @Bean
//    public Queue topic_queue01() {
//        return new Queue(TOPIC_QUEUE01);
//    }
//
//    @Bean
//    public Queue topic_queue02() {
//        return new Queue(TOPIC_QUEUE02);
//    }
//
//    @Bean
//    public TopicExchange topicExchange() {
//        return new TopicExchange(TOPIC_EXCHANGE);
//    }
//
//    /*
//
//    topic exchange
//        |---------------    a.#         ----------> queue01
//        |---------------  #.b.#, c.*    ----------> queue02
//
//     */
//
//    @Bean
//    public Binding topic_binding01() {
//        return BindingBuilder.bind(topic_queue01()).to(topicExchange()).with(ROUTING_KEY01_QUEUE01);
//    }
//
//    @Bean
//    public Binding topic_binding02() {
//        return BindingBuilder.bind(topic_queue02()).to(topicExchange()).with(ROUTING_KEY02_QUEUE02);
//    }
//
//    @Bean
//    public Binding topic_binding03() {
//        return BindingBuilder.bind(topic_queue02()).to(topicExchange()).with(ROUTING_KEY03_QUEUE02);
//    }
//
//}
