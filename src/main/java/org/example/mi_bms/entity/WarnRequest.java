package org.example.mi_bms.entity;

public class WarnRequest {
    private Integer carId;
    private String batteryType;
    private String warnName;
    private String warnLevel;
    private Integer warnId;     // 警告类型ID
    private Integer ruleId;     // 触发的规则ID

    // 无参构造函数
    public WarnRequest() {
    }
    
    // 基本构造函数
    public WarnRequest(Integer carId, String batteryType, String warnName, String warnLevel) {
        this.carId = carId;
        this.batteryType = batteryType;
        this.warnName = warnName;
        this.warnLevel = warnLevel;
    }
    
    // 完整构造函数
    public WarnRequest(Integer carId, String batteryType, String warnName, String warnLevel, Integer warnId, Integer ruleId) {
        this.carId = carId;
        this.batteryType = batteryType;
        this.warnName = warnName;
        this.warnLevel = warnLevel;
        this.warnId = warnId;
        this.ruleId = ruleId;
    }

    public Integer getCarId() {
        return carId;
    }

    public void setCarId(Integer carId) {
        this.carId = carId;
    }

    public String getBatteryType() {
        return batteryType;
    }

    public void setBatteryType(String batteryType) {
        this.batteryType = batteryType;
    }

    public String getWarnName() {
        return warnName;
    }

    public void setWarnName(String warnName) {
        this.warnName = warnName;
    }

    public String getWarnLevel() {
        return warnLevel;
    }

    public void setWarnLevel(String warnLevel) {
        this.warnLevel = warnLevel;
    }
    
    public Integer getWarnId() {
        return warnId;
    }

    public void setWarnId(Integer warnId) {
        this.warnId = warnId;
    }
    
    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }
}
