package org.example.mi_bms.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.mi_bms.entity.Vehicle;
import org.example.mi_bms.mapper.VehicleMapper;
import org.example.mi_bms.service.VehicleService;
import org.example.mi_bms.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("vehicleService")
@Slf4j
public class VehicleServiceImpl implements VehicleService {
    @Autowired
    private VehicleMapper vehicleMapper;
    
    @Autowired
    private UUID uuid;
    
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public String updateVehicle(Integer carid, Integer batteryType, Double totalDistance, Integer batteryHealth) {

        Vehicle vehicle = new Vehicle();
        vehicle.setVid(uuid.generateUUID());
        // carId 设置自增，利于之后的查询
//        vehicle.setCarid(carid);
        vehicle.setBatteryType(batteryType);
        vehicle.setTotalDistance(totalDistance);
        vehicle.setBatteryHealth(batteryHealth);
        vehicle.setDelete(0);
        vehicle.setCreateTime(new Date());
        vehicle.setUpdateTime(new Date());
        vehicleMapper.insert(vehicle);
        
        // 更新缓存
        refreshVehicleCache();
        
        return "上传成功";
    }
    
    @Override
    public List<Vehicle> getAllVehicles() {
        // 首先尝试从Redis缓存中获取
        String cachedVehicles = (String) redisTemplate.opsForValue().get("vehicle");
        if (cachedVehicles != null) {
            log.info("从缓存获取车辆信息");
            return JSONObject.parseArray(cachedVehicles, Vehicle.class);
        }
        
        // 缓存中没有，从数据库获取
        log.info("从数据库获取车辆信息");
        List<Vehicle> vehicles = vehicleMapper.selectAll();
        
        // 保存到缓存
        if (vehicles != null && !vehicles.isEmpty()) {
            redisTemplate.opsForValue().set("vehicle", JSON.toJSONString(vehicles));
        }
        
        return vehicles;
    }
    
    @Override
    public Vehicle getVehicleById(Integer vehicleId) {
        // 首先尝试从Redis缓存中获取所有车辆
        List<Vehicle> vehicles = getAllVehicles();
        
        // 在缓存的列表中查找
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getCarid().equals(vehicleId)) {
                return vehicle;
            }
        }
        
        // 缓存中没有找到，直接从数据库查询
        return vehicleMapper.selectByCarId(vehicleId);
    }
    
    @Override
    public List<Vehicle> getVehiclesByBatteryType(Integer batteryType) {
        // 首先尝试从Redis缓存中获取所有车辆
        List<Vehicle> allVehicles = getAllVehicles();
        List<Vehicle> filteredVehicles = new ArrayList<>();
        
        // 在缓存的列表中过滤
        for (Vehicle vehicle : allVehicles) {
            if (vehicle.getBatteryType().equals(batteryType)) {
                filteredVehicles.add(vehicle);
            }
        }
        
        // 如果缓存中没有找到，从数据库查询
        if (filteredVehicles.isEmpty()) {
            return vehicleMapper.selectByBatteryType(batteryType);
        }
        
        return filteredVehicles;
    }
    
    /**
     * 刷新车辆缓存
     */
    private void refreshVehicleCache() {
        List<Vehicle> vehicles = vehicleMapper.selectAll();
        if (vehicles != null && !vehicles.isEmpty()) {
            redisTemplate.opsForValue().set("vehicle", JSON.toJSONString(vehicles));
            log.info("已刷新车辆缓存");
        }
    }
}
