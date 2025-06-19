package com.dishant.tasks.management.config;

import com.example.kafka.avro.TaskEventSchema;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema-registry-url:http://localhost:8081}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.consumer.group-id:task-group}")
    private String group_id;

    @PostConstruct
    public void logKafkaConfig() {
        log.info("KafkaConsumerConfig initialized with bootstrap servers: {}", bootstrapServers);
        log.info("Schema Registry URL: {}", schemaRegistryUrl);
    }

    @Bean
    public ConsumerFactory<String, TaskEventSchema> avroConsumerFactory() {
        log.info("Creating Kafka Avro Consumer Factory");

        try {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, group_id);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
            props.put("schema.registry.url", schemaRegistryUrl);
            props.put("specific.avro.reader", true);

            log.debug("Kafka Consumer properties: {}", props);
            return new DefaultKafkaConsumerFactory<>(props);

        } catch (Exception e) {
            log.error("Error creating Kafka Avro Consumer Factory: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TaskEventSchema> kafkaListenerContainerFactory() {
        log.info("Creating Kafka Listener Container Factory");
        ConcurrentKafkaListenerContainerFactory<String, TaskEventSchema> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(avroConsumerFactory());
        return factory;
    }
}
