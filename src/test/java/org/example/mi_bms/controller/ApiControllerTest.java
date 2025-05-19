package org.example.mi_bms.controller;

import com.alibaba.fastjson.JSON;
import org.example.mi_bms.entity.Message;
import org.example.mi_bms.entity.Vehicle;
import org.example.mi_bms.entity.WarnRequest;
import org.example.mi_bms.response.R;
import org.example.mi_bms.service.RuleService;
import org.example.mi_bms.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * API控制器单元测试类
 * 包含所有控制器的单元测试
 */
public class ApiControllerTest {

    /**
     * 预警API控制器测试
     */
    @Nested
    @DisplayName("预警API控制器测试")
    class WarnApiControllerTests {
        @Mock
        private RuleService ruleService;

        @InjectMocks
        private WarnApiController warnApiController;

        private MockMvc mockMvc;

        @BeforeEach
        public void setup() {
            MockitoAnnotations.openMocks(this);
            mockMvc = MockMvcBuilders.standaloneSetup(warnApiController).build();
        }

        /**
         * 测试成功处理预警规则并返回结果
         */
        @Test
        @DisplayName("测试成功处理预警规则")
        public void testWarnReportSuccess() throws Exception {
            // 准备测试数据
            List<Message> messageList = new ArrayList<>();
            Message message1 = new Message();
            message1.setCarId(1);
            message1.setWarnId(1);
            message1.setSignal("{\"Mx\":\"12.0\",\"Mi\":\"8.6\"}");
            messageList.add(message1);

            // 准备模拟返回结果
            List<WarnRequest> warnResults = new ArrayList<>();
            WarnRequest warnRequest1 = new WarnRequest();
            warnRequest1.setCarId(1);
            warnRequest1.setBatteryType("三元电池");
            warnRequest1.setWarnName("电压差报警");
            warnRequest1.setWarnLevel("高");
            warnResults.add(warnRequest1);

            // 模拟RuleService的行为
            when(ruleService.handleRules(anyString())).thenReturn(warnResults);

            // 调用控制器方法
            R result = warnApiController.warnReport(messageList);

            // 验证结果
            assertEquals(200, result.getCode());
            assertEquals("ok", result.getMsg());
            assertTrue(result.getData() instanceof List);
            
            List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.getData();
            assertEquals(1, resultList.size());
            Map<String, Object> firstItem = resultList.get(0);
            assertEquals(1, firstItem.get("车架编号"));
            assertEquals("三元电池", firstItem.get("电池类型"));
            assertEquals("电压差报警", firstItem.get("warnName"));
            assertEquals(0, firstItem.get("warnLevel")); // 验证"高"级别被转换为0
            
            // 验证服务方法被调用
            verify(ruleService, times(1)).handleRules(anyString());
        }

