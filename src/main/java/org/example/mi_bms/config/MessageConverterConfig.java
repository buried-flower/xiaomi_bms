package org.example.mi_bms.config;

import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息转换器配置类
 */
@Configuration
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = false)
public class MessageConverterConfig {

    /**
     * 创建RocketMQ消息转换器
     */
    @Bean
    public RocketMQMessageConverter rocketMQMessageConverter() {
        // 在RocketMQ 2.3.3版本中，RocketMQMessageConverter不接受参数
        // 直接使用无参构造函数
        return new RocketMQMessageConverter();
    }
} 