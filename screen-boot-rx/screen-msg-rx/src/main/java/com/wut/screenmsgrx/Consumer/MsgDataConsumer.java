package com.wut.screenmsgrx.Consumer;

import com.wut.screencommonrx.Util.MessagePrintUtil;
import com.wut.screenmsgrx.Service.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.wut.screencommonrx.Static.MsgModuleStatic.*;

@Component
public class MsgDataConsumer {
    private final PlateParseService plateParseService;
    private final FiberParseService fiberParseService;
    private final LaserParseService laserParseService;
    private final WaveParseService waveParseService;
    private final DateTimeParseService dateTimeParseService;

    @Autowired
    public MsgDataConsumer(PlateParseService plateParseService, FiberParseService fiberParseService, LaserParseService laserParseService, WaveParseService waveParseService, DateTimeParseService dateTimeParseService) {
        this.plateParseService = plateParseService;
        this.fiberParseService = fiberParseService;
        this.laserParseService = laserParseService;
        this.waveParseService = waveParseService;
        this.dateTimeParseService = dateTimeParseService;
    }

    @KafkaListener(topics = "plate", groupId = "group-plate")
    public void plateDataListener(List<ConsumerRecord> records, Acknowledgment ack) {
        for (ConsumerRecord record : records) {
            String plateDataStr = record.value().toString();
            MessagePrintUtil.printListenerReceive(TOPIC_NAME_PLATE, plateDataStr);
            plateParseService.collectPlateData(plateDataStr).thenRunAsync(() -> {});
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = "fiber", groupId = "group-fiber")
    public void fiberDataListener(List<ConsumerRecord> records, Acknowledgment ack) {
        for (ConsumerRecord record : records) {
            String fiberDataStr = record.value().toString();
            MessagePrintUtil.printListenerReceive(TOPIC_NAME_FIBER, fiberDataStr);
            fiberParseService.collectFiberData(fiberDataStr).thenRunAsync(() -> {});
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = "laser", groupId = "group-laser")
    public void laserDataListener(List<ConsumerRecord> records, Acknowledgment ack) {
        for (ConsumerRecord record : records) {
            String laserDataStr = record.value().toString();
            MessagePrintUtil.printListenerReceive(TOPIC_NAME_LASER, laserDataStr);
            laserParseService.collectLaserData(laserDataStr).thenRunAsync(() -> {});
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = "wave", groupId = "group-wave")
    public void waveDataListener(List<ConsumerRecord> records, Acknowledgment ack) {
        for (ConsumerRecord record : records) {
            String waveDataStr = record.value().toString();
            MessagePrintUtil.printListenerReceive(TOPIC_NAME_WAVE, waveDataStr);
            waveParseService.collectWaveData(waveDataStr).thenRunAsync(() -> {});
        }
        ack.acknowledge();
    }

    @KafkaListener(topics = "timestamp", groupId = "group-timestamp")
    public void timestampDataListener(List<ConsumerRecord> records, Acknowledgment ack) {
        for (ConsumerRecord record : records) {
            String timestampStr = record.value().toString();
            MessagePrintUtil.printListenerReceive(TOPIC_NAME_TIMESTAMP, timestampStr);
            dateTimeParseService.collectTimestampData(timestampStr);
        }
        ack.acknowledge();
    }

}
