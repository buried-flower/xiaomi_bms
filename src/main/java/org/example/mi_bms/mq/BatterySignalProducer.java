package org.example.mi_bms.mq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.example.mi_bms.config.RocketMQConfig;
import org.example.mi_bms.entity.Message;
import org.example.mi_bms.entity.Vehicle;
import org.example.mi_bms.mapper.BatteryWarningMapper;
import org.example.mi_bms.mapper.VehicleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 电池信号生产者，定时扫描并发送电池信号到MQ
 * 只有在RocketMQ启用时才创建
 */
@Slf4j
@Component
@EnableScheduling
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = false)
public class BatterySignalProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    @Autowired
    private BatteryWarningMapper batteryWarningMapper;
    
    public BatterySignalProducer() {
        log.info("BatterySignalProducer已创建，开始监听电池信号");
    }
    
    /**
     * 每5秒执行一次，扫描电池信号数据并发送到消息队列
     * 生产环境可根据需求调整定时策略
     */
    @Scheduled(fixedRate = 5000)
    public void scanAndSendBatterySignals() {
        log.info("开始扫描电池信号数据...");
        try {
            // 从数据库获取电池信号数据
            List<Message> messages = getBatterySignalsFromDB();
            
            if (messages.isEmpty()) {
                log.info("未发现新的电池信号数据");
                return;
            }
            
            // 转换为JSON并发送到MQ
            String messageJson = JSON.toJSONString(messages);
            log.info("发送电池信号数据到MQ: {}", messageJson);
            
            // 发送消息到RocketMQ
            rocketMQTemplate.convertAndSend(RocketMQConfig.BATTERY_SIGNAL_TOPIC, messageJson);
            
            // 更新已处理的数据状态
            updateProcessedSignals(messages);
            
        } catch (Exception e) {
            log.error("扫描并发送电池信号失败", e);
        }
    }
    
    /**
     * 从数据库获取电池信号数据
     * 实际场景中应从真实数据源获取
     */
    private List<Message> getBatterySignalsFromDB() {
        List<Message> messages = new ArrayList<>();
        
        try {
            // 获取未处理的电池信号数据（现在从battery_warning表获取）
            List<Map<String, Object>> signalDataList = batteryWarningMapper.getUnprocessedBatterySignals();
            
            if (signalDataList == null || signalDataList.isEmpty()) {
                return messages;
            }
            
            // 将数据库结果转换为消息对象
            for (Map<String, Object> data : signalDataList) {
                Message message = new Message();
                
                // 设置信号ID
                Integer signalId = (Integer) data.get("id");
                message.setSignalId(signalId);
                
                // 设置车辆ID
                Integer carId = (Integer) data.get("car_id");
                message.setCarId(carId);
                
                // 设置警告类型
                Integer warnId = (Integer) data.get("warn_id");
                message.setWarnId(warnId);
                
                // 设置信号数据
                String signalData = (String) data.get("signal_data");
                message.setSignal(signalData);
                
                messages.add(message);
                
                log.debug("读取到电池信号: ID={}, 车辆ID={}, 警告类型={}, 信号数据={}", 
                        signalId, carId, warnId, signalData);
            }
            
            log.info("从数据库获取到{}条电池信号数据", messages.size());
            
        } catch (Exception e) {
            log.error("从数据库获取电池信号数据失败", e);
        }
        
        return messages;
    }
    
    /**
     * 更新已处理的数据状态
     */
    private void updateProcessedSignals(List<Message> messages) {
        if (messages.isEmpty()) {
            return;
        }
        
        try {
            List<Integer> signalIds = new ArrayList<>();
            for (Message message : messages) {
                if (message.getSignalId() != null) {
                    signalIds.add(message.getSignalId());
                }
            }
            
            if (!signalIds.isEmpty()) {
                batteryWarningMapper.updateSignalsProcessed(signalIds);
                log.info("已更新{}条信号数据状态为已处理", signalIds.size());
            }
        } catch (Exception e) {
            log.error("更新信号处理状态失败", e);
        }
    }
} 