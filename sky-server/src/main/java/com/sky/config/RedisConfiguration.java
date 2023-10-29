package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
@Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
    log.info("开始创建redis模板对象...");
    RedisTemplate redisTemplate=new RedisTemplate();
    //设置redis的连接工厂对象
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    //设置redis key的序列号器 使值能与java中设置的值一致，防止乱码
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    return redisTemplate;
}



}
