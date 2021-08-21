package com.engure.seckill;

import com.engure.seckill.utils.UUIDUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SeckillApplicationTests {

    @Test
    void contextLoads() {

    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 使用 redis 实现分布式锁
     */
    @Test
    void distributedLockByRedis() {
        ValueOperations opsFV = redisTemplate.opsForValue();
        Boolean set = opsFV.setIfAbsent("name", "qwq");//获取锁
        if (set) {
            //业务
            redisTemplate.delete("name");//释放锁
        } else {
            System.out.println("获取锁失败，请重试~");
        }
    }

    @Test
    void distributedLockByRedis2() {
        ValueOperations opsFV = redisTemplate.opsForValue();
        Boolean set = opsFV.setIfAbsent("name", "qwq");//获取锁
        if (set) {
            try {
                Integer.parseInt("xxxx");
            } catch (Exception e) {
                //e.printStackTrace();
            } finally {
                redisTemplate.delete("name");//捕获异常，保证最终能释放锁
            }
        } else {
            System.out.println("获取锁失败，请重试~");
        }
    }

    @Test
    void distributedLockByRedis3() {
        ValueOperations opsFV = redisTemplate.opsForValue();
        Boolean set = opsFV.setIfAbsent("name", "qwq", 5, TimeUnit.SECONDS);//获取锁,并设置超时时间
        if (set) {
            try {
                Integer.parseInt("xxxx");
            } catch (Exception e) {
                //e.printStackTrace();
            } finally {
                redisTemplate.delete("name");//捕获异常，保证最终能释放锁
            }
        } else {
            System.out.println("获取锁失败，请重试~");
        }
    }

    @Test
    void distributedLockByRedis4() {
        ValueOperations opsFV = redisTemplate.opsForValue();
        String id = UUIDUtil.uuid();
        Boolean set = opsFV.setIfAbsent("name", id, 5, TimeUnit.SECONDS);//获取锁,并设置超时时间
        if (set) {
            try {
                Integer.parseInt("xxxx");
            } catch (Exception e) {
            } finally {
                String ID = (String) opsFV.get("name");
                if (ID != null && ID.equals(id))
                    redisTemplate.delete("name");//捕获异常，保证最终能释放锁
            }
        } else {
            System.out.println("获取锁失败，请重试~");
        }
    }

    @Autowired
    private DefaultRedisScript<Boolean> redisScript;

    @Test
    void distributedLockByRedis5() {
        ValueOperations opsFV = redisTemplate.opsForValue();
        String id = UUIDUtil.uuid();
        Boolean set = opsFV.setIfAbsent("name", id, 5, TimeUnit.SECONDS);//获取锁,并设置超时时间
        if (set) {
            try {
                Integer.parseInt("xxxx");
            } catch (Exception e) {
            } finally {
                // "name" 对应 KEYS[1]，id 对应 ARGV[1]
                // 如果”name“锁对应的 id 是本线程的 id ，则删除对应的键
                // 如果锁没有自动释放，则手动释放。如果已经释放则什么也不干
                redisTemplate.execute(redisScript, Collections.singletonList("name"), id);
            }
        } else {
            System.out.println("获取锁失败，请重试~");
        }
    }

}
