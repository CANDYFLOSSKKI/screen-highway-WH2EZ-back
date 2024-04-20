package com.wut.screendbredisrx.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static com.wut.screencommonrx.Static.DbModuleStatic.REDIS_KEY_DATE_TIME;

@Component
public class RedisDateTimeService {
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisDateTimeService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    public void resetDateTime() {
        stringRedisTemplate.delete(REDIS_KEY_DATE_TIME);
    }

    public String getRecordDateTime() {
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(REDIS_KEY_DATE_TIME))) { return null; }
        return stringRedisTemplate.opsForValue().get(REDIS_KEY_DATE_TIME);
    }

    public void setRecordDateTime(String dateTimeStr) {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_DATE_TIME, dateTimeStr);
    }

}