        /**
         * 测试所有警告级别转换
         */
        @Test
        @DisplayName("测试警告级别转换")
        public void testWarnLevelConversion() throws Exception {
            // 准备测试数据 - 包含不同警告级别的请求
            List<Message> messageList = new ArrayList<>();
            Message message = new Message();
            message.setCarId(1);
            message.setWarnId(1);
            message.setSignal("{\"Mx\":\"12.0\",\"Mi\":\"8.6\"}");
            messageList.add(message);

            // 准备模拟返回结果 - 中级警告
            List<WarnRequest> warnResults = new ArrayList<>();
            
            // 测试"中"级别
            WarnRequest warnRequest1 = new WarnRequest();
            warnRequest1.setCarId(1);
            warnRequest1.setBatteryType("三元电池");
            warnRequest1.setWarnName("电压差报警");
            warnRequest1.setWarnLevel("中");
            warnResults.add(warnRequest1);
            
            // 测试"警告"级别
            WarnRequest warnRequest2 = new WarnRequest();
            warnRequest2.setCarId(2);
            warnRequest2.setBatteryType("铁锂电池");
            warnRequest2.setWarnName("电流差报警");
            warnRequest2.setWarnLevel("警告");
            warnResults.add(warnRequest2);
            
            // 测试"低"级别
            WarnRequest warnRequest3 = new WarnRequest();
            warnRequest3.setCarId(3);
            warnRequest3.setBatteryType("三元电池");
            warnRequest3.setWarnName("温度差报警");
            warnRequest3.setWarnLevel("低");
            warnResults.add(warnRequest3);
            
            // 测试"提示"级别
            WarnRequest warnRequest4 = new WarnRequest();
            warnRequest4.setCarId(4);
            warnRequest4.setBatteryType("铁锂电池");
            warnRequest4.setWarnName("SOC差报警");
            warnRequest4.setWarnLevel("提示");
            warnResults.add(warnRequest4);
            
            // 测试"严重"级别
            WarnRequest warnRequest5 = new WarnRequest();
            warnRequest5.setCarId(5);
            warnRequest5.setBatteryType("铁锂电池");
            warnRequest5.setWarnName("压差报警");
            warnRequest5.setWarnLevel("严重");
            warnResults.add(warnRequest5);
            
            // 测试数字级别
            WarnRequest warnRequest6 = new WarnRequest();
            warnRequest6.setCarId(6);
            warnRequest6.setBatteryType("铁锂电池");
            warnRequest6.setWarnName("压差报警");
            warnRequest6.setWarnLevel("3");
            warnResults.add(warnRequest6);
            
            // 测试无法解析的级别
            WarnRequest warnRequest7 = new WarnRequest();
            warnRequest7.setCarId(7);
            warnRequest7.setBatteryType("铁锂电池");
            warnRequest7.setWarnName("压差报警");
            warnRequest7.setWarnLevel("未知级别");
            warnResults.add(warnRequest7);

            // 模拟RuleService的行为
            when(ruleService.handleRules(anyString())).thenReturn(warnResults);

            // 调用控制器方法
            R result = warnApiController.warnReport(messageList);

            // 验证结果
            assertEquals(200, result.getCode());
            List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.getData();
            assertEquals(7, resultList.size());
            
            // 验证各级别转换结果
            assertEquals(1, resultList.get(0).get("warnLevel")); // "中" -> 1
            assertEquals(1, resultList.get(1).get("warnLevel")); // "警告" -> 1
            assertEquals(2, resultList.get(2).get("warnLevel")); // "低" -> 2
            assertEquals(2, resultList.get(3).get("warnLevel")); // "提示" -> 2
            assertEquals(0, resultList.get(4).get("warnLevel")); // "严重" -> 0
            assertEquals(3, resultList.get(5).get("warnLevel")); // "3" -> 3
            assertEquals(9, resultList.get(6).get("warnLevel")); // "未知级别" -> 9
        }
        
        /**
         * 测试过滤"匹配失败"和"不报警"的结果
         */
        @Test
        @DisplayName("测试结果过滤")
        public void testFilterResults() throws Exception {
            // 准备测试数据
            List<Message> messageList = new ArrayList<>();
            Message message = new Message();
            message.setCarId(1);
            message.setWarnId(1);
            message.setSignal("{\"Mx\":\"12.0\",\"Mi\":\"8.6\"}");
            messageList.add(message);

            // 准备模拟返回结果 - 包含应该被过滤的结果
            List<WarnRequest> warnResults = new ArrayList<>();
            
            // 正常报警结果
            WarnRequest warnRequest1 = new WarnRequest();
            warnRequest1.setCarId(1);
            warnRequest1.setBatteryType("三元电池");
            warnRequest1.setWarnName("电压差报警");
            warnRequest1.setWarnLevel("高");
            warnResults.add(warnRequest1);
            
            // 匹配失败的结果 - 应被过滤
            WarnRequest warnRequest2 = new WarnRequest();
            warnRequest2.setCarId(2);
            warnRequest2.setBatteryType("铁锂电池");
            warnRequest2.setWarnName("电流差报警");
            warnRequest2.setWarnLevel("匹配失败");
            warnResults.add(warnRequest2);
            
            // 不报警的结果 - 应被过滤
            WarnRequest warnRequest3 = new WarnRequest();
            warnRequest3.setCarId(3);
            warnRequest3.setBatteryType("三元电池");
            warnRequest3.setWarnName("温度差报警");
            warnRequest3.setWarnLevel("不报警");
            warnResults.add(warnRequest3);

            // 模拟RuleService的行为
            when(ruleService.handleRules(anyString())).thenReturn(warnResults);

            // 调用控制器方法
            R result = warnApiController.warnReport(messageList);

            // 验证结果
            assertEquals(200, result.getCode());
            List<Map<String, Object>> resultList = (List<Map<String, Object>>) result.getData();
            
            // 应该只有一个结果被保留（其他两个被过滤掉）
            assertEquals(1, resultList.size());
            assertEquals("电压差报警", resultList.get(0).get("warnName"));
            assertEquals(0, resultList.get(0).get("warnLevel"));
        }

