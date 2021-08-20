package com.engure.seckill.rabbitmq;

import com.engure.seckill.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 使用队列实现简单的生产者消费者模式
     *
     * @param msg
     */
    public void send(Object msg) {
        log.info("发送消息：" + msg);
        rabbitTemplate.convertAndSend("queue", msg);
    }

    /**
     * 验证 fanout 模式，给交换机发送消息
     *
     * @param msg
     */
    public void sendByFanout(Object msg) {
        log.info("发送消息" + msg);
        rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE, "", msg);
    }

}
