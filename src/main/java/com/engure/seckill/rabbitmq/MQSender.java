package com.engure.seckill.rabbitmq;

import com.engure.seckill.config.RabbitMQConfigDirect;
import com.engure.seckill.config.RabbitMQConfigFanout;
import com.engure.seckill.config.RabbitMQConfigHeaders;
import com.engure.seckill.config.RabbitMQConfigTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /////////////  default exchange  //////////////////////

    /**
     * 使用队列实现简单的生产者消费者模式
     *
     * @param msg
     */
    public void send(Object msg) {
        log.info("发送消息：" + msg);
        rabbitTemplate.convertAndSend(RabbitMQConfigFanout.DEFAULT_QUEUE, msg);
    }

    /////////////  fanout exchange  //////////////////////

    /**
     * 验证 fanout 模式，给交换机发送消息
     *
     * @param msg
     */
    public void sendByFanout(Object msg) {
        log.info("发送消息" + msg);
        rabbitTemplate.convertAndSend(RabbitMQConfigFanout.FANOUT_EXCHANGE, "", msg);
    }

    /////////////  direct exchange  //////////////////////

    /**
     * 验证 direct 模式，给 bindingKey = A 类型的队列发送消息
     *
     * @param msg
     */
    public void sendByDirect_a(Object msg) {
        log.info("direct 发送" + RabbitMQConfigDirect.BINDING_KEY_TYPE_A + "消息：" + msg);
        // (direct交换机，bindingKey，obj_msg)
        rabbitTemplate.convertAndSend(RabbitMQConfigDirect.DIRECT_EXCHANGE,
                RabbitMQConfigDirect.BINDING_KEY_TYPE_A, msg);
    }

    /**
     * 验证 direct 模式，给 bindingKey = B 类型的队列发送消息
     *
     * @param msg
     */
    public void sendByDirect_b(Object msg) {
        log.info("direct 发送" + RabbitMQConfigDirect.BINDING_KEY_TYPE_B + "消息：" + msg);
        // (direct交换机，bindingKey，obj_msg)
        rabbitTemplate.convertAndSend(RabbitMQConfigDirect.DIRECT_EXCHANGE,
                RabbitMQConfigDirect.BINDING_KEY_TYPE_B, msg);
    }

    /////////////  topic exchange  //////////////////////

    public void sendByTopic01(Object msg) {
        log.info("发送消息:" + msg);
        rabbitTemplate.convertAndSend(RabbitMQConfigTopic.TOPIC_EXCHANGE, "a.b.c", msg);
    }

    public void sendByTopic02(Object msg) {
        log.info("发送消息:" + msg);
        rabbitTemplate.convertAndSend(RabbitMQConfigTopic.TOPIC_EXCHANGE, "e.f", msg);
    }

    public void sendByTopic03(Object msg) {
        log.info("发送消息:" + msg);
        rabbitTemplate.convertAndSend(RabbitMQConfigTopic.TOPIC_EXCHANGE, "c.b.a", msg);
    }

    /////////////  headers exchange  //////////////////////

    public void sendByHeaders01(String msg) {
        log.info("发送消息：" + msg);
        MessageProperties mp = new MessageProperties();
        mp.setHeader("color", "red");
        mp.setHeader("speed", "fast");
        Message message = new Message(msg.getBytes(StandardCharsets.UTF_8), mp);
        rabbitTemplate.convertAndSend(RabbitMQConfigHeaders.HEADERS_EXCHANGE, "", message);
    }

    public void sendByHeaders02(String msg) {
        log.info("发送消息：" + msg);
        MessageProperties mp = new MessageProperties();
        mp.setHeader("color", "red");
        mp.setHeader("speed", "normal");
        Message message = new Message(msg.getBytes(StandardCharsets.UTF_8), mp);
        rabbitTemplate.convertAndSend(RabbitMQConfigHeaders.HEADERS_EXCHANGE, "", message);
    }

}
