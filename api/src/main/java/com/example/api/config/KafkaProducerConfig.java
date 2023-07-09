package com.example.api.config;


import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * 카프카 프로듀서 설정 클래스
 */
@Configuration
public class KafkaProducerConfig {


    // 스프링에서 제공하는 카프카프로듀서관련 인터페이스
    @Bean
    public ProducerFactory<String,String> producerFactory() {
        // 카프카 설정
        Map<String, Object> config = new HashMap<>();

        // 카프카 서버 설정 및 key  ,value 설정
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG , StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    // 위에 설정값을 토대로 카프카템플릿을 만들어준다.
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(){

        return new KafkaTemplate<>(producerFactory());
    }
}
