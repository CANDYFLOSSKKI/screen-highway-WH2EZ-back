package com.wut.screenmsgtx.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.wut.screencommontx.Static.MsgModuleStatic.*;

@Configuration
public class MsgTopicConfig {
    @Bean("topicPlate")
    public NewTopic topicPlate() {
        return TopicBuilder.name(TOPIC_NAME_PLATE).partitions(TOPIC_PARTITION).replicas(TOPIC_REPLICA).build();
    }

    @Bean("topicFiber")
    public NewTopic topicFiber() {
        return TopicBuilder.name(TOPIC_NAME_FIBER).partitions(TOPIC_PARTITION).replicas(TOPIC_REPLICA).build();
    }

    @Bean("topicLaser")
    public NewTopic topicLaser() {
        return TopicBuilder.name(TOPIC_NAME_LASER).partitions(TOPIC_PARTITION).replicas(TOPIC_REPLICA).build();
    }

    @Bean("topicWave")
    public NewTopic topicWave() {
        return TopicBuilder.name(TOPIC_NAME_WAVE).partitions(TOPIC_PARTITION).replicas(TOPIC_REPLICA).build();
    }

    @Bean("topicTimestamp")
    public NewTopic topicTimestamp() {
        return TopicBuilder.name(TOPIC_NAME_TIMESTAMP).partitions(TOPIC_PARTITION).replicas(TOPIC_REPLICA).build();
    }

}
