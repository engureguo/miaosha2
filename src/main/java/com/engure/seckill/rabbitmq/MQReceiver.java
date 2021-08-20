package com.engure.seckill.rabbitmq;

import com.engure.seckill.config.RabbitMQConfigDirect;
import com.engure.seckill.config.RabbitMQConfigFanout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {

    /////////////  default exchange  //////////////////////

    @RabbitListener(queues = RabbitMQConfigFanout.DEFAULT_QUEUE)
    public void receive(Object msg) {
        log.info("接收消息：" + msg);
    }

    /////////////  fanout exchange  //////////////////////

    @RabbitListener(queues = RabbitMQConfigFanout.QUEUE01)
    public void receive01(Object msg) {
        log.info("接收消息：" + msg);
    }

    @RabbitListener(queues = RabbitMQConfigFanout.QUEUE02)
    public void receive02(Object msg) {
        log.info("接收消息：" + msg);
    }

    /////////////  direct exchange  //////////////////////

    @RabbitListener(queues = RabbitMQConfigDirect.DIRECT_QUEUE01)
    public void receive_direct_queue01(Object msg) {
        log.info("收到消息：" + msg);
    }

    @RabbitListener(queues = RabbitMQConfigDirect.DIRECT_QUEUE02)
    public void receive_direct_queue02(Object msg) {
        log.info("收到消息：" + msg);
    }

}
