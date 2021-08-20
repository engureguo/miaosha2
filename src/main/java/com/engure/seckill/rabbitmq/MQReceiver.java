package com.engure.seckill.rabbitmq;

import com.engure.seckill.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {

    @RabbitListener(queues = "queue")
    public void receive(Object msg) {
        log.info("接收消息：" + msg);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE01)
    public void receive01(Object msg) {
        log.info("接收消息：" + msg);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE02)
    public void receive02(Object msg) {
        log.info("接收消息：" + msg);
    }

}
