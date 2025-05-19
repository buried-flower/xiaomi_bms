package org.example.mi_bms.redis;

import com.alibaba.fastjson.JSONObject;
import org.example.mi_bms.entity.Battery;
import org.example.mi_bms.entity.Rule;
import org.example.mi_bms.entity.Vehicle;
import org.example.mi_bms.mapper.BatteryMapper;
import org.example.mi_bms.mapper.RuleMapper;
import org.example.mi_bms.mapper.VehicleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RedisDataInitializer implements ApplicationRunner {

    @Autowired
    private RuleMapper ruleMapper;
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private BatteryMapper batteryMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RedisDataInitializer(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //电池信息全部存储
        List<Battery> batteries = batteryMapper.selectAll();
        String batteryJson = JSONObject.toJSONString(batteries);
        redisTemplate.opsForValue().set("batteries", batteryJson);
        //汽车信息初始化过程中存入空值
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        String vehicleJson = JSONObject.toJSONString(vehicles);
        redisTemplate.opsForValue().set("vehicle", vehicleJson);
        //规则信息全部存储
        List<Rule> rules = ruleMapper.selectAll();
        String ruleJson = JSONObject.toJSONString(rules);
        redisTemplate.opsForValue().set("rules", ruleJson);

        System.out.println("Redis 数据初始化完成.");
    }
}
