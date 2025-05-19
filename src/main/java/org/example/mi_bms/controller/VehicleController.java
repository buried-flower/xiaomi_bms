package org.example.mi_bms.controller;


import org.example.mi_bms.entity.Vehicle;
import org.example.mi_bms.response.R;
import org.example.mi_bms.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicle")
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    /**
     * 获取所有车辆信息
     */
    @GetMapping
    public R getAllVehicles() {
        try {
            List<Vehicle> vehicles = vehicleService.getAllVehicles();
            return R.success(vehicles);
        } catch (Exception e) {
            return R.error("获取车辆信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据车辆ID获取车辆信息
     */
    @GetMapping("/{vehicleId}")
    public R getVehicleById(@PathVariable Integer vehicleId) {
        try {
            Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
            if (vehicle == null) {
                return R.error("找不到指定ID的车辆");
            }
            return R.success(vehicle);
        } catch (Exception e) {
            return R.error("获取车辆信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据电池类型查询车辆列表
     */
    @GetMapping("/battery/{batteryType}")
    public R getVehiclesByBatteryType(@PathVariable Integer batteryType) {
        try {
            List<Vehicle> vehicles = vehicleService.getVehiclesByBatteryType(batteryType);
            return R.success(vehicles);
        } catch (Exception e) {
            return R.error("获取车辆信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    public R<String> upadteVehicle(@RequestParam Integer carid,
                           @RequestParam Integer batteryType,
                           @RequestParam Double totalDistance,
                           @RequestParam Integer batteryHealth ) throws Exception{
        String result;
        try{
            result = vehicleService.updateVehicle(carid, batteryType, totalDistance, batteryHealth);
            return R.success(result);
        }catch (Exception e){
            return R.error(e.getMessage());
        }
    }
}
