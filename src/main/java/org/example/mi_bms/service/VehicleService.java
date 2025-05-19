package org.example.mi_bms.service;

import org.example.mi_bms.entity.Vehicle;

import java.util.List;

public interface VehicleService {
    /**
     * 上传车辆信息
     */
    String updateVehicle(Integer carid,Integer batteryType,Double totalDistance,Integer batteryHealth);
    
    /**
     * 获取所有车辆信息
     */
    List<Vehicle> getAllVehicles();
    
    /**
     * 根据车辆ID获取车辆信息
     */
    Vehicle getVehicleById(Integer vehicleId);
    
    /**
     * 根据电池类型查询车辆列表
     */
    List<Vehicle> getVehiclesByBatteryType(Integer batteryType);
}
