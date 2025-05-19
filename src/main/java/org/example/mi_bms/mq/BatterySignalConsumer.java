package org.example.mi_bms.mq;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.example.mi_bms.config.RocketMQConfig;
import org.example.mi_bms.entity.BatteryWarning;
import org.example.mi_bms.entity.WarnRequest;
import org.example.mi_bms.mapper.BatteryWarningMapper;
import org.example.mi_bms.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 电池信号消费者，处理电池信号并生成预警
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = false)
@RocketMQMessageListener(
    topic = RocketMQConfig.BATTERY_SIGNAL_TOPIC,
    consumerGroup = RocketMQConfig.BATTERY_SIGNAL_CONSUMER_GROUP
)
public class BatterySignalConsumer implements RocketMQListener<String> {

    @Qualifier("RuleService")
    @Autowired
    private RuleService ruleService;
    
    @Autowired
    private BatteryWarningMapper batteryWarningMapper;
    
    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;
    
    public BatterySignalConsumer() {
        log.info("BatterySignalConsumer已创建，准备接收电池信号");
    }

    @Override
    public void onMessage(String message) {
        try {
            log.info("接收到电池信号数据: {}", message);
            
            // 调用规则服务处理信号数据
            List<WarnRequest> warnRequests = ruleService.handleRules(message);
            
            // 处理预警结果
            handleWarningResults(warnRequests, message);
            
        } catch (Exception e) {
            log.error("处理电池信号数据失败", e);
        }
    }
    
    /**
     * 处理预警结果
     * 1. 保存到数据库
     * 2. 发送到预警主题以供其他系统消费
     */
    private void handleWarningResults(List<WarnRequest> warnRequests, String originalSignal) {
        if (warnRequests == null || warnRequests.isEmpty()) {
            log.info("没有生成预警信息");
            return;
        }
        
        for (WarnRequest request : warnRequests) {
            // 如果预警级别不是"不报警"或"匹配失败"，则保存预警记录
            if (!"不报警".equals(request.getWarnLevel()) && !"匹配失败".equals(request.getWarnLevel())) {
                try {
                    // 获取警告等级（转换为整数）
                    int warnLevel = getWarnLevelAsInt(request.getWarnLevel());
                    
                    // 创建预警记录
                    BatteryWarning warning = new BatteryWarning();
                    warning.setCarId(request.getCarId());
                    warning.setRuleId(request.getRuleId() != null ? request.getRuleId() : 0);
                    warning.setWarnId(request.getWarnId());
                    warning.setBatteryType(request.getBatteryType());
                    warning.setWarnName(request.getWarnName());
                    warning.setWarnLevel(warnLevel);
                    warning.setSignalData(originalSignal);
                    warning.setRawSignalData(originalSignal); // 保存原始信号数据
                    warning.setCreateTime(new Date());
                    warning.setProcessStatus(0); // 未处理
                    warning.setProcessed(0); // 未处理
                    
                    // 保存到数据库
                    batteryWarningMapper.insert(warning);
                    
                    // 发送到预警主题
                    if (rocketMQTemplate != null) {
                        rocketMQTemplate.convertAndSend(
                            RocketMQConfig.BATTERY_WARNING_TOPIC, 
                            JSON.toJSONString(warning)
                        );
                    }
                    
                    log.info("生成预警信息: 车辆ID={}, 电池类型={}, 预警名称={}, 预警等级={}",
                        request.getCarId(), request.getBatteryType(), request.getWarnName(), request.getWarnLevel());
                } catch (Exception e) {
                    log.error("保存预警记录失败: {}", e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * 将警告等级的文字描述转换为整数
     */
    private int getWarnLevelAsInt(String warnLevel) {
        switch (warnLevel) {
            case "高":
            case "严重":
                return 0;
            case "中":
            case "警告":
                return 1;
            case "低":
            case "提示":
                return 2;
            default:
                try {
                    return Integer.parseInt(warnLevel);
                } catch (NumberFormatException e) {
                    return 9; // 未知等级
                }
        }
    }
} 