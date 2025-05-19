package org.example.mi_bms.service.Impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.example.mi_bms.entity.*;
import org.example.mi_bms.mapper.BatteryMapper;
import org.example.mi_bms.mapper.RuleMapper;
import org.example.mi_bms.mapper.VehicleMapper;
import org.example.mi_bms.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
@Service("RuleService")
public class RuleServiceImpl implements RuleService {
    @Autowired
    private RuleMapper ruleMapper;
    @Autowired
    private VehicleMapper vehicleMapper;
    @Autowired
    private BatteryMapper batteryMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @Override
    public List<WarnRequest> handleRules(String warnMessage) throws Exception {
        log.info("开始处理告警规则: {}", warnMessage);
        
        //json解析
        List<Message> MessageList = JSONObject.parseArray(warnMessage, Message.class);
        List<WarnRequest> WarnRequestList = new ArrayList<>();
        
        for (Message message : MessageList) {
            // 首先尝试从Redis获取车辆信息
            Object vehicleObjFromRedis = redisTemplate.opsForValue().get("vehicle");
            List<Vehicle> tempVehicles = new ArrayList<>();
            
            // 处理Redis数据为空的情况
            if (vehicleObjFromRedis != null) {
                String tempVehicleJson = vehicleObjFromRedis.toString();
                try {
                    tempVehicles = JSONObject.parseArray(tempVehicleJson, Vehicle.class);
                } catch (Exception e) {
                    log.error("解析Redis中的vehicle数据失败: {}", e.getMessage());
                    // Redis中数据格式错误，从数据库重新获取
                    tempVehicles = vehicleMapper.selectAll();
                    if (tempVehicles != null && !tempVehicles.isEmpty()) {
                        // 更新Redis
                        redisTemplate.opsForValue().set("vehicle", JSONObject.toJSONString(tempVehicles));
                    }
                }
            } else {
                // Redis中没有数据，从数据库获取所有车辆信息
                log.info("Redis中没有vehicle数据，从数据库获取");
                tempVehicles = vehicleMapper.selectAll();
                if (tempVehicles != null && !tempVehicles.isEmpty()) {
                    // 更新Redis
                    redisTemplate.opsForValue().set("vehicle", JSONObject.toJSONString(tempVehicles));
                }
            }
            
            // 在内存中查找当前消息对应的车辆
            boolean vehicleFound = false;
            if (tempVehicles != null) {
                for (Vehicle v : tempVehicles) {
                    if (v.getCarid() == message.getCarId()) {
                        // 在Redis或内存中找到汽车
                        log.info("找到车辆信息: carId={}, batteryType={}", v.getCarid(), v.getBatteryType());
                        Battery battery = selectBatteryByType(v.getBatteryType());
                        List<Rule> ruleList = selectRules(v.getBatteryType(), message.getWarnId());
                        for (Rule rule : ruleList) {
                            WarnRequest warnRequest = ruleCalculate(rule, message.getSignal());
                            warnRequest.setBatteryType(battery != null ? battery.getName() : "未知电池");
                            warnRequest.setCarId(message.getCarId());
                            WarnRequestList.add(warnRequest);
                        }
                        vehicleFound = true;
                        break;
                    }
                }
            }
            
            // 如果内存中没有找到车辆，直接从数据库获取
            if (!vehicleFound) {
                log.info("内存中没有找到车辆ID={}的信息，从数据库查询", message.getCarId());
                Vehicle vehicle = vehicleMapper.selectByCarId(message.getCarId());
                if (vehicle != null) {
                    // 添加信息到内存中的列表和Redis
                    if (tempVehicles == null) {
                        tempVehicles = new ArrayList<>();
                    }
                    tempVehicles.add(vehicle);
                    redisTemplate.opsForValue().set("vehicle", JSONObject.toJSONString(tempVehicles));
                    
                    // 处理规则
                    Battery battery = selectBatteryByType(vehicle.getBatteryType());
                    List<Rule> ruleList = selectRules(vehicle.getBatteryType(), message.getWarnId());
                    for (Rule rule : ruleList) {
                        WarnRequest warnRequest = ruleCalculate(rule, message.getSignal());
                        warnRequest.setBatteryType(battery != null ? battery.getName() : "未知电池");
                        warnRequest.setCarId(message.getCarId());
                        WarnRequestList.add(warnRequest);
                    }
                } else {
                    log.warn("未找到车辆ID={}的信息", message.getCarId());
                }
            }
        }
        
        log.info("告警规则处理完成，生成{}条告警结果", WarnRequestList.size());
        return WarnRequestList;
    }



    /**
     * redis中找不到信息之后，添加新信息到redis中
     */
    public void addVehicle(List<Vehicle> vehicles, Vehicle vehicle) {
        if (vehicles == null) {
            vehicles = new ArrayList<>();
        }
        vehicles.add(vehicle);
        String vehicleJson = JSONObject.toJSONString(vehicles);
        redisTemplate.opsForValue().set("vehicle", vehicleJson);
    }

