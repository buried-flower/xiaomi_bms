package org.example.mi_bms.config;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.annotation.ExtRocketMQTemplateConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = false)
public class RocketMQConfig {

    // 定义消息主题
    public static final String BATTERY_SIGNAL_TOPIC = "battery-signal-topic";
    public static final String BATTERY_WARNING_TOPIC = "battery-warning-topic";
    
    // 定义消费者组
    public static final String BATTERY_SIGNAL_CONSUMER_GROUP = "battery-signal-consumer-group";
    
    @Value("${rocketmq.name-server}")
    private String nameServer;
    
    @Value("${rocketmq.producer.group}")
    private String producerGroup;
    
    @Value("${rocketmq.producer.send-message-timeout:3000}")
    private int sendMessageTimeout;
    
    @Value("${rocketmq.producer.retry-times-when-send-failed:2}")
    private int retryTimesWhenSendFailed;
    
    @Value("${rocketmq.producer.retry-times-when-send-async-failed:2}")
    private int retryTimesWhenSendAsyncFailed;
    
    // 扩展RocketMQTemplate配置，可根据需要自定义更多设置
    @ExtRocketMQTemplateConfiguration
    public class ExtRocketMQTemplate extends RocketMQTemplate {
    }
    
    /**
     * 创建RocketMQTemplate
     */
    @Bean
    @Primary
    public RocketMQTemplate rocketMQTemplate(RocketMQMessageConverter rocketMQMessageConverter) {
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setProducerGroup(producerGroup);
        producer.setNamesrvAddr(nameServer);
        producer.setSendMsgTimeout(sendMessageTimeout);
        producer.setRetryTimesWhenSendFailed(retryTimesWhenSendFailed);
        producer.setRetryTimesWhenSendAsyncFailed(retryTimesWhenSendAsyncFailed);
        producer.setMaxMessageSize(4 * 1024 * 1024); // 设置最大消息大小为4MB
        
        rocketMQTemplate.setProducer(producer);
        rocketMQTemplate.setMessageConverter(rocketMQMessageConverter.getMessageConverter());
        
        return rocketMQTemplate;
    }

    // 仅保留一个创建DefaultMQProducer的Bean方法
    @Bean
    public DefaultMQProducer defaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setProducerGroup(producerGroup);
        producer.setNamesrvAddr(nameServer);
        producer.setSendMsgTimeout(3000);
        producer.setRetryTimesWhenSendFailed(2);
        producer.setRetryTimesWhenSendAsyncFailed(2);
        producer.setMaxMessageSize(4 * 1024 * 1024);
        return producer;
    }
} 