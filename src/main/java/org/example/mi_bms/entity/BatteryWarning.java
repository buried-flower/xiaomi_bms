package org.example.mi_bms.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 电池预警记录实体类
 */
@Data
public class BatteryWarning implements Serializable {
    
    private Integer id;
    private Integer carId;          // 车辆ID
    private Integer ruleId;         // 触发的规则ID
    private Integer warnId;         // 警告类型ID
    private String batteryType;     // 电池类型
    private String warnName;        // 预警名称
    private Integer warnLevel;      // 预警等级
    private String signalData;      // 原始信号数据
    private String rawSignalData;   // 原始信号数据（完整）
    private Date createTime;        // 创建时间
    private Integer processStatus;  // 处理状态: 0-未处理, 1-已处理, 2-已忽略
    private Integer processed;      // 是否已处理: 0-未处理, 1-已处理
    private Date processTime;       // 处理时间
    private String processUser;     // 处理人
    private String remark;          // 备注信息
    
    // 关联的规则对象（非数据库字段，用于展示）
    private transient Rule rule;
    
    // 无参构造函数
    public BatteryWarning() {
        this.createTime = new Date();
        this.processStatus = 0;
        this.processed = 0;
    }
    
    // 基本构造函数
    public BatteryWarning(Integer carId, Integer ruleId, String batteryType, String warnName, Integer warnLevel, String signalData) {
        this.carId = carId;
        this.ruleId = ruleId;
        this.batteryType = batteryType;
        this.warnName = warnName;
        this.warnLevel = warnLevel;
        this.signalData = signalData;
        this.rawSignalData = signalData;
        this.createTime = new Date();
        this.processStatus = 0;
        this.processed = 0;
    }
    
    // 完整构造函数
    public BatteryWarning(Integer carId, Integer ruleId, Integer warnId, String batteryType, 
                         String warnName, Integer warnLevel, String signalData, 
                         Integer processStatus, Date processTime, String processUser, String remark) {
        this.carId = carId;
        this.ruleId = ruleId;
        this.warnId = warnId;
        this.batteryType = batteryType;
        this.warnName = warnName;
        this.warnLevel = warnLevel;
        this.signalData = signalData;
        this.rawSignalData = signalData;
        this.createTime = new Date();
        this.processStatus = processStatus;
        this.processed = (processStatus != null && processStatus > 0) ? 1 : 0;
        this.processTime = processTime;
        this.processUser = processUser;
        this.remark = remark;
    }
}