package org.example.mi_bms.controller;

import com.alibaba.fastjson.JSON;
import org.example.mi_bms.entity.Message;
import org.example.mi_bms.entity.WarnRequest;
import org.example.mi_bms.response.R;
import org.example.mi_bms.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 电池预警接口控制器
 */
@RestController
public class WarnApiController {

    @Resource
    private RuleService ruleService;
    
    /**
     * 上报接口 - 处理预警规则匹配
     */
    @PostMapping("/api/warn")
    public R warnReport(@RequestBody List<Message> messageList) {
        try {
            // 将消息列表转换为JSON字符串，传给规则服务处理
            String messagesJson = JSON.toJSONString(messageList);
            
            // 调用规则服务处理消息
            List<WarnRequest> warnResults = ruleService.handleRules(messagesJson);
            
            // 格式化返回结果
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (WarnRequest warnRequest : warnResults) {
                // 过滤掉匹配失败的结果
                if (!"匹配失败".equals(warnRequest.getWarnLevel()) && !"不报警".equals(warnRequest.getWarnLevel())) {
                    Map<String, Object> resultItem = new HashMap<>();
                    resultItem.put("车架编号", warnRequest.getCarId());
                    resultItem.put("电池类型", warnRequest.getBatteryType());
                    resultItem.put("warnName", warnRequest.getWarnName());
                    
                    // 将字符串类型的警告等级转换为整数
                    int warnLevelInt;
                    switch (warnRequest.getWarnLevel()) {
                        case "高":
                        case "严重":
                            warnLevelInt = 0;
                            break;
                        case "中":
                        case "警告":
                            warnLevelInt = 1;
                            break;
                        case "低":
                        case "提示":
                            warnLevelInt = 2;
                            break;
                        default:
                            try {
                                warnLevelInt = Integer.parseInt(warnRequest.getWarnLevel());
                            } catch (NumberFormatException e) {
                                warnLevelInt = 9; // 未知等级
                            }
                    }
                    
                    resultItem.put("warnLevel", warnLevelInt);
                    resultList.add(resultItem);
                }
            }
            
            return R.success(resultList);
        } catch (Exception e) {
            return R.error("处理预警数据失败: " + e.getMessage());
        }
    }
} 