        /**
         * 测试处理过程中发生异常
         */
        @Test
        @DisplayName("测试处理异常情况")
        public void testWarnReportWithException() throws Exception {
            // 准备测试数据
            List<Message> messageList = new ArrayList<>();
            Message message = new Message();
            message.setCarId(1);
            message.setWarnId(1);
            message.setSignal("{\"Mx\":\"12.0\",\"Mi\":\"8.6\"}");
            messageList.add(message);

            // 模拟RuleService抛出异常
            String errorMessage = "处理规则时发生错误";
            when(ruleService.handleRules(anyString())).thenThrow(new RuntimeException(errorMessage));

            // 调用控制器方法
            R result = warnApiController.warnReport(messageList);

            // 验证结果为错误响应
            assertEquals(10000, result.getCode()); // 预期是错误码10000
            assertTrue(result.getMsg().contains(errorMessage));
            assertNull(result.getData());
            
            // 验证服务方法被调用
            verify(ruleService, times(1)).handleRules(anyString());
        }
    }
    
    /**
     * 车辆控制器测试
     */
    @Nested
    @DisplayName("车辆控制器测试")
    class VehicleControllerTests {
        @Mock
        private VehicleService vehicleService;

        @InjectMocks
        private VehicleController vehicleController;

        private MockMvc mockMvc;

        @BeforeEach
        public void setup() {
            MockitoAnnotations.openMocks(this);
            mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();
        }

        /**
         * 测试获取所有车辆信息
         */
        @Test
        @DisplayName("测试成功获取所有车辆信息")
        public void testGetAllVehiclesSuccess() {
            // 准备测试数据
            List<Vehicle> vehicleList = new ArrayList<>();
            
            Vehicle vehicle1 = new Vehicle();
            vehicle1.setVid("V001");
            vehicle1.setCarid(1001);
            vehicle1.setBatteryType(1);
            vehicle1.setTotalDistance(10000.0);
            vehicle1.setBatteryHealth(95);
            vehicle1.setCreateTime(new Date());
            vehicle1.setUpdateTime(new Date());
            vehicleList.add(vehicle1);
            
            Vehicle vehicle2 = new Vehicle();
            vehicle2.setVid("V002");
            vehicle2.setCarid(1002);
            vehicle2.setBatteryType(2);
            vehicle2.setTotalDistance(15000.0);
            vehicle2.setBatteryHealth(90);
            vehicle2.setCreateTime(new Date());
            vehicle2.setUpdateTime(new Date());
            vehicleList.add(vehicle2);

            // 模拟服务层行为
            when(vehicleService.getAllVehicles()).thenReturn(vehicleList);

            // 调用控制器方法
            R result = vehicleController.getAllVehicles();

            // 验证结果
            assertEquals(200, result.getCode());
            assertEquals("ok", result.getMsg());
            assertEquals(vehicleList, result.getData());
            
            // 验证服务方法被调用
            verify(vehicleService, times(1)).getAllVehicles();
        }

