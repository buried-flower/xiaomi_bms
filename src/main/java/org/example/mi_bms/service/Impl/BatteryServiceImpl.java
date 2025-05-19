package org.example.mi_bms.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.mi_bms.entity.Battery;
import org.example.mi_bms.mapper.BatteryMapper;
import org.example.mi_bms.service.BatteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 电池服务实现类
 */
@Service
@Slf4j
public class BatteryServiceImpl implements BatteryService {

    @Autowired
    private BatteryMapper batteryMapper;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Override
    public List<Battery> getAllBatteries() {
        // 首先尝试从Redis缓存中获取
        String cachedBatteries = (String) redisTemplate.opsForValue().get("batteries");
        if (cachedBatteries != null) {
            log.info("从缓存获取电池信息");
            return JSONObject.parseArray(cachedBatteries, Battery.class);
        }
        
        // 缓存中没有，从数据库获取
        log.info("从数据库获取电池信息");
        List<Battery> batteries = batteryMapper.selectAll();
        
        // 保存到缓存
        if (batteries != null && !batteries.isEmpty()) {
            redisTemplate.opsForValue().set("batteries", JSON.toJSONString(batteries));
        }
        
        return batteries;
    }
    
    @Override
    public Battery getBatteryByType(Integer batteryType) {
        // 首先尝试从Redis缓存中获取所有电池
        List<Battery> batteries = getAllBatteries();
        
        // 在缓存的列表中查找
        for (Battery battery : batteries) {
            if (battery.getBatteryType().equals(batteryType)) {
                return battery;
            }
        }
        
        // 缓存中没有找到，直接从数据库查询
        return batteryMapper.selectByType(batteryType);
    }
} 