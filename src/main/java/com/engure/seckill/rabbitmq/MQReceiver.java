package com.engure.seckill.rabbitmq;

import com.engure.seckill.config.RabbitMQConfig;
import com.engure.seckill.pojo.User;
import com.engure.seckill.service.IGoodsService;
import com.engure.seckill.service.IOrderService;
import com.engure.seckill.utils.JsonUtil;
import com.engure.seckill.vo.GoodsVo;
import com.engure.seckill.vo.SeckillMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.Seckill_Queue)
    public void receiveSeckillMessage(String message) {
        log.info(message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        User user = seckillMessage.getUser();
        Long goodsId = seckillMessage.getGoodsId();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);

        // 库存的控制主要在 redis，实际上进行 mysql 中库存的更新
        if (goodsVo.getStockCount() < 1) {
            return;
        }

        ValueOperations opsFV = redisTemplate.opsForValue();
        //重复抢购
        Object orderInfo = opsFV.get("seckOrder:userId-" + user.getId()
                + ":goodsId-" + goodsId);
        if (null != orderInfo) {
            return;
        }

        orderService.seckill(user, goodsVo);//减秒杀商品库存、生成订单
    }


//    /////////////  default exchange  //////////////////////
//
//    @RabbitListener(queues = RabbitMQConfigFanout.DEFAULT_QUEUE)
//    public void receive(Object msg) {
//        log.info("接收消息：" + msg);
//    }
//
//    /////////////  fanout exchange  //////////////////////
//
//    @RabbitListener(queues = RabbitMQConfigFanout.QUEUE01)
//    public void receive01(Object msg) {
//        log.info("接收消息：" + msg);
//    }
//
//    @RabbitListener(queues = RabbitMQConfigFanout.QUEUE02)
//    public void receive02(Object msg) {
//        log.info("接收消息：" + msg);
//    }
//
//    /////////////  direct exchange  //////////////////////
//
//    @RabbitListener(queues = RabbitMQConfigDirect.DIRECT_QUEUE01)
//    public void receive_direct_queue01(Object msg) {
//        log.info("收到消息：" + msg);
//    }
//
//    @RabbitListener(queues = RabbitMQConfigDirect.DIRECT_QUEUE02)
//    public void receive_direct_queue02(Object msg) {
//        log.info("收到消息：" + msg);
//    }
//
//    /////////////  topic exchange  //////////////////////
//
//    @RabbitListener(queues = RabbitMQConfigTopic.TOPIC_QUEUE01)
//    public void receive_topic_queue01(Object msg) {
//        log.info("收到消息（订阅：" + RabbitMQConfigTopic.ROUTING_KEY01_QUEUE01 + "）：" + msg);
//    }
//
//    @RabbitListener(queues = RabbitMQConfigTopic.TOPIC_QUEUE02)
//    public void receive_topic_queue02(Object msg) {
//        log.info("收到消息（订阅：" + RabbitMQConfigTopic.ROUTING_KEY02_QUEUE02
//                + ", " + RabbitMQConfigTopic.ROUTING_KEY03_QUEUE02 + "）：" + msg);
//    }
//
//    /////////////  headers exchange  //////////////////////
//
//    @RabbitListener(queues = RabbitMQConfigHeaders.HEADERS_QUEUE01)
//    public void receive_headers01(Message msg) {
//        log.info("收到消息：" + new String(msg.getBody()) + ", " + msg);
//    }
//
//    @RabbitListener(queues = RabbitMQConfigHeaders.HEADERS_QUEUE02)
//    public void receive_headers02(Message msg) {
//        log.info("收到消息：" + new String(msg.getBody()) + ", " + msg);
//    }

}
