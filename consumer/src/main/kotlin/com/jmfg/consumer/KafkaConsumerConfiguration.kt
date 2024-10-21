package com.jmfg.consumer

import java.lang.Class
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class KafkaConsumerConfiguration {

    @Value("\${spring.kafka.consumer.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}") private lateinit var groupId: String

    @Value("\${spring.kafka.consumer.key-deserializer}") private lateinit var keySerializer: String

    @Value("\${spring.kafka.consumer.value-deserializer}")
    private lateinit var valueDeserializer: String

    // JSon trusteed packages
    @Value("\${spring.kafka.consumer.properties.spring.json.trusted.packages}")
    private lateinit var trustedPackages: String

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val props =
                mapOf(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                        ConsumerConfig.GROUP_ID_CONFIG to groupId,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to
                                Class.forName(keySerializer),
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to
                                Class.forName(valueDeserializer),
                        JsonDeserializer.TRUSTED_PACKAGES to trustedPackages
                )
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory()
        return factory
    }
}
