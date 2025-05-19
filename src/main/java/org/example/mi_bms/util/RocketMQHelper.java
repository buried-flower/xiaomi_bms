package org.example.mi_bms.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ帮助类
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = false)
public class RocketMQHelper {

    @Value("${rocketmq.enabled:true}")
    private boolean rocketmqEnabled;

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;
    
    /**
     * 检查RocketMQ连接是否正常
     * @return 连接状态
     */
    public boolean checkConnection() {
        // 如果配置禁用了RocketMQ，直接返回false
        if (!rocketmqEnabled) {
            log.info("RocketMQ功能已禁用");
            return false;
        }
        
        // 如果RocketMQTemplate未注入，返回false
        if (rocketMQTemplate == null) {
            log.warn("RocketMQTemplate未注入，无法检查连接");
            return false;
        }
        
        try {
            // 获取Producer
            DefaultMQProducer producer = rocketMQTemplate.getProducer();
            log.info("RocketMQ连接参数: nameServer={}, producerGroup={}", 
                    producer.getNamesrvAddr(), producer.getProducerGroup());
            
            // 创建一个测试消息
            Message testMessage = new Message("TEST_TOPIC", "TEST_TAG", 
                    "Test message from BMS system".getBytes());
            
            // 测试发送消息
            SendResult result = producer.send(testMessage, 1000);
            log.info("RocketMQ测试消息发送结果: {}", result);
            
            return result.getSendStatus() == SendStatus.SEND_OK;
        } catch (Exception e) {
            log.error("RocketMQ连接测试失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 打印RocketMQ配置信息
     */
    public void printRocketMQConfig() {
        // 如果RocketMQTemplate未注入，直接返回
        if (rocketMQTemplate == null) {
            log.warn("RocketMQTemplate未注入，无法获取配置信息");
            return;
        }
        
        try {
            DefaultMQProducer producer = rocketMQTemplate.getProducer();
            log.info("========== RocketMQ Configuration ==========");
            log.info("NameServer Address: {}", producer.getNamesrvAddr());
            log.info("Producer Group: {}", producer.getProducerGroup());
            log.info("Send Timeout: {} ms", producer.getSendMsgTimeout());
            log.info("Retry Times: {}", producer.getRetryTimesWhenSendFailed());
            log.info("Max Message Size: {} KB", producer.getMaxMessageSize() / 1024);
            log.info("============================================");
        } catch (Exception e) {
            log.error("获取RocketMQ配置信息失败: {}", e.getMessage());
        }
    }
} 