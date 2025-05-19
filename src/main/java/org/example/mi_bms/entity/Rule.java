package org.example.mi_bms.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 电池预警规则实体类
 */
@Data
public class Rule implements Serializable {
    
    private Integer id;
    private Integer ruleId;          // 规则ID
    private Integer ruleNumber;      // 规则编号
    private String name;             // 规则名称
    private String batteryType;      // 电池类型
    private String warningCondition; // 预警条件
    private Integer warningLevel;    // 预警等级
    private String expression;       // 条件表达式
    private Date createTime;         // 创建时间
    private Date updateTime;         // 更新时间
    private Integer warnId;          // 警告类型ID
    private String detail;           // 规则详情/条件表达式JSON
    
    // 无参构造函数
    public Rule() {
        this.createTime = new Date();
        this.updateTime = new Date();
    }
    
    // 简化构造函数 - 仅包含基本属性
    public Rule(String name, String batteryType, Integer warningLevel) {
        this.name = name;
        this.batteryType = batteryType;
        this.warningLevel = warningLevel;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
    
    // 部分属性构造函数
    public Rule(Integer ruleNumber, String name, String batteryType, Integer warningLevel) {
        this.ruleNumber = ruleNumber;
        this.name = name;
        this.batteryType = batteryType;
        this.warningLevel = warningLevel;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
    
    // 完整构造函数
    public Rule(Integer ruleId, Integer ruleNumber, String name, String batteryType, 
                String warningCondition, Integer warningLevel, String expression) {
        this.ruleId = ruleId;
        this.ruleNumber = ruleNumber;
        this.name = name;
        this.batteryType = batteryType;
        this.warningCondition = warningCondition;
        this.warningLevel = warningLevel;
        this.expression = expression;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
    
    // 不包含ID的构造函数（用于新增记录）
    public Rule(Integer ruleNumber, String name, String batteryType, 
                String warningCondition, Integer warningLevel, String expression) {
        this.ruleNumber = ruleNumber;
        this.name = name;
        this.batteryType = batteryType;
        this.warningCondition = warningCondition;
        this.warningLevel = warningLevel;
        this.expression = expression;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
    
    // Lombok generates getters and setters, but adding the specific ones that are referenced
    public Integer getWarnId() {
        return warnId;
    }
    
    public void setWarnId(Integer warnId) {
        this.warnId = warnId;
    }
    
    public String getDetail() {
        return detail;
    }
    
    public void setDetail(String detail) {
        this.detail = detail;
    }
}