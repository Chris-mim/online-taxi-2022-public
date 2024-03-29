package com.mashibing.serviceorder.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RedisConfig {

    @Value("${spring.redis.port}")
    private String redisPort;
    @Value("${spring.redis.host}")
    private String redisHost;
    private String prefix = "redis://";

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(prefix + redisHost + ":" + redisPort).setDatabase(0);

        return Redisson.create(config);

    }
}