        /**
         * 测试获取所有车辆信息时发生异常
         */
        @Test
        @DisplayName("测试获取所有车辆信息时发生异常")
        public void testGetAllVehiclesException() {
            // 模拟服务层抛出异常
            String errorMessage = "数据库连接失败";
            when(vehicleService.getAllVehicles()).thenThrow(new RuntimeException(errorMessage));

            // 调用控制器方法
            R result = vehicleController.getAllVehicles();

            // 验证结果
            assertEquals(10000, result.getCode());
            assertTrue(result.getMsg().contains(errorMessage));
            assertNull(result.getData());
            
            // 验证服务方法被调用
            verify(vehicleService, times(1)).getAllVehicles();
        }

        /**
         * 测试根据车辆ID获取车辆信息
         */
        @Test
        @DisplayName("测试成功根据ID获取车辆信息")
        public void testGetVehicleByIdSuccess() {
            // 准备测试数据
            Integer vehicleId = 1001;
            Vehicle vehicle = new Vehicle();
            vehicle.setVid("V001");
            vehicle.setCarid(vehicleId);
            vehicle.setBatteryType(1);
            vehicle.setTotalDistance(10000.0);
            vehicle.setBatteryHealth(95);
            vehicle.setCreateTime(new Date());
            vehicle.setUpdateTime(new Date());

            // 模拟服务层行为
            when(vehicleService.getVehicleById(vehicleId)).thenReturn(vehicle);

            // 调用控制器方法
            R result = vehicleController.getVehicleById(vehicleId);

            // 验证结果
            assertEquals(200, result.getCode());
            assertEquals("ok", result.getMsg());
            assertEquals(vehicle, result.getData());
            
            // 验证服务方法被调用
            verify(vehicleService, times(1)).getVehicleById(vehicleId);
        }

        /**
         * 测试根据不存在的车辆ID获取车辆信息
         */
        @Test
        @DisplayName("测试根据不存在的ID获取车辆信息")
        public void testGetVehicleByIdNotFound() {
            // 准备测试数据
            Integer vehicleId = 9999;

            // 模拟服务层行为 - 返回空表示未找到
            when(vehicleService.getVehicleById(vehicleId)).thenReturn(null);

            // 调用控制器方法
            R result = vehicleController.getVehicleById(vehicleId);

            // 验证结果
            assertEquals(10000, result.getCode());
            assertEquals("找不到指定ID的车辆", result.getMsg());
            assertNull(result.getData());
            
            // 验证服务方法被调用
            verify(vehicleService, times(1)).getVehicleById(vehicleId);
        }

        /**
         * 测试根据车辆ID获取车辆信息时发生异常
         */
        @Test
        @DisplayName("测试根据ID获取车辆信息时发生异常")
        public void testGetVehicleByIdException() {
            // 准备测试数据
            Integer vehicleId = 1001;
            
            // 模拟服务层抛出异常
            String errorMessage = "查询数据库失败";
            when(vehicleService.getVehicleById(vehicleId)).thenThrow(new RuntimeException(errorMessage));

            // 调用控制器方法
            R result = vehicleController.getVehicleById(vehicleId);

            // 验证结果
            assertEquals(10000, result.getCode());
            assertTrue(result.getMsg().contains(errorMessage));
            assertNull(result.getData());
            
            // 验证服务方法被调用
            verify(vehicleService, times(1)).getVehicleById(vehicleId);
        }

