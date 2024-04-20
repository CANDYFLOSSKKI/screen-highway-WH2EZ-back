package com.wut.screenmsgtx.Context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.wut.screencommontx.Static.MsgModuleStatic.*;

@Component
public class MsgRedisDataContext {
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public MsgRedisDataContext(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void resetDataContext() {
        stringRedisTemplate.delete(REDIS_KEY_PLATE_DATA);
        stringRedisTemplate.delete(REDIS_KEY_FIBER_DATA);
        stringRedisTemplate.delete(REDIS_KEY_LASER_DATA);
        stringRedisTemplate.delete(REDIS_KEY_WAVE_DATA);
        stringRedisTemplate.opsForValue().set(REDIS_KEY_PLATE_OFFSET, "0");
        stringRedisTemplate.opsForValue().set(REDIS_KEY_FIBER_OFFSET, "0");
        stringRedisTemplate.opsForValue().set(REDIS_KEY_LASER_OFFSET, "0");
        stringRedisTemplate.opsForValue().set(REDIS_KEY_WAVE_OFFSET, "0");
        stringRedisTemplate.opsForValue().set(REDIS_KEY_FIND_TODAY, LocalDateTime.now().format(DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN)));
        stringRedisTemplate.opsForValue().set(REDIS_KEY_COLLECT_TIME,"0.0");
        stringRedisTemplate.opsForValue().set(REDIS_KEY_TRANSMIT_TIME, "0.0");
    }

    public boolean isDataListEmpty(String key) {
        return Boolean.FALSE.equals(stringRedisTemplate.hasKey(key)) || getDataListSize(key) == 0;
    }
    public int getDataListSize(String key) {
        return Objects.requireNonNull(stringRedisTemplate.opsForList().size(key)).intValue();
    }
    public double getTimestamp() {
        return Double.parseDouble(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(REDIS_KEY_TRANSMIT_TIME)));
    }
    public void setTimestamp(double timestamp) {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_TRANSMIT_TIME, Double.toString(timestamp));
    }
    public void updateTimestamp(double timestamp) {
        setTimestamp(getTimestamp() + timestamp);
    }
    public double getCollectTime() {
        return Double.parseDouble(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(REDIS_KEY_COLLECT_TIME)));
    }
    public void setCollectTime(double timestamp) {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_COLLECT_TIME, Double.toString(timestamp));
    }
    public void updateCollectTime(double timestamp) {
        setCollectTime(getCollectTime() + timestamp);
    }
    public String getFindToday() {
        return stringRedisTemplate.opsForValue().get(REDIS_KEY_FIND_TODAY);
    }
    public void setFindToday(String findToday) {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_FIND_TODAY,findToday);
    }
    public int getPlateOffset() {
        return Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(REDIS_KEY_PLATE_OFFSET)));
    }
    public int getFiberOffset() {
        return Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(REDIS_KEY_FIBER_OFFSET)));
    }
    public int getLaserOffset() {
        return Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(REDIS_KEY_LASER_OFFSET)));
    }
    public int getWaveOffset() {
        return Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(REDIS_KEY_WAVE_OFFSET)));
    }
    public void updatePlateOffset(int offset) {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_PLATE_OFFSET,Integer.toString(offset+getPlateOffset()));
    }
    public void updateFiberOffset(int offset) {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_FIBER_OFFSET,Integer.toString(offset+getFiberOffset()));
    }
    public void updateLaserOffset(int offset) {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_LASER_OFFSET,Integer.toString(offset+getLaserOffset()));
    }
    public void updateWaveOffset(int offset) {
        stringRedisTemplate.opsForValue().set(REDIS_KEY_WAVE_OFFSET,Integer.toString(offset+getWaveOffset()));
    }

}