    /**
     * 根据电池类型找到电池信息
     * @return Battery
     */
    public Battery selectBatteryByType(int batteryType) {
        // 尝试从Redis获取电池数据
        Object batteryObjFromRedis = redisTemplate.opsForValue().get("batteries");
        
        List<Battery> tempBatteries = new ArrayList<>();
        if (batteryObjFromRedis != null) {
            try {
                String tempBatteryJson = batteryObjFromRedis.toString();
                tempBatteries = JSONObject.parseArray(tempBatteryJson, Battery.class);
            } catch (Exception e) {
                log.error("解析Redis中的batteries数据失败: {}", e.getMessage());
                // 从数据库获取
                tempBatteries = batteryMapper.selectAll();
                if (tempBatteries != null && !tempBatteries.isEmpty()) {
                    redisTemplate.opsForValue().set("batteries", JSONObject.toJSONString(tempBatteries));
                }
            }
        } else {
            // Redis中没有数据，从数据库获取
            log.info("Redis中没有batteries数据，从数据库获取");
            tempBatteries = batteryMapper.selectAll();
            if (tempBatteries != null && !tempBatteries.isEmpty()) {
                redisTemplate.opsForValue().set("batteries", JSONObject.toJSONString(tempBatteries));
            }
        }
        
        // 查找指定类型的电池
        if (tempBatteries != null) {
            for (Battery battery : tempBatteries) {
                if (battery.getBatteryType() == batteryType) {
                    return battery;
                }
            }
        }
        
        // 如果在Redis或内存中找不到，直接从数据库查询
        return batteryMapper.selectByType(batteryType);
    }

    /**
     * 根据电池类型和告警类型找到规则信息
     * @return List<Rule>
     */
    public List<Rule> selectRules(Integer batteryType, Integer warnId) {
        // 尝试从Redis获取规则数据
        Object ruleObjFromRedis = redisTemplate.opsForValue().get("rules");
        
        List<Rule> tempRules = new ArrayList<>();
        if (ruleObjFromRedis != null) {
            try {
                String tempRuleJson = ruleObjFromRedis.toString();
                tempRules = JSONObject.parseArray(tempRuleJson, Rule.class);
            } catch (Exception e) {
                log.error("解析Redis中的rules数据失败: {}", e.getMessage());
                // 从数据库获取
                tempRules = ruleMapper.findAll();
                if (tempRules != null && !tempRules.isEmpty()) {
                    redisTemplate.opsForValue().set("rules", JSONObject.toJSONString(tempRules));
                }
            }
        } else {
            // Redis中没有数据，从数据库获取
            log.info("Redis中没有rules数据，从数据库获取");
            tempRules = ruleMapper.findAll();
            if (tempRules != null && !tempRules.isEmpty()) {
                redisTemplate.opsForValue().set("rules", JSONObject.toJSONString(tempRules));
            }
        }
        
        // 查找符合条件的规则
        List<Rule> rules = new ArrayList<>();
        if (tempRules != null) {
            for (Rule rule : tempRules) {
                if (rule.getBatteryType().equals(batteryType)) {
                    if (rule.getWarnId().equals(warnId)) {
                        rules.add(rule);
                        return rules;  // 找到精确匹配的规则直接返回
                    } else if (warnId.equals(0)) {
                        rules.add(rule);
                    }
                }
            }
        }
        
        // 如果在Redis或内存中找不到，直接从数据库查询
        if (rules.isEmpty()) {
            List<Rule> dbRules = ruleMapper.findByBatteryType(batteryType.toString());
            if (dbRules != null && !dbRules.isEmpty()) {
                return dbRules;
            }
        }
        
        return rules;
    }


    /**
     *规则计算
     * @return WarnRequest
     */
    public WarnRequest ruleCalculate(Rule rule, String signal) throws Exception {
        String tSignal = signal.substring(1, signal.length() - 1);
        String[] signals = tSignal.split(",");
        List<Detail> details = JSON.parseArray(rule.getDetail(), Detail.class);

        //对每个规则细则进行匹配
        for (Detail detail : details) {
            //设定表达式
            String expression = detail.getExpression();
            String tExpression = expression;
            //将所有值带入表达式中进行计算
            for (String s : signals)
            {
                String left = s.split(":")[0];
                String right = s.split(":")[1];
                left = left.replace("\"", "");
                right = right.replace("\"", "");
                tExpression = tExpression.replace(left, right);
            }//字符串没有变化
            if(tExpression.equals(expression))
            {
                WarnRequest warnRequest = new WarnRequest();
                warnRequest.setWarnName(rule.getName());
                warnRequest.setWarnLevel("匹配失败");
                warnRequest.setWarnId(rule.getWarnId());
                warnRequest.setRuleId(rule.getId());
                return warnRequest;
            }else {

                ScriptEngineManager objManager = new ScriptEngineManager();
                ScriptEngine objEngine = objManager.getEngineByName("js");
                boolean bFlag = (boolean) objEngine.eval(tExpression);
                if (bFlag) {
                    WarnRequest warnRequest = new WarnRequest();
                    warnRequest.setWarnName(rule.getName());
                    warnRequest.setWarnLevel(detail.getLevel());
                    warnRequest.setWarnId(rule.getWarnId());
                    warnRequest.setRuleId(rule.getId());
                    return warnRequest;
                }
            }
        }

        WarnRequest warnRequest = new WarnRequest();
        warnRequest.setWarnName(rule.getName());
        warnRequest.setWarnLevel("匹配失败");
        warnRequest.setWarnId(rule.getWarnId());
        warnRequest.setRuleId(rule.getId());
        return warnRequest;
    }

}