        /**
         * 测试根据电池类型获取车辆列表
         */
        @Test
        @DisplayName("测试成功根据电池类型获取车辆列表")
        public void testGetVehiclesByBatteryTypeSuccess() {
            // 准备测试数据
            Integer batteryType = 1;
            List<Vehicle> vehicleList = new ArrayList<>();
            
            Vehicle vehicle1 = new Vehicle();
            vehicle1.setVid("V001");
            vehicle1.setCarid(1001);
            vehicle1.setBatteryType(batteryType);
            vehicle1.setTotalDistance(10000.0);
            vehicle1.setBatteryHealth(95);
            vehicleList.add(vehicle1);
            
            Vehicle vehicle2 = new Vehicle();
            vehicle2.setVid("V003");
            vehicle2.setCarid(1003);
            vehicle2.setBatteryType(batteryType);
            vehicle2.setTotalDistance(12000.0);
            vehicle2.setBatteryHealth(93);
            vehicleList.add(vehicle2);

            // 模拟服务层行为
            when(vehicleService.getVehiclesByBatteryType(batteryType)).thenReturn(vehicleList);

            // 调用控制器方法
            R result = vehicleController.getVehiclesByBatteryType(batteryType);

            // 验证结果
            assertEquals(200, result.getCode());
            assertEquals("ok", result.getMsg());
            assertEquals(vehicleList, result.getData());
            
            // 验证服务方法被调用
            verify(vehicleService, times(1)).getVehiclesByBatteryType(batteryType);
        }

        /**
         * 测试根据电池类型获取车辆列表时发生异常
         */
        @Test
        @DisplayName("测试根据电池类型获取车辆列表时发生异常")
        public void testGetVehiclesByBatteryTypeException() {
            // 准备测试数据
            Integer batteryType = 1;
            
            // 模拟服务层抛出异常
            String errorMessage = "数据访问失败";
            when(vehicleService.getVehiclesByBatteryType(batteryType)).thenThrow(new RuntimeException(errorMessage));

            // 调用控制器方法
            R result = vehicleController.getVehiclesByBatteryType(batteryType);

            // 验证结果
            assertEquals(10000, result.getCode());
            assertTrue(result.getMsg().contains(errorMessage));
            assertNull(result.getData());
            
            // 验证服务方法被调用
            verify(vehicleService, times(1)).getVehiclesByBatteryType(batteryType);
        }

        /**
         * 测试更新车辆信息
         */
        @Test
        @DisplayName("测试成功更新车辆信息")
        public void testUpdateVehicleSuccess() throws Exception {
            // 准备测试数据
            Integer carId = 1001;
            Integer batteryType = 1;
            Double totalDistance = 10500.0;
            Integer batteryHealth = 92;
            
            // 模拟服务层行为
            String successMessage = "更新车辆信息成功";
            when(vehicleService.updateVehicle(carId, batteryType, totalDistance, batteryHealth)).thenReturn(successMessage);

            // 调用控制器方法
            R<String> result = vehicleController.upadteVehicle(carId, batteryType, totalDistance, batteryHealth);

            // 验证结果
            assertEquals(200, result.getCode());
            assertEquals("ok", result.getMsg());
            assertEquals(successMessage, result.getData());
            
            // 验证服务方法被调用
            verify(vehicleService, times(1)).updateVehicle(carId, batteryType, totalDistance, batteryHealth);
        }

        /**
         * 测试更新车辆信息时发生异常
         */
        @Test
        @DisplayName("测试更新车辆信息时发生异常")
        public void testUpdateVehicleException() throws Exception {
            // 准备测试数据
            Integer carId = 1001;
            Integer batteryType = 1;
            Double totalDistance = 10500.0;
            Integer batteryHealth = 92;
            
            // 模拟服务层抛出异常
            String errorMessage = "车辆信息更新失败，车辆ID不存在";
            when(vehicleService.updateVehicle(carId, batteryType, totalDistance, batteryHealth)).thenThrow(new RuntimeException(errorMessage));

            // 调用控制器方法
            R<String> result = vehicleController.upadteVehicle(carId, batteryType, totalDistance, batteryHealth);

            // 验证结果
            assertEquals(10000, result.getCode());
            assertEquals(errorMessage, result.getMsg());
            assertNull(result.getData());
            
            // 验证服务方法被调用
            verify(vehicleService, times(1)).updateVehicle(carId, batteryType, totalDistance, batteryHealth);
        }
    }
} 