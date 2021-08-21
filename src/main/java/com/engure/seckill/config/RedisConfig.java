package com.engure.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        //序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        //hash类型序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        redisTemplate.setConnectionFactory(connectionFactory);//连接工厂

        return redisTemplate;
    }

    /**
     * redis 执行 lua 脚本
     *
     * @return
     */
    @Bean
    public DefaultRedisScript<Long> redisScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        //位置：与 application.yaml 同级目录
        redisScript.setLocation(new ClassPathResource("stock.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }


}
