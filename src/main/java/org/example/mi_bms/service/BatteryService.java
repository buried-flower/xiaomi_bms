package org.example.mi_bms.service;

import org.example.mi_bms.entity.Battery;

import java.util.List;

/**
 * 电池服务接口
 */
public interface BatteryService {
    
    /**
     * 获取所有电池信息
     */
    List<Battery> getAllBatteries();
    
    /**
     * 根据电池类型获取电池信息
     */
    Battery getBatteryByType(Integer batteryType);
} 