package org.example.mi_bms.mapper;

import org.apache.ibatis.annotations.*;
import org.example.mi_bms.entity.BatteryWarning;

import java.util.List;
import java.util.Map;

@Mapper
public interface BatteryWarningMapper {
    
    /**
     * 保存预警记录
     */
    @Insert("INSERT INTO battery_warning(car_id, rule_id, warn_id, battery_type, warn_name, warn_level, signal_data, raw_signal_data, " +
            "create_time, process_status, processed) " +
            "VALUES(#{carId}, #{ruleId}, #{warnId}, #{batteryType}, #{warnName}, #{warnLevel}, #{signalData}, #{rawSignalData}, " +
            "#{createTime}, #{processStatus}, #{processed})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BatteryWarning warning);
    
    /**
     * 根据ID查询预警记录
     */
    @Select("SELECT * FROM battery_warning WHERE id = #{id}")
    BatteryWarning findById(@Param("id") Integer id);
    
    /**
     * 根据车辆ID查询预警记录
     */
    @Select("SELECT * FROM battery_warning WHERE car_id = #{carId} ORDER BY create_time DESC")
    List<BatteryWarning> findByCarId(@Param("carId") Integer carId);
    
    /**
     * 根据规则ID查询预警记录
     */
    @Select("SELECT * FROM battery_warning WHERE rule_id = #{ruleId} ORDER BY create_time DESC")
    List<BatteryWarning> findByRuleId(@Param("ruleId") Integer ruleId);
    
    /**
     * 查询最近的预警记录
     */
    @Select("SELECT * FROM battery_warning ORDER BY create_time DESC LIMIT #{limit}")
    List<BatteryWarning> findRecent(@Param("limit") Integer limit);
    
    /**
     * 根据处理状态查询预警记录
     */
    @Select("SELECT * FROM battery_warning WHERE process_status = #{processStatus} ORDER BY create_time DESC")
    List<BatteryWarning> findByProcessStatus(@Param("processStatus") Integer processStatus);
    
    /**
     * 查询所有预警记录
     */
    @Select("SELECT * FROM battery_warning ORDER BY create_time DESC")
    List<BatteryWarning> findAll();
    
    /**
     * 更新预警记录
     */
    @Update("UPDATE battery_warning SET process_status=#{processStatus}, process_time=#{processTime}, " +
            "process_user=#{processUser}, remark=#{remark} WHERE id=#{id}")
    int update(BatteryWarning warning);
    
    /**
     * 删除预警记录
     */
    @Delete("DELETE FROM battery_warning WHERE id=#{id}")
    int delete(@Param("id") Integer id);
    
    /**
     * 获取未处理的电池信号数据（替代原battery_signals表的查询）
     */
    @Select("SELECT id, car_id, warn_id, signal_data FROM battery_warning WHERE processed = 0 ORDER BY create_time ASC LIMIT 100")
    List<Map<String, Object>> getUnprocessedBatterySignals();
    
    /**
     * 将信号数据标记为已处理（替代原battery_signals表的更新）
     */
    @Update("<script>" +
            "UPDATE battery_warning SET processed = 1, process_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='signalIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int updateSignalsProcessed(@Param("signalIds") List<Integer> signalIds);
} 